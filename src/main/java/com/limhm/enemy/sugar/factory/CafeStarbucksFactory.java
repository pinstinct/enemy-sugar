package com.limhm.enemy.sugar.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limhm.enemy.sugar.domain.Beverage;
import com.limhm.enemy.sugar.domain.Cafe;
import com.limhm.enemy.sugar.domain.CafeDrink;
import com.limhm.enemy.sugar.domain.Company;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class CafeStarbucksFactory implements CafeFactory {

    /**
     * 멤버 변수 타입을 최상위 인터페이스인 제너릭 List로 만들었지만, 비어있는 ArrayList를 할당해 사용
     */
    private static final List<String> URLS = new ArrayList<>(
        List.of("https://www.starbucks.co.kr/upload/json/menu/W0000171.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000060.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000003.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000004.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000005.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000422.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000061.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000075.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000053.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000062.js",
            "https://www.starbucks.co.kr/upload/json/menu/W0000471.js"));
    private static final String CAFE_KOR_NAME = "스타벅스";

    @Override
    public Flux<Beverage> createBeverage() {
        return Flux.fromIterable(URLS).flatMap(this::fetchItems);
    }

    private Flux<Beverage> fetchItems(String url) {
        return WebClient.create().get().uri(url).retrieve().bodyToMono(String.class)
            .flatMapMany(this::parse);
    }

    /**
     * readTree(String c): JsonNode로 변환한다.
     * <p>
     * path(): 필드를 읽는다. get()과 차이는 null인 경우 get()은 null을 리턴한다.
     */
    private Flux<Beverage> parse(String response) {
        return Flux.defer(() -> {
            List<Beverage> beverages = new ArrayList<>();
            Company cafe = new Cafe(CAFE_KOR_NAME);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(response);

                for (JsonNode node : root.path("list")) {
                    String name = node.path("product_NM").asText();
                    String calories = node.path("kcal").asText();
                    String sugar = node.path("sugars").asText();
                    String protein = node.path("protein").asText();
                    String saturatedFat = node.path("sat_FAT").asText();
                    String sodium = node.path("sodium").asText();
                    String caffeine = node.path("caffeine").asText();
                    Beverage drink = new CafeDrink(cafe, name, calories, sugar, protein,
                        saturatedFat, sodium, caffeine);
                    beverages.add(drink);
                }
            } catch (JsonProcessingException e) {
                return Flux.error(e);
            }
            return Flux.fromIterable(beverages);
        });
    }
}
