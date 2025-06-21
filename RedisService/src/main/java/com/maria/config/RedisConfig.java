package com.maria.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.maria.dto.AuctionDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveSetOperations;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.data.redis.serializer.*;

@Configuration
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String redisHost;
    @Value("${spring.redis.port}")
    private int redisPort;

    /*
    @Bean
    public ReactiveRedisConnectionFactory reactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory("redis", 6379);
    }

     */

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
    public ReactiveRedisTemplate<String, AuctionDTO> auctionRedisTemplate(ReactiveRedisConnectionFactory factory, ObjectMapper objectMapper) {
        Jackson2JsonRedisSerializer<AuctionDTO> serializer = new Jackson2JsonRedisSerializer<>(objectMapper, AuctionDTO.class);

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
    public ObjectMapper objectMapper() {
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
}
