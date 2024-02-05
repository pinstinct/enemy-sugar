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
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

@Component
public class CafeEdiyaFactory implements CafeFactory {

    private static final String BASE_URL = "https://ediya.com/inc/ajax_brand.php";
    private static final int START_PAGE = 1;
    private static final int END_PAGE = 25;
    private static final String CAFE_KOR_NAME = "이디야";
    private final WebClient webClient;

    public CafeEdiyaFactory(WebClientProvider webClientProvider) {
        this.webClient = webClientProvider.provideWebClient(BASE_URL);
    }

    /**
     * "(52g)" 형식의 문자열에서 숫자만 추출해 반환한다.
     */
    private static String extractNumericValue(String text) {
        String numeric = text.replaceAll("[^\\d.]+", "");
        return numeric.isEmpty() ? "0" : numeric;
    }

    private String buildUrl(Integer page) {
        return UriComponentsBuilder.fromPath("/")
            .queryParam("gubun", "menu_more")
            .queryParam("product_cate", "7")
            .queryParam("chked_val", "")
            .queryParam("skeyword", "")
            .queryParam("page", page)
            .build().toUriString();
    }

    @Override
    public Flux<Beverage> createBeverage() {
        return Flux.range(START_PAGE, END_PAGE).flatMap(page -> fetchItems(buildUrl(page)));
    }

    private Flux<Beverage> fetchItems(String path) {
        return webClient.get().uri(path).retrieve().bodyToMono(String.class)
            .flatMapMany(this::parse)
            .onErrorResume(e -> Flux.error(
                new ConnectionException(BASE_URL + path, e)));
    }

    private Flux<Beverage> parse(String response) {
        Company cafe = new Cafe(CAFE_KOR_NAME);

        try {
            Document document = Jsoup.parse(response);
            Elements items = document.select("li");

            return Flux.fromIterable(items).map(item -> {
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
                return drink;
            });
        } catch (Exception e) {
            return Flux.error(new ParseException(response, e));
        }
    }
}
