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
public class CafeMegaCoffeeFactory implements CafeFactory {

    /**
     * static으로 지정된 객체들을 먼저 메모리에 할당한다.
     */
    private static final String BASE = "https://www.mega-mgccoffee.com/menu/menu.php?menu_category1=1&menu_category2=1&category=&list_checkbox_all=all&page=";
    private static final int START = 1;
    private static final int END = 10;

    private static final List<String> URLS = generateUrl();

    /**
     * final은 한번만 할당하고 수정할 수 없다.
     */
    private static final String CAFE_KOR_NAME = "메가커피";

    private static List<String> generateUrl() {
        List<String> urls = new ArrayList<>();
        for (int i = START; i <= END; i++) {
            String url = BASE + i;
            urls.add(url);
        }
        return urls;
    }

    /**
     * "포화지방 11.1g" 형식의 문자열에서 숫자만 추출해 반환한다.
     */
    private static String extractNumericValue(String text) {
        String numeric = text.replaceAll("[^\\d.]+", "");
        return numeric.isEmpty() ? "0" : numeric;
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
        return Flux.fromIterable(URLS).flatMap(this::fetchItems);
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
    private Flux<Beverage> fetchItems(String url) {
        return WebClient.create().get().uri(url).retrieve().bodyToMono(String.class)
            .flatMapMany(this::parse);
    }

    /**
     * defer(): 값을 지연해 방출한다. just()는 값을 바로 방출한다.
     * <p>
     * selectFirst(String cssQuery): 인수와 일치하는 첫번째 요소를 반환한다.
     * <p>
     * select(String cssQuery): 인수와 일치하는 모든 요소들을 반환한다.
     */
    private Flux<Beverage> parse(String response) {
        return Flux.defer(() -> {
            List<Beverage> beverages = new ArrayList<>();
            Company cafe = new Cafe(CAFE_KOR_NAME);
            try {
                Document document = Jsoup.parse(response);
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
                    Beverage drink = new CafeDrink(cafe, name, calories, sugar, protein,
                        saturatedFat, sodium, caffeine);
                    beverages.add(drink);
                }
            } catch (NullPointerException e) {
                return Flux.error(e);
            }
            return Flux.fromIterable(beverages);
        });
    }
}
