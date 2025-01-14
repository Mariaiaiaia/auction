package com.maria.service;

import com.maria.core.entity.*;
import com.maria.dto.AuctionUpdateDTO;
import com.maria.dto.CreateAuctionRequestDTO;
import com.maria.exception.*;
import com.maria.mapper.AuctionMapper;
import com.maria.entity.Auction;
import com.maria.repository.AuctionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
public class AuctionServiceImpl implements AuctionService {
    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;
    private final ReactiveSetOperations<String, String> setOperations;
    private final ReactiveRedisTemplate<String, AuctionDTO> auctionRedisTemplate;
    private final ReactiveValueOperations<String, AuctionDTO> valueOperationsAuction;
    private final WebClient webClient;
    private final AuctionKafkaService auctionKafkaService;
    private final Disposable.Composite disposables = Disposables.composite();

    @PostConstruct
    public void initialize() {
        auctionKafkaService.listenToBids(this::updateHighestBid);
        auctionKafkaService.listenToAcceptances(this::processAcceptanceEvent);
    }


    @Override
    public Mono<Void> updateHighestBid(NewBitEvent newBitEvent){
        return auctionRepository.findById(newBitEvent.getAuctionId())
                .switchIfEmpty(Mono.error(new AuctionNotExistException("Auction does not exist")))
                .flatMap(auction -> validateBid(auction, newBitEvent, LocalDateTime.now())
                        .doOnSuccess(validatedAuction -> log.info("Bid is validate for auction, auction id: {}", validatedAuction.getAuctionId())))
                .flatMap(auction -> {
                    auction.setBidderId(newBitEvent.getBidderId());
                    auction.setCurrentPrice(newBitEvent.getBidAmount());
                    return auctionRepository.save(auction)
                            .doOnSuccess(savedAuction -> log.info("Auction successfully saved, auction id: {}", savedAuction.getAuctionId()))
                            .flatMap(updatedAuction -> {
                                String key = "auctions:" + updatedAuction.getAuctionId();

                                return valueOperationsAuction.set(key, auctionMapper.toDto(updatedAuction))
                                        .then(auctionKafkaService.sendNewBidNotificationEvent(updatedAuction));
                            });
                })
                .onErrorMap(ex -> {
                    log.warn("Error occurred while updating auction: {}", ex.getMessage());
                    if (ex instanceof BitNotPossibleException || ex instanceof AuctionNotExistException) {
                        return ex;
                    }

                    log.warn("Unexpected error: {}", ex.getMessage());
                    return new Exception("Failed to update auction");
                });
    }


    private Mono<Auction> validateBid(Auction auction, NewBitEvent newBitEvent, LocalDateTime bidTime) {
        if (auction.getSellerId().equals(newBitEvent.getBidderId())) {
            return Mono.error(new BitNotPossibleException("Seller cannot bid in his auction"));
        }

        if (bidTime.isAfter(auction.getEndDate()) || bidTime.isBefore(auction.getStartDate()) || auction.isFinished()) {
            return Mono.error(new BitNotPossibleException("It is not possible to place a bid in this auction"));
        }

        if (newBitEvent.getBidAmount().compareTo(auction.getCurrentPrice()) <= 0) {
            return Mono.error(new BitNotPossibleException("Bid amount must be higher than the current highest bid"));
        }

        return Mono.just(auction);
    }


    @Override
    public Mono<AuctionDTO> createAuction(CreateAuctionRequestDTO auctionRequest, Long currentUserId){
        return auctionRepository.findByItemId(auctionRequest.getItemId())
                .flatMap(existingAuction ->
                        Mono.<AuctionDTO>error(new DataForAuctionIsNotValid("An auction with this item already exists")))
                .switchIfEmpty(Mono.defer(() -> validateAndCreateAuction(auctionRequest, currentUserId)))
                .onErrorMap(ex -> {
                    log.warn("Error occurred while saving auction: {}", ex.getMessage());
                    if (ex instanceof DataForAuctionIsNotValid || ex instanceof AuctionWebClientException
                    || ex instanceof ItemNotExistException || ex instanceof AuctionNotAvailableException) {
                        return ex;
                    }
                    log.warn("Unexpected error: {}", ex.getMessage());
                    return new Exception("Failed to create auction");
                });
    }


    private Mono<AuctionDTO> validateAndCreateAuction(CreateAuctionRequestDTO auctionRequest, Long currentUserId){
        return validateItemOwnership(auctionRequest.getItemId(), currentUserId)
                .then(validateAuctionDates(auctionRequest))
                .then(createAndSaveAuction(auctionRequest, currentUserId));
    }


    private Mono<Void> validateAuctionDates(CreateAuctionRequestDTO auctionRequest){
        if(auctionRequest.getStartDate().isAfter(auctionRequest.getEndDate()) || auctionRequest.getStartDate().isBefore(LocalDateTime.now())){
            return Mono.error(new DataForAuctionIsNotValid("The auction time is incorrect"));
        }
        return Mono.empty();
    }


    private Mono<AuctionDTO> createAndSaveAuction(CreateAuctionRequestDTO auctionRequest, Long currentUserId){
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
                .doOnSuccess(savedAuctionDTO -> log.info("Auction successfully saved, auction id: {}", savedAuctionDTO.getAuctionId()))
                .onErrorResume(ex -> {
                    log.error("Failed to save auction: {}", ex.getMessage());
                    return Mono.error(new DatabaseOperationException("Failed to save the auction"));
                });
    }


    private Mono<ItemDTO> validateItemOwnership(Long itemId, Long currentUserId) {
        return webClient
                .get()
                .uri("/items/{id}", itemId)
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals,response -> {
                    log.error("Item is not founded: {}", itemId);
                    return Mono.error(new ItemNotExistException("This item does not exist"));
                })
                .onStatus(HttpStatusCode::isError, response ->
                    response.bodyToMono(String.class)
                                    .flatMap(errorBody -> {
                                        log.error("Failed to get the item: {}", errorBody);
                                        return Mono.error(new AuctionWebClientException("Service error occurred"));
                                    }))
                .bodyToMono(ItemDTO.class)
                .filter(item -> item.getSellerId().equals(currentUserId))
                .switchIfEmpty(Mono.error(new AuctionNotAvailableException("This item does not belong to this user")));
    }


    @Override
    public Mono<Void> sendInvitation(Long auctionId, Long sellerId, List<String> userEmails){
        return auctionAvailableToInteraction(auctionId, sellerId)
                .flatMap(auction -> resolveUsersId(userEmails)
                                .flatMapMany(emailToIdMap ->  Flux.fromIterable(emailToIdMap.values())
                                                .flatMap(userId -> {
                                                    if(userId != -1L){
                                                        return auctionKafkaService.sendInvitationEvent(auctionId, sellerId, userId)
                                                                .doOnNext(result -> log.info("Invitation sent for auction {} to user {}", auctionId, userId))
                                                                .onErrorContinue((throwable, obj) ->
                                                                        log.warn("Failed to send invitation for auction {} to user {}", auction.getAuctionId(), userId));
                                                    }else {
                                                        log.warn("User not found for auction {}: userId = {}", auctionId, userId);
                                                        return Mono.empty();
                                                    }
                                                })
                                )
                        .then())
                .onErrorMap(ex -> {
                    log.warn("Error occurred while sending invitation: {}", ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException) {
                        return ex;
                    }
                    log.warn("Unexpected error: {}", ex.getMessage());
                    return new Exception("Failed to send invitation");
                });
    }


    @Override
    public Mono<Void> processAcceptanceEvent(AcceptanceEvent acceptanceEvent){
        return auctionRepository.findById(acceptanceEvent.getAuctionId())
                .switchIfEmpty(Mono.error(new AuctionNotExistException("Auction does not exist")))
                .flatMap(auction -> {
                    if(auction.isFinished()){
                        return Mono.error(new AuctionNotAvailableException("Auction is not available"));
                    }

                    if(!acceptanceEvent.isAcceptance()){
                        return Mono.empty();
                    }

                    return saveUserForAuction(auction.getAuctionId(), acceptanceEvent.getUserId())
                            .flatMap(result -> {
                                if(result == 0){
                                    return Mono.error(new RedisOperationException("An unexpected error occurred while accessing Redis"));
                                }
                                log.info("User successfully added to the auction");
                                return Mono.empty().then();
                            });
                })
                .onErrorMap(ex -> {
                    log.warn("Error during acceptance processing: {}", ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException
                    || ex instanceof RedisOperationException) {
                        return ex;
                    }
                    log.warn("Unexpected error: {}", ex.getMessage());
                    return new Exception("Failed to process acceptance");
                });
    }


    @Override
    public Flux<AuctionDTO> getAllActivePrivateAuctions(Long userId){
        return getAllActiveAuctionsForUser(userId)
                .filter(auction -> !auction.isPublicAccess());
    }


    @Override
    public Mono<Void> removeUserFromAuction(String removedUserEmail, Long auctionId, Long currentUserId){
        return auctionAvailableToInteraction(auctionId, currentUserId)
                .flatMap(auction -> getUserByEmail(removedUserEmail)
                        .filterWhen(user -> canUserSeeAuction(user.getUserId(), auctionId))
                        .switchIfEmpty(Mono.error(new UserException("The user is not a participant in this auction")))
                        .flatMap(user -> removeAuctionUser(user.getUserId(), auctionId)
                                .flatMap(result -> {
                                    if(result == 0){
                                        log.error("Failed to remove user {} from auction {}", user.getUserId(), auctionId);
                                        return Mono.error(new RedisOperationException("An unexpected error occurred while accessing Redis"));
                                    }
                                        log.info("User successfully removed");
                                        return auctionKafkaService.sendUserRemovedFromAuctionEvent(auction, user.getUserId());
                                })))
                .onErrorMap(ex -> {
                    log.warn("Error during user removing: {}", ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException
                            || ex instanceof UserException || ex instanceof AuctionWebClientException
                    || ex instanceof RedisOperationException) {
                        return ex;
                    }
                    log.warn("Unexpected error: {}", ex.getMessage());
                    return new Exception("Failed to remove user");
                });
    }


    private Mono<SharedUser> getUserByEmail (String removedUserEmail){
        return webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/users/find_user/{userEmail}")
                        .build(removedUserEmail))
                        .header("X-Internal-Service", "true")
                .retrieve()
                .onStatus(HttpStatus.NOT_FOUND::equals,response -> {
                    log.error("User is not founded: {}", removedUserEmail);
                    return Mono.error(new UserException("This user does not exist"));
                })
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(errorBody -> {
                                    log.error("Failed to get the item: {}", errorBody);
                                    return Mono.error(new AuctionWebClientException("Service error occurred"));
                                }))
                .bodyToMono(SharedUser.class);
    }


    @Override
    public Mono<AuctionDTO> getAuction(Long auctionId, Long currentUserId){
        String key = "auctions:" + auctionId;

        return auctionRedisTemplate.hasKey(key)
                .flatMap(exists -> exists ? getCacheAuction(auctionId) : auctionRepository.findById(auctionId).map(auctionMapper::toDto))
                .flatMap(auction -> {
                    if(auction.isPublicAccess() || auction.getSellerId().equals(currentUserId)) {
                        return Mono.just(auction);
                    }
                    return canUserSeeAuction(currentUserId, auctionId)
                            .flatMap(access -> {
                                if(access){
                                    return Mono.just(auction);
                                }else {
                                    log.error("User: {} does not have access to this auction", currentUserId);
                                    return Mono.error(new AuctionNotAvailableException("You are not a participant in this auction"));
                                }});
                })
                .switchIfEmpty(Mono.error(new AuctionNotExistException("Auction does not exist")));
    }


    @Override
    public Mono<AuctionDTO> updateAuction(Long currentUserId, Long auctionId, AuctionUpdateDTO auctionUpdateDTO){
        return auctionAvailableToInteraction(auctionId, currentUserId)
                .flatMap(auction -> {
                    if(auction.getStartDate().isBefore(LocalDateTime.now())){
                        return Mono.error(new AuctionStatusException("It is not possible to make changes to an auction that has already started"));
                    }

                    if (auctionUpdateDTO.getStartingPrice() != null) {
                        auction.setStartingPrice(auctionUpdateDTO.getStartingPrice());
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
                            .map(auctionMapper::toDto)
                            .doOnSuccess(savedAuctionDTO -> log.info("Auction successfully saved, auction id: {}", savedAuctionDTO.getAuctionId()))
                            .onErrorMap(ex -> new DatabaseOperationException("Failed to save auction"));
                })
                .onErrorMap(ex -> {
                    log.warn("Error during updating: {}", ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException
                            || ex instanceof AuctionStatusException || ex instanceof DatabaseOperationException) {
                        return ex;
                    }
                    log.warn("Unexpected error: {}", ex.getMessage());
                    return new Exception("Failed to update auction");
                });
    }


    @Override
    public Flux<AuctionDTO> getAllActivePublicAuctions(){
        return auctionRepository.findByFinishedIsFalseAndPublicAccessIsTrue()
                .map(auctionMapper::toDto)
                .onErrorResume(ex -> {
                    log.error("Error occurred while retrieving public auctions: {}", ex.getMessage());
                    return Flux.empty();
                });
    }


    @Override
    public Flux<AuctionDTO> getAllSellersAuctions (Long userId){
        return auctionRepository.findBySellerId(userId)
                .flatMap(auction -> {
                    if(auction.getPublicAccess() || auction.getSellerId().equals(userId)){
                        return Mono.just(auction);
                    }
                    else {
                        return canUserSeeAuction(userId, auction.getAuctionId())
                                .filter(Boolean::booleanValue)
                                .map(access -> auction);
                    }
                })
                .map(auctionMapper::toDto)
                .onErrorResume(ex -> {
                    log.error("Error occurred while retrieving seller's auctions: {}", ex.getMessage());
                    return Flux.empty();
                });
    }


    @Override
    public Flux<AuctionDTO> getAllActiveAuctionsForUser (Long userId){
        return auctionRepository.findByFinishedIsFalse()
                .flatMap(auction -> {
                    if(auction.getPublicAccess() || auction.getSellerId().equals(userId)){
                        return Mono.just(auction);
                    }
                    else {
                        return canUserSeeAuction(userId, auction.getAuctionId())
                                .filter(Boolean::booleanValue)
                                .map(access -> auction);
                    }
                })
                .map(auctionMapper::toDto)
                .onErrorResume(ex -> {
                    log.error("Error occurred while retrieving active auctions: {}", ex.getMessage());
                    return Flux.empty();
                });
    }


    @Override
    public Mono<AuctionDTO> closeAuction(Long auctionId, Long currentUserId){
        return auctionAvailableToInteraction(auctionId, currentUserId)
                .flatMap(auction -> setAuctionFinished(auctionId)
                        .map(auctionMapper::toDto))
                .doOnSuccess(auctionDTO -> log.info("Auction {} successfully closed", auctionDTO.getAuctionId()))
                .onErrorMap(ex -> {
                    log.warn("Failed to close auction with ID {}: {}", auctionId, ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException) {
                        return ex;
                    }
                    return new Exception("Failed to create auction");
                });

    }


    @Override
    public Mono<Void> deleteAuction(Long auctionId, Long currentUserId){
        return auctionAvailableToInteraction(auctionId, currentUserId)
                .flatMap(auction -> closeAuction(auctionId, currentUserId)
                        .then(auctionRepository.delete(auction))
                        .then(auctionKafkaService.sendAuctionRemovedEvent(auction)))
                .onErrorMap(ex -> {
                    log.warn("Failed to delete auction with ID {}: {}", auctionId, ex.getMessage());
                    if (ex instanceof AuctionNotExistException || ex instanceof AuctionNotAvailableException) {
                        return ex;
                    }
                    return new Exception("Failed to delete auction");
                });
    }


    @PostConstruct
    public void scheduleAuctionCaching() {
        disposables.add(
                Flux.interval(Duration.ofHours(1))
                .flatMap(tick -> cacheAuctionsEndingSoon())
                .subscribeOn(Schedulers.boundedElastic())
                        .doOnNext(success -> log.info("Auctions successfully cached"))
                        .doOnError(error -> log.error("Error while caching auctions: {}", error.getMessage()))
                        .subscribe());
    }


    @PostConstruct
    public void scheduleAuctionSetFinished() {
        disposables.add(
                Flux.interval(Duration.ofMinutes(2))
                        .flatMap(tick -> findExpiredAuctions())
                        .subscribeOn(Schedulers.boundedElastic())
                        .doOnNext(success -> log.info("Expired auctions successfully processed"))
                        .doOnError(error -> log.error("Error while processing expired auctions: ", error))
                        .subscribe()
        );
    }


    @PreDestroy
    public void shutdownSchedulers() {
        disposables.dispose();
    }


    private Mono<Void> cacheAuctionsEndingSoon(){
        LocalDateTime nowDate = LocalDateTime.now();
        LocalDateTime oneHourLater = nowDate.plusHours(1);

        return auctionRepository.findByEndDateBetween(nowDate, oneHourLater)
                .filter(auction -> !auction.isFinished())
                .collectList()
                .flatMap(auctions -> {
                    if(auctions.isEmpty()){
                        log.info("No auctions ending soon found.");
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
                    log.error("Error while caching auctions: " + ex.getMessage());
                    return Mono.error(new RedisOperationException("Error while caching auctions"));
                });
    }


    private Mono<AuctionDTO> getCacheAuction (Long auctionId){
        String key = "auctions:" + auctionId;

        return valueOperationsAuction.get(key)
                .retry(3)
                .onErrorResume(ex -> {
                    log.error("Error occurred while checking if auction {} exists in cache: {}", auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException("Failed to get auction in cache"));
                });
    }


    private Mono<Boolean> ifAuctionInCache (Long auctionId){
        String key = "auctions:" + auctionId;
        return auctionRedisTemplate.hasKey(key)
                .retry(3)
                .onErrorResume(ex -> {
                    log.error("Error occurred while checking if auction {} exists in cache: {}", auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException("Failed to find auction in cache"));
                });
    }


    private Mono<Void> findExpiredAuctions(){
        return auctionRedisTemplate.keys("auctions:*")
                .flatMap(key -> valueOperationsAuction.get(key)
                        .switchIfEmpty(Mono.empty())
                )
                .flatMap(valueOperationsAuction::get)
                .filter(auctionDTO -> auctionDTO.getEndDate().isBefore(LocalDateTime.now()))
                .flatMap(auctionDTO -> setAuctionFinished(auctionDTO.getAuctionId()))
                .onErrorContinue((error, obj) -> log.error("Find Error while caching auctions: " + error.getMessage()))
                .then();
    }


    private Mono<Auction> setAuctionFinished(Long auctionId){
        return auctionRepository.findById(auctionId)
                .switchIfEmpty(Mono.error(new AuctionNotExistException("Auction does not exist")))
                .flatMap(auction -> {
                    auction.setFinished(true);

                    return auctionRepository.save(auction)
                            .doOnSuccess(savedAuction -> log.info("Auction {} marked as finished in DB", savedAuction.getAuctionId()))
                            .flatMap(updatedAuction ->
                                    auctionKafkaService.sendAuctionFinishedNotificationEvent(updatedAuction)
                                    .onErrorResume(ex -> {
                                        log.error("Failed to send notification for auction {}", updatedAuction.getAuctionId(), ex);
                                        return Mono.empty();
                                    })
                                    .thenReturn(updatedAuction)
                            )
                            .flatMap(this::handleCacheRemoval);
                });
    }


    private Mono<Auction> handleCacheRemoval(Auction auction){
        return ifAuctionInCache(auction.getAuctionId())
                .flatMap(isInCache -> {
                    if(isInCache){
                        return removeAuctionFromCache(auction.getAuctionId())
                                .flatMap(result -> {
                                    if (result == 0) {
                                        log.error("Failed to remove auction {} from cache", auction.getAuctionId());
                                        return Mono.error(new RedisOperationException("Failed to remove auction from cache"));
                                    }
                                    return Mono.just(auction);
                                });
                    }
                    return Mono.just(auction);
                });
    }


    private Mono<Long> removeAuctionUser(Long removedUserId, Long auctionId){
        String auctionKey = "auction:" + auctionId + ":users";
        return setOperations.remove(auctionKey, String.valueOf(removedUserId))
                .retry(3)
                .onErrorResume(ex -> {
                    log.error("Error occurred while removing user {} from auction {}: {}", removedUserId, auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException("Failed to remove user from auction"));
                });
    }


    private Mono<Long> removeAuctionFromCache(Long auctionId){
        String auctionKey = "auctions:" + auctionId;
        return auctionRedisTemplate.delete(auctionKey)
                .retry(3)
                .onErrorResume(ex -> {
                    log.error("Error occurred while removing auction {} from cache: {}", auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException("Failed to remove auction from cache"));
                });
    }


    private Mono<Boolean> canUserSeeAuction(Long userId, Long auctionId){
        String key = "auction:" + auctionId + ":users";
        return setOperations.isMember(key, String.valueOf(userId))
                .retry(3)
                .onErrorResume(ex -> {
                    log.error("Error occurred while checking if user {} can see auction {}: {}", userId, auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException("Failed to check user"));
                });
    }


    private Mono<Long> saveUserForAuction(Long auctionId, Long userId) {
        String key = "auction:" + auctionId + ":users";
        return setOperations.add(key, String.valueOf(userId))
                .retry(3)
                .onErrorResume(ex -> {
                    log.error("Error occurred while saving user {} to auction {}: {}", userId, auctionId, ex.getMessage());
                    return Mono.error(new RedisOperationException("Failed to save user"));
                });
    }


    private Mono<Auction> auctionAvailableToInteraction(Long auctionId, Long userId){
        return auctionRepository.findById(auctionId)
                .switchIfEmpty(Mono.error(new AuctionNotExistException("Auction does not exist")))
                .filter(auction -> !auction.isFinished() && auction.getSellerId().equals(userId))
                .switchIfEmpty(Mono.error(new AuctionNotAvailableException("Auction is not available")));
    }


    private Mono<Map<String, Long>> resolveUsersId(List<String> emails){
        return Flux.fromIterable(emails)
                .flatMap(email -> webClient
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/users/find_user/{userEmail}")
                                .build(email))
                        .header("X-Internal-Service", "true")
                        .retrieve()
                        .onStatus(HttpStatus.NOT_FOUND::equals,response -> {
                            log.error("User is not founded, user email: {}", email);
                            return Mono.empty();
                        })
                        .onStatus(HttpStatusCode::isError, response ->
                                response.bodyToMono(String.class)
                                        .flatMap(errorBody -> {
                                            log.error("Failed to get the user: {}", errorBody);
                                            return Mono.error(new AuctionWebClientException("Service error occurred"));
                                        }))
                        .bodyToMono(SharedUser.class)
                        .map(user -> Map.entry(email, user.getUserId()))
                        .defaultIfEmpty(Map.entry(email, -1L)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
