package com.limhm.enemy.sugar.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Optional;

@Getter
@Setter
@ToString
public class CafeDrink implements Beverage {
    private String name;
    private double calories;
    private double sugar;
    private double protein;
    private double saturatedFat;
    private double sodium;
    private double caffeine;

    public CafeDrink(String name, String calories, String sugar, String protein,
                     String saturatedFat, String sodium, String caffeine) {
        this.name = name;
        this.calories = parseDouble(calories);
        this.sugar = parseDouble(sugar);
        this.protein = parseDouble(protein);
        this.saturatedFat = parseDouble(saturatedFat);
        this.sodium = parseDouble(sodium);
        this.caffeine = parseDouble(caffeine);
    }

    private double parseDouble(String str) {
        return Optional.ofNullable(str)
                .map(Double::parseDouble)
                .orElse(0.0);
    }
}
