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
public class CafeMegaCoffeeFactory implements CafeFactory {

    /**
     * final은 한번만 할당하고 수정할 수 없다.
     */
    private final String BASE_URL = "https://www.mega-mgccoffee.com/menu/menu.php";
    private final int START_PAGE = 1;
    private final int END_PAGE = 10;
    private final String CAFE_KOR_NAME = "메가커피";
    private final WebClient webClient;

    public CafeMegaCoffeeFactory(WebClientProvider webClientProvider) {
        this.webClient = webClientProvider.provideWebClient(BASE_URL);
    }

    /**
     * "포화지방 11.1g" 형식의 문자열에서 숫자만 추출해 반환한다.
     */
    private String extractNumericValue(String text) {
        String numeric = text.replaceAll("[^\\d.]+", "");
        return numeric.isEmpty() ? "0" : numeric;
    }

    private String buildUrl(Integer page) {
        return UriComponentsBuilder.fromPath("/")
            .queryParam("menu_category1", "1")
            .queryParam("menu_category2", "1")
            .queryParam("category", "")
            .queryParam("list_checkbox_all", "all")
            .queryParam("page", page)
            .build().toUriString();
    }

    /**
     * fromIterable(Iterable it): Iterable 인자를 넘기면 Iterable을 Flux로 변환
     * <p>
     * flatMap, flatMapSequential, concatMap: 값을 꺼내 새로운 퍼블리셔로 바꿔주는 연산자이다. flatMap은 비동기로 동작할 때 순서를
     * 보장하지 않으므로, 순서를 보장하려면 flatMapSequential 또는 concatMap을 사용한다. flatMapSequential과 concatMap의 차이는
     * concatMap은 퍼블리셔의 스트림이 다 끝난 후에 그 다음 넘어오는 값의 퍼블리셔 스트림을 처리한다. flatMapSequential은 일단 오는대로 구독하고
     * 결과를 순서에 맞게 리턴한다.
     */
    @Override
    public Flux<Beverage> createBeverage() {
        return Flux.range(START_PAGE, END_PAGE).flatMap(page -> fetchItems(buildUrl(page)));
    }

    /**
     * WebClient: 리액터 기반의 비동기 HTTP 요청
     * <p>
     * create(): 정적 팩토리 메서드를 이용해 WebClient를 생성한다.
     * <p>
     * bodyToMono(): 응답 body를 Mono 객체로 반환한다.
     * <p>
     * flatMapMany(): Mono에서 Flux로 변환한다.
     */
    private Flux<Beverage> fetchItems(String path) {
        return webClient.get().uri(path).retrieve().bodyToMono(String.class)
            .flatMapMany(this::parse)
            .onErrorResume(e -> Flux.error(new ConnectionException(BASE_URL + path, e)));
    }

    /**
     * defer(): 값을 지연해 방출한다. just()는 값을 바로 방출한다.
     * <p>
     * selectFirst(String cssQuery): 인수와 일치하는 첫번째 요소를 반환한다.
     * <p>
     * select(String cssQuery): 인수와 일치하는 모든 요소들을 반환한다.
     */
    private Flux<Beverage> parse(String response) {
        Company cafe = new Cafe(CAFE_KOR_NAME);
        try {
            Document document = Jsoup.parse(response);
            Element menu = document.selectFirst("#menu_list");
            Elements items = menu.select("> li");

            return Flux.fromIterable(items).map(item -> {
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
                Beverage drink = new CafeDrink(cafe, name, calories, sugar, protein,
                    saturatedFat, sodium, caffeine);
                return drink;
            });
        } catch (Exception e) {
            return Flux.error(new ParseException(response, e));
        }
    }
}
