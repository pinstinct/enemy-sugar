package com.limhm.enemy.sugar.config;

import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class WebClientProvider {

    /**
     * 오버로딩(Overloading)
     */
    public static WebClient provideWebClient(String baseUrl) {
        return provideWebClient(baseUrl, "");
    }

    /**
     * static으로 지정된 객체들을 먼저 메모리에 할당한다.
     */
    public static WebClient provideWebClient(String baseUrl, String headerType) {
        WebClient.Builder builder = WebClient.builder().baseUrl(baseUrl);
        if (!headerType.isEmpty()) {
            builder.defaultHeader(HttpHeaders.CONTENT_TYPE, headerType);
        }
        return builder.build();
    }
}
