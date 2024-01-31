package com.limhm.enemy.sugar.factory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.limhm.enemy.sugar.domain.Beverage;
import com.limhm.enemy.sugar.domain.Cafe;
import com.limhm.enemy.sugar.domain.CafeDrink;
import com.limhm.enemy.sugar.domain.CafeTwosomeRequestBody;
import com.limhm.enemy.sugar.domain.Company;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

@Component
public class CafeTwosomePlaceCoffeeFactory implements CafeFactory {

    private static final String BASE_URL = "https://mo.twosome.co.kr/mn";
    private static final int START_PAGE = 1;
    private static final int END_PAGE = 5;
    private static final List<String> MID_CDS = new ArrayList<>(List.of("01", "02", "03"));
    private static final String CAFE_KOR_NAME = "투썸플레이스";

    private final WebClient webClient;

    public CafeTwosomePlaceCoffeeFactory() {
        this.webClient = WebClient.builder().baseUrl(BASE_URL)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
            .build();
    }

    /**
     * "355ml" 형식의 문자열에서 숫자만 추출해 반환한다.
     */
    private static String extractNumericValue(String text) {
        String numeric = text.replaceAll("[^\\d.]+", "");
        return numeric.isEmpty() ? "0" : numeric;
    }

    private static String getNodeValue(JsonNode rootNode, String infoTitle) {
        for (JsonNode node : rootNode) {
            String title = node.path("ADD_INFO_TITLE").asText();
            if (infoTitle.equals(title)) {
                String[] values = node.path("MENU_CNTNT").asText().split("/");
                return extractNumericValue(values[0]);
            }
        }
        return "0";
    }

    private MultiValueMap<String, String> createRequestBodyForMenuInfoList(int page, String midCd) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("pageNum", String.valueOf(page));
        parameters.add("grtCd", "1");
        parameters.add("midCd", midCd);
        return parameters;
    }

    @Override
    public Flux<Beverage> createBeverage() {
        return Flux.range(START_PAGE, END_PAGE)
            .flatMap(page -> Flux.fromIterable(MID_CDS).flatMap(midCd -> fetchItems(page, midCd)))
            .flatMap(this::fetchTemperatureOptions).flatMap(this::fetchSizeOptions)
            .flatMap(this::fetchInfo);
    }

    private Flux<CafeTwosomeRequestBody> fetchItems(int page, String midCd) {
        MultiValueMap<String, String> parameters = createRequestBodyForMenuInfoList(page, midCd);
        return webClient.post().uri("/menuInfoListAjax.json")
            .bodyValue(parameters).retrieve().bodyToMono(String.class)
            .flatMapMany(this::parseMenuNameAndCode);
    }

    private Flux<CafeTwosomeRequestBody> fetchTemperatureOptions(CafeTwosomeRequestBody request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("menuCd", request.getMenuCode());
        formData.add("grtCd", "1");
        return webClient.post().uri("/menuAddInfoListAjax.json").bodyValue(formData).retrieve()
            .bodyToMono(String.class)
            .flatMapMany(response -> parseTemperatureOptions(response, request));
    }

    private Flux<CafeTwosomeRequestBody> fetchSizeOptions(CafeTwosomeRequestBody request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("menuCd", request.getMenuCode());
        formData.add("ondoOpt", request.getTemperatureOption());
        return webClient.post().uri("/menuSizeOptListAjax.json").bodyValue(formData).retrieve()
            .bodyToMono(String.class).flatMapMany(response -> parseSizeOptions(response, request));
    }

    private Flux<Beverage> fetchInfo(CafeTwosomeRequestBody request) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("menuCd", request.getMenuCode());
        formData.add("ondoOpt", request.getTemperatureOption());
        formData.add("sizeOpt", request.getSizeOption());
        return webClient.post().uri("/menuAddInfoCntnListAjax.json").bodyValue(formData).retrieve()
            .bodyToMono(String.class).flatMapMany(response -> parseInfo(response, request));
    }

    private Flux<CafeTwosomeRequestBody> parseMenuNameAndCode(String response) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            JsonNode fetchResult = jsonNode.get("fetchResultListSet");

            if (fetchResult.isArray()) {
                return Flux.fromIterable(fetchResult).map(node -> {
                    String menuName = node.get("MENU_NM").asText();
                    String menuCode = node.get("MENU_CD").asText();
                    return CafeTwosomeRequestBody.builder().menuName(menuName).menuCode(menuCode)
                        .build();
                });
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Flux.error(e);
        }
        return Flux.empty();
    }

    private Flux<CafeTwosomeRequestBody> parseTemperatureOptions(String response,
        CafeTwosomeRequestBody request) {
        Document document = Jsoup.parse(response);
        Elements items = document.select("div.tabs.__layout2 ul li");

        return Flux.fromIterable(items).map(item -> {
            String temperatureOption = item.select("a").attr("data-code");
            String temperature = item.select("a").text();
            String menuName = "(" + temperature + ") " + request.getMenuName();

            CafeTwosomeRequestBody newRequest = CafeTwosomeRequestBody.builder()
                .menuCode(request.getMenuCode()).menuName(menuName)
                .temperatureOption(temperatureOption).build();
            return newRequest;
        });
    }

    private Flux<CafeTwosomeRequestBody> parseSizeOptions(String response,
        CafeTwosomeRequestBody request) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);

            if (jsonNode.isArray()) {
                return Flux.fromIterable(jsonNode).map(node -> {
                    String sizeOption = node.get("OPTS").asText();
                    String sizeOptionName = node.get("SIZE_OPT_NM").asText();
                    String menuName = request.getMenuName() + " (" + sizeOptionName + ")";

                    CafeTwosomeRequestBody newRequest = CafeTwosomeRequestBody.builder()
                        .menuCode(request.getMenuCode())
                        .temperatureOption(request.getTemperatureOption()).menuName(menuName)
                        .sizeOption(sizeOption).build();
                    return newRequest;
                });
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Flux.error(e);
        }
        return Flux.empty();
    }

    /**
     * just() 사용
     */
    private Flux<Beverage> parseInfo(String response, CafeTwosomeRequestBody request) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = objectMapper.readTree(response);
            Company cafe = new Cafe(CAFE_KOR_NAME);
            String menuName = request.getMenuName();
            if (jsonNode.isArray()) {
                String calories = getNodeValue(jsonNode, "열량(Kcal)");
                String sugar = getNodeValue(jsonNode, "당류(g/%)");
                String protein = getNodeValue(jsonNode, "단백질(g/%)");
                String saturatedFat = getNodeValue(jsonNode, "포화지방(g/%)");
                String sodium = getNodeValue(jsonNode, "나트륨(mg/%)");
                String caffeine = getNodeValue(jsonNode, "카페인(mg/%)");
                CafeDrink drink = new CafeDrink(cafe, menuName, calories, sugar, protein,
                    saturatedFat, sodium, caffeine);
                return Flux.just(drink);
            }
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return Flux.error(e);
        }
        return Flux.empty();
    }
}
