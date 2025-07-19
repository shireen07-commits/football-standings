package com.football.standings.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.time.Duration;

/**
 * Configuration class for Football Standings Application
 * Implements Factory Pattern for creating configuration beans
 */
@Configuration
public class FootballConfig {
    
	@Value("${football.api.base-url}")
	private String apiBaseUrl;
    @Value("${football.api.timeout}")
    private int timeout;
    
    @Value("${football.offline.cache-duration}")
    private long cacheDuration;
    
    /**
     * Factory method to create WebClient bean for API calls
     * Implements Builder Pattern for WebClient configuration
     */
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(apiBaseUrl)
                .build();
    }
    
    /**
     * Factory method to create CacheManager with Caffeine implementation
     * Implements Strategy Pattern for caching strategy
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(Duration.ofSeconds(cacheDuration))
                .maximumSize(1000));
        return cacheManager;
    }
}