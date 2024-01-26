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
public class CafePaikDaBangFactory implements CafeFactory {

    private static final List<String> URLS = new ArrayList<>(
        List.of("https://paikdabang.com/menu/menu_coffee/",
            "https://paikdabang.com/menu/menu_drink/", "https://paikdabang.com/menu/menu_ccino/"));

    private static final String CAFE_KOR_NAME = "빽다방";

    @Override
    public Flux<Beverage> createBeverage() {
        return Flux.fromIterable(URLS).flatMap(this::fetchItems);
    }

    public Flux<Beverage> fetchItems(String url) {
        return WebClient.create().get().uri(url).retrieve().bodyToMono(String.class)
            .flatMapMany(this::parse);
    }

    private Flux<Beverage> parse(String response) {
        return Flux.defer(() -> {
            List<Beverage> beverages = new ArrayList<>();
            Company cafe = new Cafe(CAFE_KOR_NAME);

            Document document = Jsoup.parse(response);
            Elements items = document.select(".menu_list > ul> li");

            for (Element item : items) {
                String name = item.select(".menu_tit").text();
                String calories = item.select(".ingredient_table li:contains(칼로리) div:nth-child(2)")
                    .text();
                String saturatedFat = item.select(
                        ".ingredient_table li:contains(포화지방) div:nth-child(2)").text()
                    .replace("-", "0");
                String sugar = item.select(".ingredient_table li:contains(당류) div:nth-child(2)")
                    .text().replace("-", "0");
                String sodium = item.select(".ingredient_table li:contains(나트륨) div:nth-child(2)")
                    .text().replace("-", "0");
                String protein = item.select(".ingredient_table li:contains(단백질) div:nth-child(2)")
                    .text().replace("-", "0");
                String caffeine = item.select(".ingredient_table li:contains(카페인) div:nth-child(2)")
                    .text().replace("-", "0");
                Beverage drink = new CafeDrink(cafe, name, calories, sugar, protein, saturatedFat,
                    sodium, caffeine);
                beverages.add(drink);
            }
            return Flux.fromIterable(beverages);
        });
    }
}
