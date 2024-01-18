package com.limhm.enemy.sugar.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    /*한 번만 생성하고, 여러 곳에서 재사용합니다.*/
    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
