package com.maria.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maria.core.entity.AuctionDTO;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class AuctionServiceConfig {
    @Value("${url.item-service}")
    private String itemServiceUrl;
    @Value("${url.user-service}")
    private String userServiceUrl;
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;

    @Bean
    @Qualifier("webClientItem")
    public WebClient webClientItem() {
        return WebClient.builder()
                .baseUrl(itemServiceUrl)
                .build();
    }

    @Bean
    @Qualifier("webClientUser")
    public WebClient webClientUser() {
        return WebClient.builder()
                .baseUrl(userServiceUrl)
                .build();
    }

    @Bean
    @Primary
    public ReactiveRedisTemplate<String, String> stringReactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext()
                .key(StringRedisSerializer.UTF_8)
                .hashKey(StringRedisSerializer.UTF_8)
                .hashValue(StringRedisSerializer.UTF_8)
                .value(StringRedisSerializer.UTF_8)
                .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }

    @Bean
    public ReactiveRedisTemplate<String, AuctionDTO> auctionRedisTemplate(
            ReactiveRedisConnectionFactory factory,
            @Qualifier("redisObjectMapper") ObjectMapper redisObjectMapper) {
        Jackson2JsonRedisSerializer<AuctionDTO> serializer = new Jackson2JsonRedisSerializer<>(redisObjectMapper, AuctionDTO.class);

        RedisSerializationContext<String, AuctionDTO> serializationContext = RedisSerializationContext
                .<String, AuctionDTO>newSerializationContext()
                .key(StringRedisSerializer.UTF_8)
                .hashKey(StringRedisSerializer.UTF_8)
                .value(serializer)
                .hashValue(serializer)
                .build();

        return new ReactiveRedisTemplate<>(factory, serializationContext);
    }

    @Bean
    @Qualifier("redisObjectMapper")
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .activateDefaultTyping(
                        LaissezFaireSubTypeValidator.instance,
                        ObjectMapper.DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                );
    }

    @Bean
    @Primary
    public ObjectMapper restObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Bean
    public ReactiveValueOperations<String, AuctionDTO> reactiveValueOperationsAuction(ReactiveRedisTemplate<String, AuctionDTO> redisTemplate) {
        return redisTemplate.opsForValue();
    }

    @Bean
    public ReactiveSetOperations<String, String> reactiveSetOperationsString(ReactiveRedisTemplate<String, String> redisTemplate) {
        return redisTemplate.opsForSet();
    }

    @Bean
    @Primary
    public ReactiveRedisConnectionFactory auctionReactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(redisHost, redisPort);
    }
}
