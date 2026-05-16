package com.autoparts.autoparts_system;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import java.io.FileInputStream;

@SpringBootTest
public class ExcelStructureTest {

    @Test
    public void testExcelStructure() throws Exception {
        String filePath = "C:\\Users\\User\\Desktop\\База\\Амортизаторы\\Амортизаторы.xls";

        try (FileInputStream fis = new FileInputStream(filePath);
             Workbook workbook = new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            Row headerRow = sheet.getRow(0);
            Row dataRow = sheet.getRow(1); // Первая строка с данными

            System.out.println("=== СТРУКТУРА EXCEL ===\n");

            if (headerRow != null) {
                System.out.println("ЗАГОЛОВКИ:");
                for (int i = 0; i < 15; i++) {
                    Cell cell = headerRow.getCell(i);
                    if (cell != null) {
                        String value = cell.toString();
                        if (!value.trim().isEmpty()) {
                            System.out.println("  Колонка " + i + ": " + value);
                        }
                    }
                }
            }

            System.out.println("\nПЕРВАЯ СТРОКА ДАННЫХ:");
            if (dataRow != null) {
                for (int i = 0; i < 15; i++) {
                    Cell cell = dataRow.getCell(i);
                    if (cell != null) {
                        String value = cell.toString();
                        if (!value.trim().isEmpty()) {
                            System.out.println("  Колонка " + i + ": " + value);
                        }
                    }
                }
            }
        }
    }
}