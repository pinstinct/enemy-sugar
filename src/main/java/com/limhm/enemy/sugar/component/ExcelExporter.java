package com.limhm.enemy.sugar.component;

import com.limhm.enemy.sugar.domain.CafeDrink;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("CallToPrintStackTrace")
public class ExcelExporter {
    public byte[] generateExcel(Map<String, List<CafeDrink>> menus) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            menus.forEach((sheetName, items) -> createSheet(workbook, sheetName, items));

            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private void createSheet(Workbook workbook, String sheetName, List<CafeDrink> items) {
        Sheet sheet = workbook.createSheet(sheetName);

        Row header = sheet.createRow(0);
        String[] headers = {"이름", "칼로리", "포화지방", "당류", "나트륨", "단백질", "카페인"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
        }

        int rowIndex = 1;
        for (CafeDrink item: items) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(item.getName());
            row.createCell(1).setCellValue(item.getCalories());
            row.createCell(2).setCellValue(item.getSaturatedFat());
            row.createCell(3).setCellValue(item.getSugar());
            row.createCell(4).setCellValue(item.getSodium());
            row.createCell(5).setCellValue(item.getProtein());
            row.createCell(6).setCellValue(item.getCaffeine());
        }
    }
}
