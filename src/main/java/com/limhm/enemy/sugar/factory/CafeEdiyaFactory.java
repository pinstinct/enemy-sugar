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
public class CafeEdiyaFactory implements CafeFactory {

    private static final String BASE = "https://ediya.com/inc/ajax_brand.php?gubun=menu_more&product_cate=7&chked_val=&skeyword=&page=";
    private static final int START = 1;
    private static final int END = 25;
    private static final List<String> URLS = generateUrl();
    private static final String CAFE_KOR_NAME = "이디야";

    private static List<String> generateUrl() {
        List<String> urls = new ArrayList<>();
        for (int i = START; i <= END; i++) {
            String url = BASE + i;
            urls.add(url);
        }
        return urls;
    }

    /**
     * "(52g)" 형식의 문자열에서 숫자만 추출해 반환한다.
     */
    private static String extractNumericValue(String text) {
        String numeric = text.replaceAll("[^\\d.]+", "");
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
            Elements items = document.select("li");

            for (Element item : items) {
                Element h2 = item.select("h2").first();
                String name = h2.ownText().trim();
                String calories = extractNumericValue(
                    item.select("dl dt:contains(칼로리) + dd").text());
                String saturatedFat = extractNumericValue(
                    item.select("dl dt:containsOwn(포화지방) + dd").text());
                String sugar = extractNumericValue(
                    item.select("dl dt:containsOwn(당류) + dd").text());
                String sodium = extractNumericValue(
                    item.select("dl dt:containsOwn(나트륨) + dd").text());
                String protein = extractNumericValue(
                    item.select("dl dt:containsOwn(단백질) + dd").text());
                String caffeine = extractNumericValue(
                    item.select("dl dt:containsOwn(카페인) + dd").text());
                Beverage drink = new CafeDrink(cafe, name, calories, sugar, protein, saturatedFat,
                    sodium, caffeine);
                beverages.add(drink);
            }
            return Flux.fromIterable(beverages);
        });
    }
}
