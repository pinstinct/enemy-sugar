package com.limhm.enemy.sugar.domain;

import com.limhm.enemy.sugar.templatemethod.ExcelExportable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

import java.util.Optional;

@Getter
@Setter
@ToString
public class CafeDrink implements Beverage, ExcelExportable {

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

    @Override
    public void writeRow(Row row) {
        CellStyle numericStyle = row.getSheet().getWorkbook().createCellStyle();
        numericStyle.setDataFormat(
            row.getSheet().getWorkbook().getCreationHelper().createDataFormat().getFormat("0.0")
        );
        createCell(row, 0, getName());
        createCellByStyle(row, 1, getCalories(), numericStyle);
        createCellByStyle(row, 2, getSaturatedFat(), numericStyle);
        createCellByStyle(row, 3, getSugar(), numericStyle);
        createCellByStyle(row, 4, getSodium(), numericStyle);
        createCellByStyle(row, 5, getProtein(), numericStyle);
        createCellByStyle(row, 6, getCaffeine(), numericStyle);
    }
}
