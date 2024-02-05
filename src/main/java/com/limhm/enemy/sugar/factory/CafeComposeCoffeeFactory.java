package com.limhm.enemy.sugar.factory;

import com.limhm.enemy.sugar.config.WebClientProvider;
import com.limhm.enemy.sugar.domain.Beverage;
import com.limhm.enemy.sugar.domain.Cafe;
import com.limhm.enemy.sugar.domain.CafeDrink;
import com.limhm.enemy.sugar.domain.Company;
import com.limhm.enemy.sugar.exception.ConnectionException;
import com.limhm.enemy.sugar.exception.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Component
public class CafeComposeCoffeeFactory implements CafeFactory {

    private static final String BASE_URL = "https://composecoffee.com/menu/category";
    private static final int START_PAGE = 185;
    private static final int COUNT = 10;
    private static final String CAFE_KOR_NAME = "컴포즈커피";
    private final WebClient webClient;

    public CafeComposeCoffeeFactory(WebClientProvider webClientProvider) {
        this.webClient = webClientProvider.provideWebClient(BASE_URL);
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

    private String buildUrl(Integer page) {
        return UriComponentsBuilder.fromPath("/").pathSegment(String.valueOf(page)).build()
            .toUriString();
    }

    @Override
    public Flux<Beverage> createBeverage() {
        return Flux.range(START_PAGE, COUNT).flatMap(page -> fetchItems(buildUrl(page)));
    }

    private Flux<Beverage> fetchItems(String path) {
        return webClient.get().uri(path).retrieve().bodyToMono(String.class)
            .flatMapMany(this::parse)
            .onErrorResume(e -> Flux.error(new ConnectionException(BASE_URL + path, e)));
    }

    private Flux<Beverage> parse(String response) {
        Company cafe = new Cafe(CAFE_KOR_NAME);
        try {
            Document document = Jsoup.parse(response);
            Elements items = document.select(".itemBox");

            return Flux.fromIterable(items).map(item -> {
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
                return drink;
            });
        } catch (Exception e) {
            return Flux.error(new ParseException(response, e));
        }
    }
}
