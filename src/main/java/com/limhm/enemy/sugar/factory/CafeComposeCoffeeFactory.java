package com.limhm.enemy.sugar.factory;

import com.limhm.enemy.sugar.domain.Beverage;
import com.limhm.enemy.sugar.domain.Cafe;
import com.limhm.enemy.sugar.domain.CafeDrink;
import com.limhm.enemy.sugar.domain.Company;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class CafeComposeCoffeeFactory implements CafeFactory {

    private static final String BASE = "https://composecoffee.com/menu/category/";
    private static final int START = 185;
    private static final int END = 193;
    private static final List<String> URLS = generateUrl();
    private static final String CAFE_KOR_NAME = "컴포즈커피";

    private static List<String> generateUrl() {
        List<String> urls = new ArrayList<>();
        for (int i = START; i <= END; i++) {
            String url = BASE + i;
            urls.add(url);
        }
        return urls;
    }

    /**
     * 메뉴제공 : 2shot : 156mg/45ml
     * <p>
     * 카페인 - 4shot : 312mg/90ml
     * <p>
     * 카페인(mg) : 85
     * <p>
     * 세가지 형식의 문자열에서 숫자만 추출해서 반환한다.
     */
    private static String extractNumericValue(String text) {
        String[] extractMl = text.split("/");
        String[] parts = extractMl[0].split(":");
        String numeric = parts[parts.length - 1].replaceAll("[^\\d.]+", "");
        return numeric.isEmpty() ? "0" : numeric;
    }

    @Override
    public Flux<Beverage> createBeverage() {
        return Flux.fromIterable(URLS).flatMap(this::fetchItems);
    }

    private Flux<Beverage> fetchItems(String url) {
        return WebClient.create().get().uri(url).retrieve().bodyToMono(String.class)
            .flatMapMany(this::parse);
    }

    private Flux<Beverage> parse(String response) {
        return Flux.defer(() -> {
            List<Beverage> beverages = new ArrayList<>();
            Company cafe = new Cafe(CAFE_KOR_NAME);
            Document document = Jsoup.parse(response);
            Elements items = document.select(".itemBox");

            for (Element item : items) {
                String name = item.select(".title").text();
                String calories = extractNumericValue(
                    item.select(".info li.extra:contains(열량)").text());
                String sugar = extractNumericValue(
                    item.select(".info li.extra:contains(당류)").text());
                String protein = extractNumericValue(
                    item.select(".info li.extra:contains(단백질)").text());
                String saturatedFat = extractNumericValue(
                    item.select(".info li.extra:contains(포화지방)").text());
                String sodium = extractNumericValue(
                    item.select(".info li.extra:contains(나트륨)").text());
                String caffeine = extractNumericValue(
                    item.select(".info li.extra:contains(카페인)").text());
                Beverage drink = new CafeDrink(cafe, name, calories, sugar, protein, saturatedFat,
                    sodium, caffeine);
                beverages.add(drink);
            }
            return Flux.fromIterable(beverages);
        });
    }
}
