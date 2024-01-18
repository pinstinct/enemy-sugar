package com.limhm.enemy.sugar.factory;

import com.limhm.enemy.sugar.domain.CafeFactory;
import com.limhm.enemy.sugar.domain.CafeDrink;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CafeMegaCoffeeFactory implements CafeFactory {

    @Override
    public List<CafeDrink> createBeverages() {
        List<CafeDrink> beverages = new ArrayList<>();
        try {
            String base = "https://www.mega-mgccoffee.com/menu/menu.php?menu_category1=1&menu_category2=1&category=&list_checkbox_all=all&page=";
            List<String> urls = generateUrl(base, 1, 10);

            for (String url: urls) {
                Document document = Jsoup.connect(url).get();
                Objects.requireNonNull(document);

                Element menu = document.selectFirst("#menu_list");
                Elements items = menu.select("> li");


                for (Element item : items) {
                    String name = item.select(".cont_text_title b").text();
                    String calories = extractNumericValue(item.select(".cont_text_inner:contains(1회 제공량)").text());
                    String saturatedFat = extractNumericValue(item.select(".cont_list li:contains(포화지방)").text());
                    String sugar = extractNumericValue(item.select(".cont_list li:contains(당류)").text());
                    String sodium = extractNumericValue(item.select(".cont_list li:contains(나트륨)").text());
                    String protein = extractNumericValue(item.select(".cont_list li:contains(단백질)").text());
                    String caffeine = extractNumericValue(item.select(".cont_list li:contains(카페인)").text());
                    CafeDrink food = new CafeDrink(name, calories, sugar, protein, saturatedFat, sodium, caffeine);
                    beverages.add(food);
                }
            }
        } catch (IOException | NullPointerException e) {
            e.printStackTrace();
        }
        return beverages;
    }

    private static List<String> generateUrl(String base, int start, int end) {
        List<String> urls = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            String url = base + i;
            urls.add(url);
        }
        return urls;
    }

    private static String extractNumericValue(String text) {
        String numeric = text.replaceAll("[^\\d.]+", "");
        return numeric.isEmpty() ? "0": numeric;
    }
}
