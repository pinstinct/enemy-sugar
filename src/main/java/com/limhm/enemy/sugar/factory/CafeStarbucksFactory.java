package com.limhm.enemy.sugar.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limhm.enemy.sugar.domain.CafeFactory;
import com.limhm.enemy.sugar.domain.CafeDrink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CafeStarbucksFactory implements CafeFactory {
    private static final List<String> URLS = new ArrayList<>(List.of(
            "https://www.starbucks.co.kr/upload/json/menu/W0000171.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000060.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000003.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000004.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000005.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000422.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000061.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000075.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000053.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000062.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000471.js"
    ));

    @Autowired
    private final WebClient webClient = WebClient.builder().build();

    @Override
    public List<CafeDrink> createBeverages() {
        return fetchMenus().collectList().block();
    }

    private List<CafeDrink> parse(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        List<CafeDrink> items = new ArrayList<>();

        try {
            // JSON 문자열을 JsonNode로 변환
            JsonNode root = objectMapper.readTree(response);

            for (JsonNode node: root.path("list")) {
                CafeDrink cafeDrink = new CafeDrink(
                        node.path("product_NM").asText(),
                        node.path("kcal").asText(),
                        node.path("sugars").asText(),
                        node.path("protein").asText(),
                        node.path("sat_FAT").asText(),
                        node.path("sodium").asText(),
                        node.path("caffeine").asText()
                );
                items.add(cafeDrink);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return items;
    }

    private Flux<CafeDrink> fetchMenus() {
        List<Mono<List<CafeDrink>>> results = URLS.stream()
                .map(this::fetchMenu)
                .collect(Collectors.toList());
        return Flux.concat(results).flatMapIterable(items -> items);
    }

    private Mono<List<CafeDrink>> fetchMenu(String url) {
        // WebClient 사용하여 비동기로 API 호출 후 결과를 Mono로 감싸어 반환
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .map(this::parse);
    }
}
