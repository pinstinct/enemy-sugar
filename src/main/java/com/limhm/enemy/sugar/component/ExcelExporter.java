package com.limhm.enemy.sugar.component;

import com.limhm.enemy.sugar.templatemethod.ExcelExportable;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
@SuppressWarnings("CallToPrintStackTrace")
public class ExcelExporter {
    public <T extends ExcelExportable> byte[] generateExcel(Map<String, List<T>> menus, String[] headers) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()){
            menus.forEach((sheetName, items) -> createSheet(workbook, sheetName, items, headers));
            workbook.write(outputStream);
            return outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }

    private <T extends ExcelExportable> void createSheet(Workbook workbook, String sheetName, List<T> items, String[] headers) {
        Sheet sheet = workbook.createSheet(sheetName);
        createHeader(sheet, headers);

        int rowIndex = 1;
        for (T item: items) {
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
