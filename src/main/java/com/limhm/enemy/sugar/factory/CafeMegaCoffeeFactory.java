package com.limhm.enemy.sugar.factory;

import com.limhm.enemy.sugar.domain.CafeFactory;
import com.limhm.enemy.sugar.domain.CafeDrink;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CafeMegaCoffeeFactory implements CafeFactory {

    @Override
    public Flux<CafeDrink> createBeverages() {
        String base = "https://www.mega-mgccoffee.com/menu/menu.php?menu_category1=1&menu_category2=1&category=&list_checkbox_all=all&page=";
        return Flux.fromIterable(generateUrl(base, 1, 10))
            .flatMap(this::fetchItems);
    }

    private static List<String> generateUrl(String base, int start, int end) {
        List<String> urls = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String url = base + i;
            urls.add(url);
        }
        return urls;
    }

    private Flux<CafeDrink> fetchItems(String url) {
        return WebClient.create().get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .flatMapMany(this::parse);
    }

    private Flux<CafeDrink> parse(String response) {
        return Flux.defer(() -> {
            List<CafeDrink> beverages = new ArrayList<>();
            try {
                Document document = Jsoup.parse(response);
                Objects.requireNonNull(document);

                Element menu = document.selectFirst("#menu_list");
                Elements items = menu.select("> li");

                for (Element item : items) {
                    String name = item.select(".cont_text_title b").first().text();
                    String calories = extractNumericValue(
                        item.select(".cont_text_inner:contains(1회 제공량)").text()
                            .replace("1회 제공량", ""));
                    String saturatedFat = extractNumericValue(
                        item.select(".cont_list li:contains(포화지방)").text());
                    String sugar = extractNumericValue(
                        item.select(".cont_list li:contains(당류)").text());
                    String sodium = extractNumericValue(
                        item.select(".cont_list li:contains(나트륨)").text());
                    String protein = extractNumericValue(
                        item.select(".cont_list li:contains(단백질)").text());
                    String caffeine = extractNumericValue(
                        item.select(".cont_list li:contains(카페인)").text());
                    CafeDrink drink = new CafeDrink(name, calories, sugar, protein, saturatedFat,
                        sodium, caffeine);
                    beverages.add(drink);
                }
            } catch (NullPointerException e) {
//                e.printStackTrace();
                return Flux.error(e);
            }
            return Flux.fromIterable(beverages);
        });
    }

    private static String extractNumericValue(String text) {
        String numeric = text.replaceAll("[^\\d.]+", "");
        return numeric.isEmpty() ? "0" : numeric;
    }
}
