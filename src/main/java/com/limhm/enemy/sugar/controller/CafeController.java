package com.limhm.enemy.sugar.controller;

import com.limhm.enemy.sugar.common.ExcelExporter;
import com.limhm.enemy.sugar.domain.Beverage;
import com.limhm.enemy.sugar.factory.CafeComposeCoffeeFactory;
import com.limhm.enemy.sugar.factory.CafeEdiyaFactory;
import com.limhm.enemy.sugar.factory.CafeMegaCoffeeFactory;
import com.limhm.enemy.sugar.factory.CafePaikDaBangFactory;
import com.limhm.enemy.sugar.factory.CafeStarbucksFactory;
import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * RestController = @Controller + @ResponseBody
 */
@RestController
@RequestMapping("/cafe")
@RequiredArgsConstructor
public class CafeController {

    private final ExcelExporter excelExporter;
    private final CafeStarbucksFactory starbucks;
    private final CafeMegaCoffeeFactory megaCoffee;
    private final CafeComposeCoffeeFactory composeCoffee;
    private final CafePaikDaBangFactory paikDaBang;
    private final CafeEdiyaFactory ediya;

    /**
     * collectList(): Flux에서 넘어오는 항목들을 하나의 리스트로 모은 Mono로 변환(Mono<List<T>>)한다.
     * <p>
     * merge(): 퍼블리셔의 순서와 상관없이 데이터가 발생할 때마다 Flux로 전달한다. concat()은 퍼블리셔의 순서대로 Flux로 전달한다.
     * <p>
     * collectMap(): Flux에서 방출되는 모든 요소들을 Map에 담아 Mono로 반환(Mono<Map<K, V>>)한다.
     * <p>
     * map(Function mapper): Mono에서 방출된 값을 인수 함수를 적용해 동기방식으로 변형한다. flatMap()과 비동기로 방출된 값을 변형한다.
     */
    @GetMapping(value = "/menu/down", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public Mono<ResponseEntity<InputStreamResource>> downloadMenu() {
        List<Mono<List<Beverage>>> allCafeMenuMono = Arrays.asList(
            starbucks.createBeverage().collectList(), megaCoffee.createBeverage().collectList(),
            composeCoffee.createBeverage().collectList(),
            paikDaBang.createBeverage().collectList(), ediya.createBeverage().collectList());

        return Flux.merge(allCafeMenuMono)
            .collectMap(menu -> menu.get(0).getCompany().getKorName())
            .map(allCafeMenu -> {
                String[] header = {"이름", "칼로리", "포화지방", "당류", "나트륨", "단백질", "카페인"};
                byte[] excelBytes = excelExporter.generateExcel(allCafeMenu, header);
                InputStreamResource resource = new InputStreamResource(
                    new ByteArrayInputStream(excelBytes));
                HttpHeaders headers = new HttpHeaders();
                headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=menu.xlsx");

                return ResponseEntity.ok().headers(headers)
                    .contentType(MediaType.APPLICATION_OCTET_STREAM).body(resource);
            });
    }
}
