package com.limhm.enemy.sugar.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limhm.enemy.sugar.config.WebClientProvider;
import com.limhm.enemy.sugar.domain.Beverage;
import com.limhm.enemy.sugar.domain.Cafe;
import com.limhm.enemy.sugar.domain.CafeDrink;
import com.limhm.enemy.sugar.domain.Company;
import com.limhm.enemy.sugar.exception.ConnectionException;
import com.limhm.enemy.sugar.exception.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class CafeStarbucksFactory implements CafeFactory {

    private static final String BASE_URL = "https://www.starbucks.co.kr/upload/json/menu/";

    /**
     * 멤버 변수 타입을 최상위 인터페이스인 제너릭 List로 만들었지만, 비어있는 ArrayList를 할당해 사용
     */
    private static final List<String> PATH = new ArrayList<>(
        List.of("W0000171.js", "W0000060.js", "W0000003.js", "W0000004.js", "W0000005.js",
            "W0000422.js", "W0000061.js", "W0000075.js", "W0000053.js", "W0000062.js",
            "W0000471.js"));
    private static final String CAFE_KOR_NAME = "스타벅스";
    private final WebClient webClient;

    public CafeStarbucksFactory(WebClientProvider webClientProvider) {
        this.webClient = webClientProvider.provideWebClient(BASE_URL);
    }

    @Override
    public Flux<Beverage> createBeverage() {
        return Flux.fromIterable(PATH).flatMap(this::fetchItems);
    }

    private Flux<Beverage> fetchItems(String path) {
        return webClient.get().uri(path).retrieve().bodyToMono(String.class)
            .flatMapMany(this::parse)
            .onErrorResume(e -> Flux.error(new ConnectionException(BASE_URL + path, e)));
    }

    /**
     * readTree(String c): JsonNode로 변환한다.
     * <p>
     * path(): 필드를 읽는다. get()과 차이는 null인 경우 get()은 null을 리턴한다.
     */
    private Flux<Beverage> parse(String response) {
        Company cafe = new Cafe(CAFE_KOR_NAME);
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(response);
            JsonNode items = root.path("list");

            return Flux.fromIterable(items).map(item -> {
                String name = item.path("product_NM").asText();
                String calories = item.path("kcal").asText();
                String sugar = item.path("sugars").asText();
                String protein = item.path("protein").asText();
                String saturatedFat = item.path("sat_FAT").asText();
                String sodium = item.path("sodium").asText();
                String caffeine = item.path("caffeine").asText();
                Beverage drink = new CafeDrink(cafe, name, calories, sugar, protein, saturatedFat,
                    sodium, caffeine);
                return drink;
            });
        } catch (Exception e) {
            return Flux.error(new ParseException(response, e));
        }
    }
}
