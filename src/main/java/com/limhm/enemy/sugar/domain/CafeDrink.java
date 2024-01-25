package com.limhm.enemy.sugar.domain;

import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;

@Getter
@Setter
public class CafeDrink implements Beverage {

    private Company company;
    private String name;
    private double calories;
    private double sugar;
    private double protein;
    private double saturatedFat;
    private double sodium;
    private double caffeine;

    public CafeDrink(Company company, String name, String calories, String sugar, String protein,
        String saturatedFat, String sodium, String caffeine) {
        this.company = company;
        this.name = name;
        this.calories = parseDouble(calories);
        this.sugar = parseDouble(sugar);
        this.protein = parseDouble(protein);
        this.saturatedFat = parseDouble(saturatedFat);
        this.sodium = parseDouble(sodium);
        this.caffeine = parseDouble(caffeine);
    }

    /**
     * Optional 클래스: (Wrapper class) NullPointerException 간단히 회피하기 위해 사용한다. of() 혹은 ofNullable()
     * 메소드를 사용해 Optional 객체를 생성한다.
     * <p>
     * map(Function mapper): 인자로 넘긴 함수를 적용한 값(Optional)을 반환한다.
     * <p>
     * 이중 콜론 연산자(Double Colon Operator, ::): value -> Double.parseDouble(value) =
     * Double.parseDouble(value)
     */
    private double parseDouble(String str) {
        return Optional.ofNullable(str).map(Double::parseDouble).orElse(0.0);
    }

    @Override
    public void writeRow(Row row) {
        CellStyle numericStyle = row.getSheet().getWorkbook().createCellStyle();
        numericStyle.setDataFormat(
            row.getSheet().getWorkbook().getCreationHelper().createDataFormat().getFormat("0.0"));
        createCell(row, 0, getName());
        createCellByStyle(row, 1, getCalories(), numericStyle);
        createCellByStyle(row, 2, getSaturatedFat(), numericStyle);
        createCellByStyle(row, 3, getSugar(), numericStyle);
        createCellByStyle(row, 4, getSodium(), numericStyle);
        createCellByStyle(row, 5, getProtein(), numericStyle);
        createCellByStyle(row, 6, getCaffeine(), numericStyle);
    }
}
