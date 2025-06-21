package com.maria.service;

import com.maria.constant.AuctionServiceConstants;
import com.maria.core.entity.*;
import com.maria.dto.AuctionUpdateDTO;
import com.maria.dto.CreateAuctionRequestDTO;
import com.maria.exception.*;
import com.maria.mapper.AuctionMapper;
import com.maria.entity.Auction;
import com.maria.repository.AuctionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.Disposable;
import reactor.core.Disposables;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AuctionServiceImpl implements AuctionService {
    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;
    private final ReactiveSetOperations<String, String> setOperations;
    private final ReactiveRedisTemplate<String, AuctionDTO> auctionRedisTemplate;
    private final ReactiveValueOperations<String, AuctionDTO> valueOperationsAuction;
    private final WebClient webClientItem;
    private final WebClient webClientUser;
    private final AuctionKafkaService auctionKafkaService;
    private final Disposable.Composite disposables = Disposables.composite();
    @Value("${uri.get-seller}")
    private String getSellerUri;
    @Value("${uri.find-user}")
    private String findUserUri;

    @Autowired
    public AuctionServiceImpl(
            AuctionRepository auctionRepository,
            AuctionMapper auctionMapper,
            ReactiveSetOperations<String, String> setOperations,
            ReactiveRedisTemplate<String, AuctionDTO> auctionRedisTemplate,
            ReactiveValueOperations<String, AuctionDTO> valueOperationsAuction,
            @Qualifier("webClientItem") WebClient webClientItem,
            @Qualifier("webClientUser") WebClient webClientUser,
            AuctionKafkaService auctionKafkaService
    ) {
        this.auctionRepository = auctionRepository;
        this.auctionMapper = auctionMapper;
        this.setOperations = setOperations;
        this.auctionRedisTemplate = auctionRedisTemplate;
        this.valueOperationsAuction = valueOperationsAuction;
        this.webClientItem = webClientItem;
        this.webClientUser = webClientUser;
        this.auctionKafkaService = auctionKafkaService;
    }

    @PostConstruct
    public void initialize() {
        auctionKafkaService.listenToBids(this::updateHighestBid);
        auctionKafkaService.listenToAcceptances(this::processAcceptanceEvent);
    }

    @Override
    public Mono<Void> updateHighestBid(NewBitEvent newBitEvent) {
        return auctionRepository.findById(newBitEvent.getAuctionId())
                .switchIfEmpty(Mono.error(new AuctionNotExistException(AuctionServiceConstants.EX_AUCTION_NOT_EXIST)))
                .flatMap(auction -> validateBid(auction, newBitEvent, LocalDateTime.now())
                        .doOnSuccess(validatedAuction -> log.info(AuctionServiceConstants.LOG_BID_VALID, validatedAuction.getAuctionId())))
                .flatMap(auction -> {
                    auction.setBidderId(newBitEvent.getBidderId());
                    auction.setCurrentPrice(newBitEvent.getBidAmount());
                    return auctionRepository.save(auction)
                            .doOnSuccess(savedAuction -> log.info(AuctionServiceConstants.LOG_AUCTION_SAVED, savedAuction.getAuctionId()))
                            .flatMap(updatedAuction -> {
                                String key = "auctions:" + updatedAuction.getAuctionId();

                                return auctionRedisTemplate.hasKey(key)
                                        .flatMap(hasKey -> {
                                            if (hasKey) {
                                                return valueOperationsAuction.set(key, auctionMapper.toDto(updatedAuction))
                                                        .then(auctionKafkaService.sendNewBidNotificationEvent(updatedAuction));
                                            }
                                            return auctionKafkaService.sendNewBidNotificationEvent(updatedAuction);
                                        });
                            });
                })
                .onErrorMap(ex -> {
                    log.warn(AuctionServiceConstants.LOG_ERROR_UPDATING_AUCTION, ex.getMessage());
                    if (ex instanceof BitNotPossibleException || ex instanceof AuctionNotExistException) {
                        return ex;
                    }
                    log.warn(AuctionServiceConstants.LOG_UNEXPECTED_ERROR, ex.getMessage());
                    return new Exception(AuctionServiceConstants.EX_FAIL_UPDATE_AUCTION);
                });
    }

    private Mono<Auction> validateBid(Auction auction, NewBitEvent newBitEvent, LocalDateTime bidTime) {
        if (auction.getSellerId().equals(newBitEvent.getBidderId())) {
            return Mono.error(new BitNotPossibleException(AuctionServiceConstants.EX_SELLER_BID_IN_HIS_AUCTION));
        }
        if (bidTime.isAfter(auction.getEndDate()) || bidTime.isBefore(auction.getStartDate()) || auction.isFinished()) {
            return Mono.error(new BitNotPossibleException(AuctionServiceConstants.EX_UNAVAILABLE_AUCTION_FOR_BID));
        }
        if (newBitEvent.getBidAmount().compareTo(auction.getCurrentPrice()) <= 0) {
            return Mono.error(new BitNotPossibleException(AuctionServiceConstants.EX_LOW_BID));
        }
        return Mono.just(auction);
    }

    @Override
    public Mono<AuctionDTO> createAuction(CreateAuctionRequestDTO auctionRequest, Long currentUserId) {
        return auctionRepository.findByItemId(auctionRequest.getItemId())
                .flatMap(existingAuction ->
                        Mono.<AuctionDTO>error(new DataForAuctionIsNotValid(AuctionServiceConstants.EX_AUCTION_ITEM_ALREADY_EXISTS)))
                .switchIfEmpty(Mono.defer(() -> validateAndCreateAuction(auctionRequest, currentUserId)))
                .onErrorMap(ex -> {
                    log.warn(AuctionServiceConstants.LOG_ERROR_SAVING_AUCTION, ex.getMessage());
                    if (ex instanceof DataForAuctionIsNotValid || ex instanceof AuctionWebClientException
                            || ex instanceof ItemNotExistException || ex instanceof AuctionNotAvailableException) {
                        return ex;
                    }
                    log.warn(AuctionServiceConstants.LOG_UNEXPECTED_ERROR, ex.getMessage());
                    return new Exception(AuctionServiceConstants.EX_FAIL_TO_CREATE_AUCTION);
                });
    }

    private Mono<AuctionDTO> validateAndCreateAuction(CreateAuctionRequestDTO auctionRequest, Long currentUserId) {
        return validateItemOwnership(auctionRequest.getItemId(), currentUserId)
                .then(validateAuctionDates(auctionRequest))
                .then(createAndSaveAuction(auctionRequest, currentUserId));
    }

    private Mono<Void> validateAuctionDates(CreateAuctionRequestDTO auctionRequest) {
        if (auctionRequest.getStartDate().isAfter(auctionRequest.getEndDate()) || auctionRequest.getStartDate().isBefore(LocalDateTime.now())) {
            return Mono.error(new DataForAuctionIsNotValid(AuctionServiceConstants.EX_TIME_INCORRECT));
        }
        return Mono.empty();
    }

    private Mono<AuctionDTO> createAndSaveAuction(CreateAuctionRequestDTO auctionRequest, Long currentUserId) {
        Auction newAuction = Auction.builder()
                .itemId(auctionRequest.getItemId())
                .startingPrice(auctionRequest.getStartingPrice())
                .sellerId(currentUserId)
                .startDate(auctionRequest.getStartDate())
                .endDate(auctionRequest.getEndDate())
                .bidderId(null)
                .currentPrice(auctionRequest.getStartingPrice())
                .finished(false)
                .publicAccess(auctionRequest.isPublic())
                .build();

        return auctionRepository.save(newAuction)
                .flatMap(auction -> auctionKafkaService.sendAuctionCreatedEvent(auction)
                        .thenReturn(auctionMapper.toDto(auction)))
                .doOnSuccess(savedAuctionDTO -> log.info(AuctionServiceConstants.LOG_AUCTION_SAVED, savedAuctionDTO.getAuctionId()))
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_SAVING_AUCTION, ex.getMessage());
                    return Mono.error(new DatabaseOperationException(AuctionServiceConstants.EX_FAIL_SAVE_AUCTION));
                });
    }

    private Mono<Long> validateItemOwnership(Long itemId, Long currentUserId) {
        return webClientItem
                .get()
                .uri(getSellerUri, itemId)
                .header("X-Internal-Service", "true")
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error(AuctionServiceConstants.LOG_FAIL_GET_ITEM, errorBody);
                                    return Mono.error(new AuctionWebClientException(AuctionServiceConstants.EX_SERVICE_ERROR));
                                }))
                .bodyToMono(Long.class)
                .filter(id -> id.equals(currentUserId))
                .switchIfEmpty(Mono.error(new AuctionNotAvailableException(AuctionServiceConstants.EX_ITEM_NOT_BELONG_TO_USER)));
    }

    @Override
    public Mono<Void> sendInvitation(Long auctionId, Long sellerId, List<String> userEmails) {
        return auctionAvailableToInteraction(auctionId, sellerId)
                .flatMap(auction -> resolveUsersId(userEmails)
                        .flatMapMany(emailToIdMap -> Flux.fromIterable(emailToIdMap.values())
                                .flatMap(userId -> {
                                    if (userId != -1L) {
                                        return auctionKafkaService.sendInvitationEvent(auctionId, sellerId, userId)
                                                .doOnNext(result -> log.info(AuctionServiceConstants.LOG_INVITATION_SENT, auctionId, userId))
                                                .onErrorContinue((throwable, obj) ->
                                                        log.warn(AuctionServiceConstants.LOG_FAIL_SEND_INVITATION_DETAILS, auction.getAuctionId(), userId));
                                    } else {
                                        log.warn(AuctionServiceConstants.LOG_USER_NOT_FOUND, auctionId, userId);
                                        return Mono.empty();
                                    }
                                })
                        )
                        .then())
                .onErrorMap(ex -> {
                    log.warn(AuctionServiceConstants.LOG_FAIL_SEND_INVITATION_ERROR_MESSAGE, ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException) {
                        return ex;
                    }
                    log.warn(AuctionServiceConstants.LOG_UNEXPECTED_ERROR, ex.getMessage());
                    return new Exception(AuctionServiceConstants.EX_FAIL_SEND_INVITATION);
                });
    }

    @Override
    public Mono<Void> processAcceptanceEvent(AcceptanceEvent acceptanceEvent) {
        return auctionRepository.findById(acceptanceEvent.getAuctionId())
                .switchIfEmpty(Mono.error(new AuctionNotExistException(AuctionServiceConstants.EX_AUCTION_NOT_EXIST)))
                .flatMap(auction -> {
                    if (auction.isFinished()) {
                        return Mono.error(new AuctionNotAvailableException(AuctionServiceConstants.EX_AUCTION_NOT_AVAILABLE));
                    }
                    if (!acceptanceEvent.isAcceptance()) {
                        return Mono.empty();
                    }
                    return saveUserForAuction(auction.getAuctionId(), acceptanceEvent.getUserId())
                            .flatMap(result -> {
                                if (result == 0) {
                                    return Mono.error(new RedisOperationException(AuctionServiceConstants.EX_REDIS_ERROR));
                                }
                                log.info(AuctionServiceConstants.LOG_USER_ADDED_TO_AUCTION);
                                return Mono.empty().then();
                            });
                })
                .onErrorMap(ex -> {
                    log.warn(AuctionServiceConstants.LOG_ACCEPTANCE_ERROR, ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException
                            || ex instanceof RedisOperationException) {
                        return ex;
                    }
                    log.warn(AuctionServiceConstants.LOG_UNEXPECTED_ERROR, ex.getMessage());
                    return new Exception(AuctionServiceConstants.EX_ACCEPTANCE_ERROR);
                });
    }

    @Override
    public Flux<AuctionDTO> getAllActivePrivateAuctions(Long userId) {
        return getAllActiveAuctionsForUser(userId)
                .filter(auction -> !auction.isPublicAccess());
    }

    @Override
    public Mono<AuctionDTO> getAuction(Long auctionId, Long currentUserId) {
        String key = "auctions:" + auctionId;

        return auctionRedisTemplate.hasKey(key)
                .flatMap(exists -> exists ? getCacheAuction(auctionId) : auctionRepository.findById(auctionId).map(auctionMapper::toDto))
                .flatMap(auction -> {
                    if (auction.isPublicAccess() || auction.getSellerId().equals(currentUserId)) {
                        return Mono.just(auction);
                    }
                    return canUserSeeAuction(currentUserId, auctionId)
                            .flatMap(access -> {
                                if (access) {
                                    return Mono.just(auction);
                                } else {
                                    log.error(AuctionServiceConstants.LOG_NO_ACCESS_TO_AUCTION, currentUserId);
                                    return Mono.error(new AuctionNotAvailableException(AuctionServiceConstants.EX_USER_NOT_PARTICIPANT));
                                }
                            });
                })
                .switchIfEmpty(Mono.error(new AuctionNotExistException(AuctionServiceConstants.EX_AUCTION_NOT_EXIST)));
    }

    @Override
    public Mono<AuctionDTO> updateAuction(Long currentUserId, Long auctionId, AuctionUpdateDTO auctionUpdateDTO) {
        return auctionAvailableToInteraction(auctionId, currentUserId)
                .flatMap(auction -> {
                    if (auction.getStartDate().isBefore(LocalDateTime.now())) {
                        return Mono.error(new AuctionStatusException(AuctionServiceConstants.EX_AUCTION_ALREADY_STARTED));
                    }
                    if (auctionUpdateDTO.getStartingPrice() != null) {
                        auction.setStartingPrice(auctionUpdateDTO.getStartingPrice());
                        auction.setCurrentPrice(auctionUpdateDTO.getStartingPrice());
                    }
                    if (auctionUpdateDTO.getStartDate() != null
                            && auctionUpdateDTO.getStartDate().isAfter(LocalDateTime.now())
                            && auctionUpdateDTO.getStartDate().isBefore(auction.getStartDate())) {
                        auction.setStartDate(auctionUpdateDTO.getStartDate());
                    }
                    if (auctionUpdateDTO.getEndDate() != null
                            && auctionUpdateDTO.getEndDate().isAfter(LocalDateTime.now())
                            && auctionUpdateDTO.getEndDate().isAfter(auction.getStartDate())) {
                        auction.setEndDate(auctionUpdateDTO.getEndDate());
                    }
                    if (auctionUpdateDTO.getPublicAccess() != null) {
                        auction.setPublicAccess(auctionUpdateDTO.getPublicAccess());
                    }
                    return auctionRepository.save(auction)
                            .flatMap(updatedAuction -> {
                                String key = "auctions:" + updatedAuction.getAuctionId();

                                return auctionRedisTemplate.hasKey(key)
                                        .flatMap(hasKey -> {
                                            if (hasKey) {
                                                return valueOperationsAuction.set(key, auctionMapper.toDto(updatedAuction))
                                                        .thenReturn(updatedAuction);
                                            }
                                            return Mono.just(updatedAuction);
                                        });
                            })
                            .map(auctionMapper::toDto)
                            .doOnSuccess(savedAuctionDTO -> log.info(AuctionServiceConstants.LOG_AUCTION_SAVED, savedAuctionDTO.getAuctionId()))
                            .onErrorMap(ex -> new DatabaseOperationException(AuctionServiceConstants.EX_FAIL_SAVE_AUCTION));
                })
                .onErrorMap(ex -> {
                    log.warn(AuctionServiceConstants.LOG_ERROR_UPDATING_AUCTION, ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException
                            || ex instanceof AuctionStatusException || ex instanceof DatabaseOperationException) {
                        return ex;
                    }
                    log.warn(AuctionServiceConstants.LOG_UNEXPECTED_ERROR, ex.getMessage());
                    return new Exception(AuctionServiceConstants.EX_FAIL_UPDATE_AUCTION);
                });
    }

    @Override
    public Mono<Long> getSellerId(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .map(Auction::getSellerId)
                .switchIfEmpty(Mono.error(new AuctionNotExistException(AuctionServiceConstants.EX_AUCTION_NOT_EXIST)))
                .onErrorMap(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_GET_SELLER_ID, ex.getMessage());
                    if (ex instanceof AuctionNotExistException) {
                        return ex;
                    }
                    return new DatabaseOperationException(AuctionServiceConstants.EX_FAIL_GET_SELLER);
                });
    }

    @Override
    public Flux<AuctionDTO> getAllActivePublicAuctions() {
        return auctionRepository.findByFinishedIsFalseAndPublicAccessIsTrue()
                .map(auctionMapper::toDto)
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_GET_PUBLIC_AUCTIONS, ex.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Flux<AuctionDTO> getAllSellersAuctions(Long userId) {
        return auctionRepository.findBySellerId(userId)
                .map(auctionMapper::toDto)
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_GET_SELLERS_AUCTIONS, ex.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Flux<AuctionDTO> getAllActiveAuctionsForUser(Long userId) {
        return auctionRepository.findByFinishedIsFalse()
                .filter(auction -> !auction.getSellerId().equals(userId))
                .flatMap(auction -> {
                    if (auction.getPublicAccess()) {
                        return Mono.just(auction);
                    } else {
                        return canUserSeeAuction(userId, auction.getAuctionId())
                                .filter(Boolean::booleanValue)
                                .map(access -> auction);
                    }
                })
                .map(auctionMapper::toDto)
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_GET_ACTIVE_AUCTIONS, ex.getMessage());
                    return Flux.empty();
                });
    }

    @Override
    public Mono<AuctionDTO> closeAuction(Long auctionId, Long currentUserId) {
        return auctionAvailableToInteraction(auctionId, currentUserId)
                .flatMap(auction -> setAuctionFinished(auctionId)
                        .map(auctionMapper::toDto))
                .doOnSuccess(auctionDTO -> log.info(AuctionServiceConstants.LOG_AUCTION_CLOSED, auctionDTO.getAuctionId()))
                .onErrorMap(ex -> {
                    log.warn(AuctionServiceConstants.LOG_FAIL_CLOSE_AUCTION, auctionId, ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException
                            || ex instanceof RedisOperationException) {
                        return ex;
                    }
                    return new Exception(AuctionServiceConstants.EX_FAIL_CLOSE_AUCTION);
                });
    }

    @Override
    public Mono<Void> deleteAuction(Long auctionId, Long currentUserId) {
        return auctionRepository.findById(auctionId)
                .switchIfEmpty(Mono.error(new AuctionNotExistException(AuctionServiceConstants.EX_AUCTION_NOT_EXIST)))
                .flatMap(auction -> {
                    if (!auction.getSellerId().equals(currentUserId)) {
                        return Mono.error(new AuctionNotAvailableException(AuctionServiceConstants.EX_AUCTION_NOT_AVAILABLE));
                    }
                    return setAuctionFinished(auctionId)
                            .then(auctionKafkaService.sendAuctionRemovedEvent(auction))
                            .then(auctionRepository.delete(auction))
                            .onErrorResume(ex -> Mono.error(new DatabaseOperationException(AuctionServiceConstants.EX_FAIL_DELETE_AUCTION)))
                            .doOnSuccess(success -> log.info(AuctionServiceConstants.LOG_AUCTION_DELETED));
                })
                .onErrorMap(ex -> {
                    log.warn(AuctionServiceConstants.LOG_FAIL_DELETE_AUCTION, auctionId, ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException ||
                            ex instanceof DatabaseOperationException) {
                        return ex;
                    }
                    return new Exception(AuctionServiceConstants.EX_FAIL_DELETE_AUCTION);
                });
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        scheduleAuctionCaching();
        scheduleAuctionSetFinished();
    }

    public void scheduleAuctionCaching() {
        disposables.add(
                Flux.interval(Duration.ofMinutes(15))
                        .flatMap(tick -> cacheAuctionsEndingSoon())
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnNext(success -> log.info(AuctionServiceConstants.LOG_AUCTION_CACHED))
                        .doOnError(error -> log.error(AuctionServiceConstants.LOG_ERROR_CACHING_AUCTION, error.getMessage()))
                        .subscribe());
    }

    public void scheduleAuctionSetFinished() {
        disposables.add(
                Flux.interval(Duration.ofMinutes(60))
                        .flatMap(tick -> findExpiredAuctions())
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnNext(success -> log.info(AuctionServiceConstants.LOG_EXPIRED_AUCTIONS_PROCESSED))
                        .doOnError(error -> log.error(AuctionServiceConstants.LOG_ERROR_PROCESS_EXPIRED_AUCTIONS, error))
                        .subscribe());
    }

    @PreDestroy
    public void shutdownSchedulers() {
        disposables.dispose();
    }

    private Mono<Void> cacheAuctionsEndingSoon() {
        LocalDateTime nowDate = LocalDateTime.now();
        LocalDateTime oneHourLater = nowDate.plusHours(1);

        return auctionRepository.findByEndDateBetween(nowDate, oneHourLater)
                .filter(auction -> !auction.isFinished())
                .collectList()
                .flatMap(auctions -> {
                    if (auctions.isEmpty()) {
                        log.info(AuctionServiceConstants.LOG_NO_AUCTIONS_ENDING_SOON);
                        return Mono.empty();
                    }
                    Map<String, AuctionDTO> auctionMap = auctions.stream().
                            collect(Collectors.toMap(
                                    auction -> "auctions:" + auction.getAuctionId(),
                                    auctionMapper::toDto
                            ));
                    return auctionRedisTemplate.opsForValue().multiSet(auctionMap);
                })
                .then()
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_CACHING_AUCTION, ex.getMessage());
                    return Mono.error(new RedisOperationException(AuctionServiceConstants.EX_ERROR_CACHING_AUCTIONS));
                });
    }

    private Mono<AuctionDTO> getCacheAuction(Long auctionId) {
        String key = "auctions:" + auctionId;
        return valueOperationsAuction.get(key)
                .retry(3)
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_GET_AUCTION_FROM_CACHE, auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException(AuctionServiceConstants.EX_FAIL_GET_AUCTION_IN_CACHE));
                });
    }

    private Mono<Void> findExpiredAuctions() {
        return auctionRedisTemplate.keys("auctions:*")
                .flatMap(key -> valueOperationsAuction.get(key)
                        .switchIfEmpty(Mono.empty())
                )
                .filter(auctionDTO -> auctionDTO.getEndDate().isBefore(LocalDateTime.now()))
                .flatMap(auctionDTO -> setAuctionFinished(auctionDTO.getAuctionId()))
                .onErrorContinue((error, obj) -> log.error(AuctionServiceConstants.LOG_ERROR_SETTING_AUCTION_FINISHED + error.getMessage()))
                .then();
    }

    private Mono<Void> cacheAuctionEndingSoon() {
        LocalDateTime nowDate = LocalDateTime.now();
        LocalDateTime oneHourLater = nowDate.plusHours(1);

        return auctionRepository.findByEndDateBetween(nowDate, oneHourLater)
                .filter(auction -> !auction.isFinished())
                .collectList()
                .flatMap(auctions -> {
                    if (auctions.isEmpty()) {
                        log.info(AuctionServiceConstants.LOG_NO_AUCTIONS_ENDING_SOON);
                        return Mono.empty();
                    }
                    Map<String, AuctionDTO> auctionMap = auctions.stream().
                            collect(Collectors.toMap(
                                    auction -> "auctions:" + auction.getAuctionId(),
                                    auctionMapper::toDto
                            ));
                    return auctionRedisTemplate.opsForValue().multiSet(auctionMap);
                })
                .then()
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_CACHING_AUCTION, ex.getMessage());
                    return Mono.error(new RedisOperationException(AuctionServiceConstants.EX_ERROR_CACHING_AUCTIONS));
                });
    }

    private Mono<Auction> setAuctionFinished(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .flatMap(auction -> {
                    auction.setFinished(true);

                    return auctionRepository.save(auction)
                            .doOnSuccess(savedAuction -> log.info(AuctionServiceConstants.LOG_AUCTION_SET_FINISHED, savedAuction.getAuctionId()))
                            .flatMap(updatedAuction ->
                                    auctionKafkaService.sendAuctionFinishedNotificationEvent(updatedAuction)
                                            .onErrorResume(ex -> {
                                                log.error(AuctionServiceConstants.LOG_FAIL_SEND_NOTIFICATION, updatedAuction.getAuctionId(), ex.getMessage());
                                                return Mono.empty();
                                            })
                                            .thenReturn(updatedAuction))
                            .flatMap(this::handleCacheRemoval);
                });
    }

    private Mono<Auction> handleCacheRemoval(Auction auction) {
        String key = "auctions:" + auction.getAuctionId();
        return auctionRedisTemplate.hasKey(key)
                .flatMap(exist -> {
                    if (exist) {
                        return removeAuctionFromCache(auction.getAuctionId())
                                .flatMap(result -> {
                                    if (result == 0) {
                                        log.error(AuctionServiceConstants.LOG_FAIL_REMOVE_AUCTION_FROM_CACHE, auction.getAuctionId());
                                        return Mono.error(new RedisOperationException(AuctionServiceConstants.EX_FAIL_REMOVE_AUCTION_FROM_CACHE));
                                    }
                                    return Mono.just(auction);
                                });
                    }
                    return Mono.just(auction);
                });
    }

    private Mono<Long> removeAuctionFromCache(Long auctionId) {
        String auctionKey = "auctions:" + auctionId;
        return auctionRedisTemplate.delete(auctionKey)
                .retry(3)
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_REMOVING_AUCTION_FROM_CACHE, auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException(AuctionServiceConstants.EX_FAIL_REMOVE_AUCTION_FROM_CACHE));
                });
    }

    private Mono<Boolean> canUserSeeAuction(Long userId, Long auctionId) {
        String key = "auction:" + auctionId + ":users";
        return setOperations.isMember(key, String.valueOf(userId))
                .retry(3)
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_CHECK_IF_USER_SEE_AUCTION, userId, auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException(AuctionServiceConstants.EX_FAIL_TO_CHECK_USER));
                });
    }

    private Mono<Long> saveUserForAuction(Long auctionId, Long userId) {
        String key = "auction:" + auctionId + ":users";
        return setOperations.add(key, String.valueOf(userId))
                .retry(3)
                .onErrorResume(ex -> {
                    log.error(AuctionServiceConstants.LOG_ERROR_SAVING_USER_TO_AUCTION, userId, auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException(AuctionServiceConstants.EX_FAIL_TO_SAVE_USER));
                });
    }

    private Mono<Auction> auctionAvailableToInteraction(Long auctionId, Long userId) {
        return auctionRepository.findById(auctionId)
                .switchIfEmpty(Mono.error(new AuctionNotExistException(AuctionServiceConstants.EX_AUCTION_NOT_EXIST)))
                .filter(auction -> !auction.isFinished() && auction.getSellerId().equals(userId))
                .switchIfEmpty(Mono.error(new AuctionNotAvailableException(AuctionServiceConstants.EX_AUCTION_NOT_AVAILABLE)));
    }

    private Mono<Map<String, Long>> resolveUsersId(List<String> emails) {
        return Flux.fromIterable(emails)
                .flatMap(email -> webClientUser
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path(findUserUri)
                                .build(email))
                        .header("X-Internal-Service", "true")
                        .retrieve()
                        .onStatus(HttpStatus.NOT_FOUND::equals, response -> {
                            log.error(AuctionServiceConstants.LOG_USER_EMAIL_NOT_FONDED, email);
                            return Mono.empty();
                        })
                        .onStatus(HttpStatusCode::isError, response ->
                                response.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            log.error(AuctionServiceConstants.LOG_FAIL_GET_USER, errorBody);
                                            return Mono.error(new AuctionWebClientException(AuctionServiceConstants.EX_SERVICE_ERROR));
                                        }))
                        .bodyToMono(SharedUser.class)
                        .map(user -> Map.entry(email, user.getUserId()))
                        .defaultIfEmpty(Map.entry(email, -1L)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
