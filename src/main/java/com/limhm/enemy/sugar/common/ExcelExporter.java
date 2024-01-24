package com.limhm.enemy.sugar.common;

import com.limhm.enemy.sugar.strategy.ExcelExportable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

/**
 * ExcelExportable 인터페이스를 구현한 클래스의 데이터를 엑셀로 출력한다.
 */
@Component
public class ExcelExporter {

    public <T extends ExcelExportable> byte[] generateExcel(
        Map<String, List<T>> sheetData,
        String[] headers) {
        try (Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            sheetData.forEach(
                (sheetName, items) -> createSheet(workbook, sheetName, items, headers));
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    private <T extends ExcelExportable> void createSheet(Workbook workbook, String sheetName,
        List<T> items, String[] headers) {
        Sheet sheet = workbook.createSheet(sheetName);
        createHeader(sheet, headers);

        int rowIndex = 1;
        for (T item : items) {
            Row row = sheet.createRow(rowIndex++);
            item.writeRow(row);
        }
    }

    private void createHeader(Sheet sheet, String[] headers) {
        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
        }
    }
}
