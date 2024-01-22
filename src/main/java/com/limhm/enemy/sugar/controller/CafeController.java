package com.limhm.enemy.sugar.controller;

import com.limhm.enemy.sugar.component.ExcelExporter;
import com.limhm.enemy.sugar.domain.CafeDrink;
import com.limhm.enemy.sugar.factory.CafeMegaCoffeeFactory;
import com.limhm.enemy.sugar.factory.CafeStarbucksFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cafe")
@RequiredArgsConstructor
public class CafeController {

    private final ExcelExporter excelExporter;
    private final CafeStarbucksFactory starbucks;
    private final CafeMegaCoffeeFactory megaCoffee;

    @GetMapping(value = "/menu/down", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<InputStreamResource> downloadMenu() {
        Flux<CafeDrink> starbucksMenu = starbucks.createBeverages();
        Flux<CafeDrink> megaCoffeeMenu = megaCoffee.createBeverages();

        List<CafeDrink> a = starbucksMenu.collectList().block();
        List<CafeDrink> b = megaCoffeeMenu.collectList().block();

        Map<String, List<CafeDrink>> allCafeMenu = Map.of(a.get(0).getCafe().getKorName(), a,
            b.get(0).getCafe().getKorName(), b);

        String[] header = {"이름", "칼로리", "포화지방", "당류", "나트륨", "단백질", "카페인"};
        byte[] excelBytes = excelExporter.generateExcel(allCafeMenu, header);
        InputStreamResource resource = new InputStreamResource(
            new ByteArrayInputStream(excelBytes));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=menu.xlsx");

        return ResponseEntity.ok().headers(headers).contentType(MediaType.APPLICATION_OCTET_STREAM)
            .body(resource);
    }
}
