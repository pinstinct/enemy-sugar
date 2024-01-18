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

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/call")
@RequiredArgsConstructor
public class ApiCallTest {

    private final ExcelExporter excelExporter;

    @GetMapping
    ResponseEntity<InputStreamResource> getBody() {
        CafeStarbucksFactory starbucks = new CafeStarbucksFactory();
        List<CafeDrink> starbucksMenu = starbucks.createBeverages();

        CafeMegaCoffeeFactory megaCoffee = new CafeMegaCoffeeFactory();
        List<CafeDrink> megaCoffeeMenu = megaCoffee.createBeverages();

        Map<String, List<CafeDrink>> allCafeMenu = Map.of(
                "스타벅스", starbucksMenu,
                "메가커피", megaCoffeeMenu
        );

        byte[] excelBytes = excelExporter.generateExcel(allCafeMenu);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(excelBytes));
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=menu.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
