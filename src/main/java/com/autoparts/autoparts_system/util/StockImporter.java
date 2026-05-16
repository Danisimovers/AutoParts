package com.autoparts.autoparts_system.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StockImporter {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Integer MAIN_WAREHOUSE_ID = 1;

    // Наценка для расчета цены продажи (себестоимость + 1000 руб)
    private static final BigDecimal PRICE_MARKUP = new BigDecimal("1000");

    private static final Set<String> SKIP_KEYWORDS = new HashSet<>(Arrays.asList(
            "Характеристика", "Номенклатура", "Место хранения", "Свободный остаток",
            "Себестоимость", "Основной склад", "ИТОГО", "Всего", "Страница",
            "Номенклатура 1С"
    ));

    // Бренды и служебные слова, которые не могут быть артикулами
    private static final Set<String> SKIP_WORDS = new HashSet<>(Arrays.asList(
            "VOLVO", "SCANIA", "MAN", "DAF", "RENAULT", "IVECO", "MERCEDES", "MERCEDES-BENZ",
            "KAMAZ", "MAZ", "КАМАЗ", "МАЗ", "DONGFENG", "HOWO", "SITRAK", "FAW", "FOTON",
            "CATERPILLAR", "CUMMINS", "DEUTZ", "PERKINS", "BOSCH", "ZF", "WABCO", "KNORR",
            "TOPCOVER", "SAMPA", "ROSTAR", "SACHS", "AUGER", "FEBI", "PAAZ", "SABO",
            "MONROE", "MARSHALL", "STELLOX", "BPW", "S&K", "GMBH", "SIMPECO", "AIRKRAFT",
            "TRIALLI", "FENOX", "LUCKTECH", "KMZ", "MARSHAL", "SONDER", "SE-M", "PE", "HD-PARTS"
    ));

    public void importStockFromExcel(String filePath) throws Exception {
        importStockFromExcel(filePath, MAIN_WAREHOUSE_ID);
    }

    public void importStockFromExcel(String filePath, Integer warehouseId) throws Exception {
        System.out.println("=== ИМПОРТ ОСТАТКОВ ТОВАРОВ ИЗ EXCEL ===\n");
        System.out.println("Склад ID: " + warehouseId + "\n");

        // 1. Загружаем все SKU из БД в HashMap для быстрого поиска
        System.out.println("📥 Загрузка всех SKU и цен из базы данных...");
        Map<String, DbProduct> skuToProductMap = new HashMap<>();
        List<DbProduct> allProducts = jdbcTemplate.query(
                "SELECT id, sku, price FROM products",
                (rs, rowNum) -> {
                    DbProduct product = new DbProduct(
                            rs.getInt("id"),
                            rs.getString("sku"),
                            rs.getBigDecimal("price")
                    );
                    skuToProductMap.put(rs.getString("sku"), product);
                    return product;
                }
        );
        System.out.println("✅ Загружено " + allProducts.size() + " товаров из БД\n");

        // Счетчики для статистики
        int stockUpdated = 0;
        int priceUpdated = 0;
        int notFound = 0;
        int errors = 0;
        int skipped = 0;
        int invalidSku = 0;
        int priceCheckCount = 0;
        int zeroPriceInDbCount = 0;

        // Подсчитываем товары с ценой 0 в БД
        for (DbProduct p : allProducts) {
            if (p.price == null || p.price.compareTo(BigDecimal.ZERO) == 0) {
                zeroPriceInDbCount++;
            }
        }
        System.out.println("📊 Товаров с ценой 0 или NULL в БД: " + zeroPriceInDbCount + "\n");

        // Для отладки - выведем первые 10 товаров из БД с ценами 0
        System.out.println("🔍 ПРИМЕРЫ ТОВАРОВ С ЦЕНОЙ 0 В БД:");
        int zeroPriceExamples = 0;
        for (DbProduct p : allProducts) {
            if (zeroPriceExamples < 10 && (p.price == null || p.price.compareTo(BigDecimal.ZERO) == 0)) {
                System.out.println("   ID=" + p.id + ", SKU='" + p.sku + "', price=" + p.price);
                zeroPriceExamples++;
            }
        }
        if (zeroPriceExamples == 0) {
            System.out.println("   ⚠️ Нет товаров с ценой 0 в БД! Цены обновляться не будут.");
        }
        System.out.println();

        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = file.getName().endsWith(".xlsx") ?
                     new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            // Диагностика структуры Excel
            diagnoseExcelStructure(sheet);

            System.out.println("Начинаю импорт остатков и цен...\n");
            System.out.println("Логика поиска: извлекаем артикул из строки и ищем ТОЧНОЕ совпадение в БД");
            System.out.println("Цены обновляются ТОЛЬКО для товаров, у которых цена = 0 или NULL\n");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    // Получаем номенклатуру (название товара) - столбец A (индекс 0)
                    String nomenclature = getCellString(row.getCell(0));
                    if (nomenclature == null || nomenclature.trim().isEmpty()) {
                        nomenclature = getCellString(row.getCell(1));
                    }

                    if (nomenclature == null || nomenclature.trim().isEmpty()) {
                        skipped++;
                        continue;
                    }

                    // Пропускаем служебные строки
                    boolean isSkip = false;
                    String trimmedNomenclature = nomenclature.trim();
                    for (String keyword : SKIP_KEYWORDS) {
                        if (trimmedNomenclature.equalsIgnoreCase(keyword) ||
                                trimmedNomenclature.startsWith(keyword) ||
                                trimmedNomenclature.toLowerCase().contains(keyword.toLowerCase())) {
                            isSkip = true;
                            break;
                        }
                    }
                    if (isSkip) {
                        skipped++;
                        continue;
                    }

                    // Получаем количество - столбец C (индекс 2)
                    BigDecimal quantity = getCellBigDecimal(row.getCell(2));

                    // Получаем себестоимость - столбец D (индекс 3)
                    BigDecimal costPrice = getCellBigDecimal(row.getCell(3));
                    if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
                        // Если в столбце 3 нет, пробуем столбец 2 (на всякий случай)
                        costPrice = getCellBigDecimal(row.getCell(2));
                    }

                    // Извлекаем артикул из номенклатуры
                    String sku = extractSkuFromNomenclature(nomenclature);

                    // Пропускаем невалидные артикулы
                    if (sku == null || sku.trim().isEmpty()) {
                        invalidSku++;
                        continue;
                    }

                    // Очищаем артикул от лишних символов
                    sku = sku.trim();

                    // Пропускаем, если это бренд или служебное слово
                    if (isSkipWord(sku)) {
                        invalidSku++;
                        continue;
                    }

                    // Ищем ТОЧНОЕ совпадение в БД
                    DbProduct product = skuToProductMap.get(sku);

                    // Если не нашли по точному совпадению, пробуем найти по нормализованному SKU
                    if (product == null) {
                        String normalizedSku = normalizeSku(sku);
                        for (DbProduct p : allProducts) {
                            if (normalizeSku(p.sku).equals(normalizedSku)) {
                                product = p;
                                System.out.println("  🔍 Найден по нормализованному SKU: '" + sku + "' -> " + p.sku);
                                break;
                            }
                        }
                    }

                    if (product == null) {
                        if (notFound < 30) {
                            System.out.println("❌ Товар не найден: SKU='" + sku + "' | Оригинал: " +
                                    (nomenclature.length() > 60 ? nomenclature.substring(0, 60) + "..." : nomenclature));
                        }
                        notFound++;
                        continue;
                    }

                    // Обновляем остаток
                    updateStock(product.id, quantity, warehouseId, sku);
                    stockUpdated++;

                    // ОТЛАДКА ЦЕН - показываем первые 30 товаров с себестоимостью
                    if (priceCheckCount < 30 && costPrice != null && costPrice.compareTo(BigDecimal.ZERO) > 0) {
                        System.out.println("\n🔍 [ОТЛАДКА ЦЕНЫ #" + (priceCheckCount + 1) + "]");
                        System.out.println("   Строка Excel: " + (row.getRowNum() + 1));
                        System.out.println("   SKU: '" + sku + "'");
                        System.out.println("   ID товара в БД: " + product.id);
                        System.out.println("   Себестоимость из Excel (costPrice): " + costPrice);
                        System.out.println("   Цена в БД (product.price): " + product.price);
                        System.out.println("   product.price == null? " + (product.price == null));
                        System.out.println("   product.price == 0? " + (product.price != null && product.price.compareTo(BigDecimal.ZERO) == 0));
                        System.out.println("   product.price > 0? " + (product.price != null && product.price.compareTo(BigDecimal.ZERO) > 0));

                        if (product.price != null && product.price.compareTo(BigDecimal.ZERO) > 0) {
                            System.out.println("   ⚠️ ЦЕНА НЕ БУДЕТ ОБНОВЛЕНА, так как в БД уже есть цена: " + product.price);
                        } else {
                            System.out.println("   ✅ ЦЕНА БУДЕТ ОБНОВЛЕНА (в БД цена 0 или NULL)");
                        }
                    }

                    // Обновляем цену, если она не установлена (0 или NULL)
                    if (costPrice != null && costPrice.compareTo(BigDecimal.ZERO) > 0) {
                        boolean priceUpdatedFlag = updatePriceIfNeeded(product.id, costPrice, sku, product.price);
                        if (priceUpdatedFlag) {
                            priceUpdated++;
                            if (priceCheckCount < 30) {
                                System.out.println("   ✅ ЦЕНА УСПЕШНО ОБНОВЛЕНА!");
                            }
                        } else if (priceCheckCount < 30) {
                            System.out.println("   ❌ ЦЕНА НЕ ОБНОВЛЕНА (функция updatePriceIfNeeded вернула false)");
                            if (product.price != null && product.price.compareTo(BigDecimal.ZERO) > 0) {
                                System.out.println("      Причина: в БД уже есть цена " + product.price);
                            }
                        }
                        priceCheckCount++;
                    } else if (priceCheckCount < 30) {
                        if (costPrice == null) {
                            System.out.println("\n🔍 [ОТЛАДКА ЦЕНЫ #" + (priceCheckCount + 1) + "]");
                            System.out.println("   SKU: '" + sku + "'");
                            System.out.println("   Себестоимость из Excel: NULL (нет данных)");
                            priceCheckCount++;
                        } else if (costPrice.compareTo(BigDecimal.ZERO) == 0) {
                            System.out.println("\n🔍 [ОТЛАДКА ЦЕНЫ #" + (priceCheckCount + 1) + "]");
                            System.out.println("   SKU: '" + sku + "'");
                            System.out.println("   Себестоимость из Excel: 0 (пропускаем)");
                            priceCheckCount++;
                        }
                    }

                    if ((stockUpdated + priceUpdated) % 100 == 0) {
                        System.out.println("Обработано " + (stockUpdated + priceUpdated) + " позиций, не найдено: " + notFound);
                    }

                } catch (Exception e) {
                    errors++;
                    System.err.println("Ошибка в строке " + (row.getRowNum() + 1) + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("\n=== ИМПОРТ ОСТАТКОВ И ЦЕН ЗАВЕРШЕН ===");
            System.out.println("✅ Обновлено остатков: " + stockUpdated);
            System.out.println("💰 Обновлено цен (было 0): " + priceUpdated);
            System.out.println("❌ Товаров не найдено в БД: " + notFound);
            System.out.println("⚠️ Не удалось извлечь артикул или артикул невалиден: " + invalidSku);
            System.out.println("⏭️ Пропущено строк (заголовки, итоги): " + skipped);
            System.out.println("🔥 Ошибок: " + errors);
            System.out.println("📊 Проверено цен на обновление: " + priceCheckCount);
            System.out.println("📊 Товаров с ценой 0 в БД: " + zeroPriceInDbCount);

            if (zeroPriceInDbCount == 0) {
                System.out.println("\n⚠️ ВНИМАНИЕ: В БД нет товаров с ценой 0!");
                System.out.println("   Чтобы обновить цены, нужно либо:");
                System.out.println("   1. Обнулить цены в БД для нужных товаров, либо");
                System.out.println("   2. Изменить логику - обновлять цены для всех товаров");
            }
        }
    }

    /**
     * Диагностика структуры Excel файла
     */
    private void diagnoseExcelStructure(Sheet sheet) {
        System.out.println("\n=== ДИАГНОСТИКА СТРУКТУРЫ EXCEL ===\n");

        // Берем первую строку с заголовками
        Row headerRow = sheet.getRow(0);
        if (headerRow != null) {
            System.out.println("Заголовки столбцов:");
            for (int i = 0; i < 10; i++) {
                Cell cell = headerRow.getCell(i);
                String value = getCellString(cell);
                if (value != null && !value.isEmpty()) {
                    System.out.println("  Столбец " + i + " (" + getColumnName(i) + "): '" + value + "'");
                }
            }
        }

        System.out.println("\nПервые 3 строки с данными (строки 1-3):");
        for (int rowNum = 1; rowNum <= 3; rowNum++) {
            Row row = sheet.getRow(rowNum);
            if (row == null) continue;

            System.out.println("\n--- Строка " + (rowNum + 1) + " ---");
            System.out.println("  Столбец 0 (A): '" + getCellString(row.getCell(0)) + "'");
            System.out.println("  Столбец 1 (B): '" + getCellString(row.getCell(1)) + "'");
            System.out.println("  Столбец 2 (C): '" + getCellString(row.getCell(2)) + "'");
            System.out.println("  Столбец 3 (D): '" + getCellString(row.getCell(3)) + "'");
            System.out.println("  Столбец 4 (E): '" + getCellString(row.getCell(4)) + "'");
            System.out.println("  Столбец 5 (F): '" + getCellString(row.getCell(5)) + "'");
            System.out.println("  Столбец 6 (G): '" + getCellString(row.getCell(6)) + "'");
            System.out.println("  Столбец 7 (H): '" + getCellString(row.getCell(7)) + "'");
            System.out.println("  Столбец 8 (I): '" + getCellString(row.getCell(8)) + "'");
            System.out.println("  Столбец 9 (J): '" + getCellString(row.getCell(9)) + "'");
        }
        System.out.println("\n=== КОНЕЦ ДИАГНОСТИКИ ===\n");
    }

    private String getColumnName(int index) {
        String[] names = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P"};
        if (index < names.length) return names[index];
        return String.valueOf(index);
    }

    /**
     * Обновление цены товара, если текущая цена равна 0 или NULL
     * @param productId ID товара
     * @param costPrice себестоимость из Excel
     * @param sku артикул для логирования
     * @param currentPrice текущая цена в БД
     * @return true если цена была обновлена
     */
    private boolean updatePriceIfNeeded(Integer productId, BigDecimal costPrice, String sku, BigDecimal currentPrice) {
        try {
            System.out.println("   🔧 [updatePriceIfNeeded] Вызов для SKU=" + sku);
            System.out.println("      currentPrice = " + currentPrice);
            System.out.println("      costPrice = " + costPrice);

            // Проверяем, нужно ли обновлять цену (только если текущая цена 0 или NULL)
            boolean needUpdate = (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) == 0);
            System.out.println("      needUpdate = " + needUpdate);

            if (!needUpdate) {
                System.out.println("      ⏭️ Пропускаем, т.к. цена уже установлена: " + currentPrice);
                return false;
            }

            // Рассчитываем цену продажи: себестоимость + 1000 руб
            BigDecimal salePrice = costPrice.add(PRICE_MARKUP);

            // Округляем до 2 знаков после запятой
            salePrice = salePrice.setScale(2, BigDecimal.ROUND_HALF_UP);

            System.out.println("      🏷️ Новая цена продажи: " + costPrice + " + 1000 = " + salePrice);

            String updateSql = "UPDATE products SET price = ? WHERE id = ?";
            int rows = jdbcTemplate.update(updateSql, salePrice, productId);

            if (rows > 0) {
                System.out.println("  💰 Установлена цена для SKU=" + sku + " (ID=" + productId +
                        "): " + costPrice + " + 1000 = " + salePrice + " руб.");
                return true;
            } else {
                System.out.println("      ⚠️ UPDATE не затронул строк (rows=" + rows + ")");
            }

        } catch (Exception e) {
            System.err.println("  ❌ Ошибка обновления цены для SKU=" + sku + ": " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Извлечение артикула из строки номенклатуры
     * Простая логика: берем первое слово, которое похоже на артикул
     */
    private String extractSkuFromNomenclature(String nomenclature) {
        if (nomenclature == null || nomenclature.trim().isEmpty()) {
            return null;
        }

        String trimmed = nomenclature.trim();

        // Разбиваем на слова
        String[] words = trimmed.split("\\s+");

        for (String word : words) {
            // Очищаем слово от спецсимволов в начале и конце
            String cleanWord = word.replaceAll("^[^A-Za-z0-9]+|[^A-Za-z0-9]+$", "");

            if (cleanWord.isEmpty()) continue;

            // Пропускаем слишком короткие слова
            if (cleanWord.length() < 2) continue;

            // Пропускаем слишком длинные слова (более 30 символов - это не артикул)
            if (cleanWord.length() > 30) continue;

            // Пропускаем, если это бренд или служебное слово
            if (isSkipWord(cleanWord)) continue;

            // Пропускаем, если состоит только из букв и слишком короткое (менее 4 символов)
            if (cleanWord.matches("[A-Za-z]+") && cleanWord.length() < 4) continue;

            // Пропускаем, если похоже на дробное число
            if (cleanWord.matches("\\d+[.,]\\d+")) continue;

            // Это похоже на артикул
            return cleanWord;
        }

        return null;
    }

    /**
     * Проверка, является ли слово брендом или служебным словом
     */
    private boolean isSkipWord(String word) {
        if (word == null) return true;
        String upperWord = word.toUpperCase();

        // Проверяем по списку брендов
        if (SKIP_WORDS.contains(upperWord)) {
            return true;
        }

        // Проверяем, не является ли слово общеизвестным брендом авто
        String[] commonBrands = {"MB", "BMW", "AUDI", "VW", "OPEL", "FORD", "NISSAN",
                "TOYOTA", "HYUNDAI", "KIA", "MITSUBISHI", "HONDA", "MAZDA", "SUBARU",
                "SUZUKI", "DAIHATSU", "ISUZU", "HINO", "UD", "FUSO", "YUTONG", "KINGLONG"};

        for (String brand : commonBrands) {
            if (upperWord.equals(brand)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Нормализация SKU (удаление спецсимволов)
     */
    private String normalizeSku(String sku) {
        if (sku == null || sku.isEmpty()) return "";
        return sku.toUpperCase().replaceAll("[^A-Za-z0-9]", "");
    }

    /**
     * Обновление остатка товара
     */
    private void updateStock(Integer productId, BigDecimal quantity, Integer warehouseId, String sku) {
        try {
            int intQuantity = quantity != null ? quantity.intValue() : 0;

            String checkSql = "SELECT COUNT(*) FROM inventory WHERE product_id = ? AND warehouse_id = ?";
            Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, productId, warehouseId);

            if (count != null && count > 0) {
                String updateSql = "UPDATE inventory SET quantity = ? WHERE product_id = ? AND warehouse_id = ?";
                jdbcTemplate.update(updateSql, intQuantity, productId, warehouseId);
                System.out.println("  ✅ Обновлен остаток для SKU=" + sku + " (ID=" + productId + "): " + intQuantity + " шт.");
            } else {
                String insertSql = "INSERT INTO inventory (product_id, quantity, warehouse_id) VALUES (?, ?, ?)";
                jdbcTemplate.update(insertSql, productId, intQuantity, warehouseId);
                System.out.println("  ✅ Создан новый остаток для SKU=" + sku + " (ID=" + productId + "): " + intQuantity + " шт.");
            }

        } catch (Exception e) {
            System.err.println("  ❌ Ошибка для SKU=" + sku + ": " + e.getMessage());
        }
    }

    private String getCellString(Cell cell) {
        if (cell == null) return null;

        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> {
                double value = cell.getNumericCellValue();
                if (value == (long) value) {
                    yield String.valueOf((long) value);
                } else {
                    yield String.valueOf(value);
                }
            }
            case FORMULA -> {
                try {
                    CellType resultType = cell.getCachedFormulaResultType();
                    if (resultType == CellType.NUMERIC) {
                        double value = cell.getNumericCellValue();
                        if (value == (long) value) {
                            yield String.valueOf((long) value);
                        } else {
                            yield String.valueOf(value);
                        }
                    } else if (resultType == CellType.STRING) {
                        yield cell.getStringCellValue().trim();
                    } else {
                        yield null;
                    }
                } catch (Exception e) {
                    yield null;
                }
            }
            default -> null;
        };
    }

    private BigDecimal getCellBigDecimal(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;

        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING -> {
                    String str = cell.getStringCellValue().trim()
                            .replace(" ", "")
                            .replace(",", ".")
                            .replace("₽", "")
                            .replace("руб", "")
                            .replace("шт", "");
                    yield str.isEmpty() ? BigDecimal.ZERO : new BigDecimal(str);
                }
                case FORMULA -> {
                    try {
                        double value = cell.getNumericCellValue();
                        yield BigDecimal.valueOf(value);
                    } catch (Exception e) {
                        yield BigDecimal.ZERO;
                    }
                }
                default -> BigDecimal.ZERO;
            };
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    // Вспомогательный класс для хранения товаров из БД
    private static class DbProduct {
        int id;
        String sku;
        BigDecimal price;

        DbProduct(int id, String sku, BigDecimal price) {
            this.id = id;
            this.sku = sku;
            this.price = price;
        }
    }
}