package com.autoparts.autoparts_system.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileInputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataImporter {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final Map<String, Long> CATEGORY_MAP = new HashMap<>();
    private static final Map<String, Long> MANUFACTURER_MAP = new HashMap<>();
    private static final Map<String, Long> VEHICLE_MAP = new HashMap<>();

    // Бренды-производители запчастей (расширенный список)
    private static final Set<String> KNOWN_BRANDS = new LinkedHashSet<>(Arrays.asList(
            // Оригинальные бренды
            "TOPCOVER", "SAMPA", "ROSTAR", "SACHS", "AUGER", "FEBI", "PAAZ",
            "SABO", "WABCO", "MONROE", "MARSHALL", "STELLOX", "BPW", "S&K", "GMBH",
            "SIMPECO", "AIRKRAFT", "TRIALLI", "FENOX", "LUCKTECH", "KMZ", "MARSHAL",
            "SONDER", "SE-M", "PE", "HD-PARTS",

            // Бренды масел, фильтров, химии
            "НЕЙС", "РОСНЕФТЬ", "FILMANT", "MFILTER", "TOTACHI", "TRUCK PART",
            "BOSCH", "CARVILLE RACING", "DIFA", "GOODWILL", "SAKURA", "BIG FILTER",
            "KS", "HENGST", "ZENTPARTS", "DONALDSON", "FLEETGUARD", "MANN-FILTER",
            "BALDWIN", "LUBER-FINER", "KNORR", "WURTH", "LAVR", "ABRO", "HI-GEAR",
            "KUDO", "OILRIGHT", "FELIX", "ASTROHIM", "MOBIL", "SHELL", "LUKOIL",
            "TOTAL", "ELF", "ZIC", "TEXACO", "GAZPROMNEFT", "LUBEX", "TEBOIL",
            "REPSOL", "C.N.R.G.", "SINTEC", "MOTUL", "ROLF", "SEAGULL", "LEMARC",
            "PETROVISCOL", "MANNOL", "PEMCO", "CNRG", "NEXT", "DEVRON", "ЛУКОЙЛ",
            "ГАЗПРОМ", "ТОТАЧИ", "М+", "Н/М",

            // НОВЫЕ БРЕНДЫ (добавьте их)
            "KRAUF",        // бренд фильтров
            "GRASS",        // бренд автохимии
            "КАМЧАТКА",     // бренд антифризов
            "REINZ",        // Victor Reinz - бренд прокладок
            "VICTOR REINZ", // полное имя
            "SHAANXI",      // бренд грузовиков и запчастей
            "SHACMAN",      // бренд грузовиков
            "FILTRON",      // бренд фильтров
            "TRP",          // бренд запчастей
            "ЛИВНЫ",        // Ливны - бренд фильтров
            "SVR",          // бренд фильтров
            "MOZER",        // бренд автохимии
            "SORL",         // бренд запчастей
            "PAAZ",         // уже есть, но убедитесь
            "WABCO",        // уже есть
            "HALDEX",       // бренд тормозных систем
            "ZF",           // бренд трансмиссий
            "EATON",        // бренд трансмиссий
            "DETROIT",      // Detroit Diesel
            "DETROIT DIESEL",
            "CATERPILLAR",  // бренд техники
            "CUMMINS",      // бренд двигателей
            "DEUTZ",        // бренд двигателей
            "PERKINS",      // бренд двигателей
            "MANITOU",      // бренд техники
            "JCB",          // бренд техники
            "KOMATSU",      // бренд техники
            "CASE",         // бренд техники
            "CASE IH",      // бренд техники
            "NEW HOLLAND",  // бренд техники
            "JOHN DEERE",   // бренд техники
            "BOBCAT",       // бренд техники
            "TEREX",        // бренд техники
            "LIEBHERR",     // бренд техники
            "ATLAS COPCO",  // бренд компрессоров
            "INGERSOLL-RAND", // бренд компрессоров

            // Торговые марки
            "NEXT",         // бренд автохимии
            "DEVRON",       // бренд смазок
            "DEVON",        // альтернативное написание
            "TEXACO",       // бренд масел
            "ENEOS",        // бренд масел
            "CASTROL",      // бренд масел
            "LIQUI MOLY",   // бренд автохимии
            "MOTUL",        // бренд масел
            "PEMCO",        // бренд масел

            // Китайские бренды
            "HOWO",         // грузовики
            "SITRAK",       // грузовики
            "FAW",          // грузовики
            "FOTON",        // грузовики
            "DONGFENG",     // грузовики
            "SHACMAN",      // грузовики
            "SHAANXI",       // грузовики

            // Добавьте эти бренды в KNOWN_BRANDS (в конец списка)
            "ALCAN", "EXOVO", "TTT", "BERAL", "RAPIT", "JURATEK", "TEXTAR",
            "TRW", "DON", "FRAS-LE", "FOMAR", "TECHNO BRAKE", "VIAGGE",
            "COJALI", "ANDAC", "ANDTECH", "ATD", "BIGOAL", "EMMERRE", "ENTERPRISE",
            "GEPARTS", "GRONAX", "HOTTECKE", "KANN", "LUMAG", "MANSIONS",
            "MAXI", "MEGAPOWER", "MONIVA", "ONYARBI", "OREX", "PE", "PRAMO",
            "RIGINAL", "ROTTA", "SAMPA", "SE-M", "SIMPECO", "SOLERS", "SONDER",
            "SORL", "STAL", "TABOC", "TRIALLI", "VALEO", "VIBERTI", "WWI",
            "ZENTPARTS", "ZF"
    ));

    // Нормализация названий марок
    private static final Map<String, String> VEHICLE_NORMALIZE = new HashMap<>();
    static {
        VEHICLE_NORMALIZE.put("VOLVO", "VOLVO");
        VEHICLE_NORMALIZE.put("MERCEDES", "MERCEDES-BENZ");
        VEHICLE_NORMALIZE.put("MERCEDES-BENZ", "MERCEDES-BENZ");
        VEHICLE_NORMALIZE.put("MB", "MERCEDES-BENZ");
        VEHICLE_NORMALIZE.put("SCANIA", "SCANIA");
        VEHICLE_NORMALIZE.put("DAF", "DAF");
        VEHICLE_NORMALIZE.put("MAN", "MAN");
        VEHICLE_NORMALIZE.put("RENAULT", "RENAULT");
        VEHICLE_NORMALIZE.put("IVECO", "IVECO");
        VEHICLE_NORMALIZE.put("КАМАЗ", "КАМАЗ");
        VEHICLE_NORMALIZE.put("KAMAZ", "КАМАЗ");
        VEHICLE_NORMALIZE.put("МАЗ", "МАЗ");
        VEHICLE_NORMALIZE.put("MAZ", "МАЗ");
        // Добавляем новые марки
        VEHICLE_NORMALIZE.put("CASE", "CASE");
        VEHICLE_NORMALIZE.put("NEW HOLLAND", "NEW HOLLAND");
        VEHICLE_NORMALIZE.put("JCB", "JCB");
        VEHICLE_NORMALIZE.put("KOMATSU", "KOMATSU");
        VEHICLE_NORMALIZE.put("HOWO", "HOWO");
        VEHICLE_NORMALIZE.put("SITRAK", "SITRAK");
        VEHICLE_NORMALIZE.put("SHAANXI", "SHAANXI");
        VEHICLE_NORMALIZE.put("SHACMAN", "SHACMAN");
        VEHICLE_NORMALIZE.put("FAW", "FAW");
        VEHICLE_NORMALIZE.put("FOTON", "FOTON");
        VEHICLE_NORMALIZE.put("DONGFENG", "DONGFENG");
        VEHICLE_NORMALIZE.put("CUMMINS", "CUMMINS");
        VEHICLE_NORMALIZE.put("CATERPILLAR", "CATERPILLAR");
        VEHICLE_NORMALIZE.put("PERKINS", "PERKINS");
        VEHICLE_NORMALIZE.put("DEUTZ", "DEUTZ");
        VEHICLE_NORMALIZE.put("HYUNDAI", "HYUNDAI");
        VEHICLE_NORMALIZE.put("KIA", "KIA");
        VEHICLE_NORMALIZE.put("MITSUBISHI", "MITSUBISHI");
        VEHICLE_NORMALIZE.put("NISSAN", "NISSAN");
        VEHICLE_NORMALIZE.put("TOYOTA", "TOYOTA");
        VEHICLE_NORMALIZE.put("FORD", "FORD");
        VEHICLE_NORMALIZE.put("VOLKSWAGEN", "VOLKSWAGEN");
        VEHICLE_NORMALIZE.put("VW", "VOLKSWAGEN");
        VEHICLE_NORMALIZE.put("OPEL", "OPEL");
        VEHICLE_NORMALIZE.put("PEUGEOT", "PEUGEOT");
        VEHICLE_NORMALIZE.put("CITROEN", "CITROEN");
        VEHICLE_NORMALIZE.put("FIAT", "FIAT");
    }

    public void importFromExcel(String filePath, String categoryName) throws Exception {
        System.out.println("=== ИМПОРТ ТОВАРОВ ИЗ EXCEL ===\n");

        Long categoryId = getOrCreateCategory(categoryName);
        System.out.println("Категория: " + categoryName + " (ID: " + categoryId + ")\n");

        File file = new File(filePath);
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = file.getName().endsWith(".xlsx") ?
                     new XSSFWorkbook(fis) : new HSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);

            int imported = 0;
            int errors = 0;
            int linkedVehicles = 0;

            System.out.println("Начинаю импорт...\n");

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;

                try {
                    String fullName = getCellString(row.getCell(0));
                    String sku = getCellString(row.getCell(8));
                    BigDecimal price = getCellBigDecimal(row.getCell(6));
                    String description = getCellString(row.getCell(9));

                    if (sku == null || sku.trim().isEmpty()) {
                        // Если SKU нет, пробуем взять из другого столбца или генерируем
                        sku = generateSku(fullName);
                    }

                    if (sku == null || sku.trim().isEmpty()) continue;

                    // Обрезаем sku если слишком длинный
                    if (sku.length() > 100) {
                        sku = sku.substring(0, 100);
                    }

                    // Определяем производителя товара
                    String manufacturer = detectManufacturer(fullName);
                    Long manufacturerId = getOrCreateManufacturer(manufacturer);

                    String oemCodes = extractOemCodes(description);
                    String cleanName = cleanProductName(fullName, manufacturer);

                    // Обрезаем name если слишком длинный
                    if (cleanName != null && cleanName.length() > 255) {
                        cleanName = cleanName.substring(0, 255);
                    }

                    if (description != null && description.length() > 1000) {
                        description = description.substring(0, 1000);
                    }

                    // Сохраняем товар
                    saveProduct(sku, cleanName, description, price, categoryId, manufacturerId, oemCodes);
                    imported++;

                    // Извлекаем марки автомобилей (только марки, без моделей)
                    Set<String> vehicleMakes = extractVehicleMakes(fullName);
                    for (String make : vehicleMakes) {
                        Long vehicleId = getOrCreateVehicle(make);
                        if (vehicleId != null) {
                            linkProductToVehicle(sku, vehicleId);
                            linkedVehicles++;
                        }
                    }

                    if (imported % 100 == 0) {
                        System.out.println("Импортировано " + imported + " товаров, создано связей: " + linkedVehicles);
                    }

                } catch (Exception e) {
                    errors++;
                    System.err.println("Ошибка в строке " + row.getRowNum() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("\n=== ИМПОРТ ЗАВЕРШЕН ===");
            System.out.println("Импортировано товаров: " + imported);
            System.out.println("Создано связей с автомобилями: " + linkedVehicles);
            System.out.println("Ошибок: " + errors);
        }
    }

    /**
     * Генерация SKU если его нет
     */
    private String generateSku(String fullName) {
        if (fullName == null) return null;
        // Генерируем SKU из первых букв и цифр
        String sku = fullName.replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (sku.length() > 20) {
            sku = sku.substring(0, 20);
        }
        return sku;
    }

    /**
     * Извлечение марок автомобилей из названия
     * Сначала проверяем КАМАЗ, потом только МАЗ (чтобы не было путаницы)
     */
    private Set<String> extractVehicleMakes(String fullName) {
        Set<String> makes = new HashSet<>();
        if (fullName == null) return makes;

        String upperName = fullName.toUpperCase();

        System.out.println("DEBUG: Анализируем название: " + fullName);

        // ===== НАСТОЯЩИЕ МАРКИ АВТОМОБИЛЕЙ =====
        // Это только те бренды, которые производят ГРУЗОВЫЕ АВТОМОБИЛИ
        Set<String> validTruckBrands = new HashSet<>(Arrays.asList(
                "VOLVO", "MERCEDES-BENZ", "MERCEDES", "SCANIA", "DAF", "MAN",
                "RENAULT", "IVECO", "КАМАЗ", "KAMAZ", "МАЗ", "MAZ",
                "HOWO", "SITRAK", "SHAANXI", "SHACMAN", "FAW", "FOTON", "DONGFENG",
                "FORD", "VOLKSWAGEN", "VW", "OPEL", "PEUGEOT", "CITROEN", "FIAT",
                "HYUNDAI", "KIA", "NISSAN", "TOYOTA", "MITSUBISHI"
        ));

        // ===== ПРОИЗВОДИТЕЛИ ДВИГАТЕЛЕЙ И СПЕЦТЕХНИКИ (НЕ ДОБАВЛЯТЬ В vehicles) =====
        Set<String> invalidBrands = new HashSet<>(Arrays.asList(
                "CATERPILLAR", "CUMMINS", "DEUTZ", "PERKINS", "DETROIT", "DETROIT DIESEL",
                "BOSCH", "ZF", "EATON", "WABCO", "KNORR", "HALDEX"
        ));

        // Сначала проверяем составные или проблемные марки
        // КАМАЗ проверяем ДО МАЗ, чтобы не было ложного срабатывания
        if (upperName.contains("КАМАЗ") || upperName.contains("KAMAZ")) {
            makes.add("КАМАЗ");
            System.out.println("DEBUG: Найдена марка: КАМАЗ");
        }
        // МАЗ проверяем только если нет КАМАЗ
        else if ((upperName.contains("МАЗ") || upperName.contains("MAZ")) &&
                !upperName.contains("КАМАЗ") && !upperName.contains("KAMAZ")) {
            makes.add("МАЗ");
            System.out.println("DEBUG: Найдена марка: МАЗ");
        }

        // Проверяем остальные марки (только валидные)
        if (upperName.contains("MERCEDES") || upperName.contains("MERCEDES-BENZ") ||
                (upperName.contains("MB") && !upperName.contains("MB ACTROS"))) {
            makes.add("MERCEDES-BENZ");
            System.out.println("DEBUG: Найдена марка: MERCEDES-BENZ");
        }
        if (upperName.contains("VOLVO")) {
            makes.add("VOLVO");
            System.out.println("DEBUG: Найдена марка: VOLVO");
        }
        if (upperName.contains("SCANIA")) {
            makes.add("SCANIA");
            System.out.println("DEBUG: Найдена марка: SCANIA");
        }
        if (upperName.contains("DAF")) {
            makes.add("DAF");
            System.out.println("DEBUG: Найдена марка: DAF");
        }
        if (upperName.contains("MAN")) {
            makes.add("MAN");
            System.out.println("DEBUG: Найдена марка: MAN");
        }
        if (upperName.contains("RENAULT")) {
            makes.add("RENAULT");
            System.out.println("DEBUG: Найдена марка: RENAULT");
        }
        if (upperName.contains("IVECO")) {
            makes.add("IVECO");
            System.out.println("DEBUG: Найдена марка: IVECO");
        }

        // Дополнительные марки грузовиков
        if (upperName.contains("HOWO") && !upperName.contains("HOWO")) {
            makes.add("HOWO");
            System.out.println("DEBUG: Найдена марка: HOWO");
        }
        if (upperName.contains("SITRAK")) {
            makes.add("SITRAK");
            System.out.println("DEBUG: Найдена марка: SITRAK");
        }
        if (upperName.contains("FAW")) {
            makes.add("FAW");
            System.out.println("DEBUG: Найдена марка: FAW");
        }
        if (upperName.contains("FOTON")) {
            makes.add("FOTON");
            System.out.println("DEBUG: Найдена марка: FOTON");
        }
        if (upperName.contains("DONGFENG")) {
            makes.add("DONGFENG");
            System.out.println("DEBUG: Найдена марка: DONGFENG");
        }

        // Легковые автомобили
        if (upperName.contains("FORD")) {
            makes.add("FORD");
            System.out.println("DEBUG: Найдена марка: FORD");
        }
        if (upperName.contains("VOLKSWAGEN") || upperName.contains("VW")) {
            makes.add("VOLKSWAGEN");
            System.out.println("DEBUG: Найдена марка: VOLKSWAGEN");
        }
        if (upperName.contains("OPEL")) {
            makes.add("OPEL");
            System.out.println("DEBUG: Найдена марка: OPEL");
        }
        if (upperName.contains("PEUGEOT")) {
            makes.add("PEUGEOT");
            System.out.println("DEBUG: Найдена марка: PEUGEOT");
        }
        if (upperName.contains("CITROEN")) {
            makes.add("CITROEN");
            System.out.println("DEBUG: Найдена марка: CITROEN");
        }
        if (upperName.contains("FIAT")) {
            makes.add("FIAT");
            System.out.println("DEBUG: Найдена марка: FIAT");
        }
        if (upperName.contains("HYUNDAI")) {
            makes.add("HYUNDAI");
            System.out.println("DEBUG: Найдена марка: HYUNDAI");
        }
        if (upperName.contains("KIA")) {
            makes.add("KIA");
            System.out.println("DEBUG: Найдена марка: KIA");
        }
        if (upperName.contains("NISSAN")) {
            makes.add("NISSAN");
            System.out.println("DEBUG: Найдена марка: NISSAN");
        }
        if (upperName.contains("TOYOTA")) {
            makes.add("TOYOTA");
            System.out.println("DEBUG: Найдена марка: TOYOTA");
        }
        if (upperName.contains("MITSUBISHI")) {
            makes.add("MITSUBISHI");
            System.out.println("DEBUG: Найдена марка: MITSUBISHI");
        }

        // ❌ Удаляем невалидные марки (производители двигателей)
        makes.removeAll(invalidBrands);

        // Дополнительная проверка: если остались подозрительные марки, выводим предупреждение
        for (String make : makes) {
            if (!validTruckBrands.contains(make) &&
                    !make.equals("КАМАЗ") && !make.equals("МАЗ")) {
                System.out.println("⚠️ ВНИМАНИЕ: Подозрительная марка '" + make +
                        "' добавлена в vehicles. Проверьте необходимость.");
            }
        }

        System.out.println("DEBUG: Найдено марок (после фильтрации): " + makes);
        return makes;
    }

    /**
     * Получить или создать автомобиль
     */
    private Long getOrCreateVehicle(String make) {
        String normalizedMake = make.toUpperCase();

        if (VEHICLE_MAP.containsKey(normalizedMake)) {
            return VEHICLE_MAP.get(normalizedMake);
        }

        try {
            Long id = jdbcTemplate.queryForObject(
                    "SELECT id FROM vehicles WHERE UPPER(make) = ? AND (model IS NULL OR model = '')",
                    Long.class, normalizedMake
            );
            VEHICLE_MAP.put(normalizedMake, id);
            return id;
        } catch (Exception e) {
            // Не найден - создаем новый
        }

        try {
            String sql = "INSERT INTO vehicles (make, model) VALUES (?, '')";
            jdbcTemplate.update(sql, make);
            Long id = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
            VEHICLE_MAP.put(normalizedMake, id);
            System.out.println("  Добавлена марка авто: " + make);
            return id;
        } catch (DuplicateKeyException e) {
            return VEHICLE_MAP.get(normalizedMake);
        } catch (Exception e) {
            System.err.println("  Ошибка добавления марки " + make + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Связывание товара с автомобилем
     */
    private void linkProductToVehicle(String sku, Long vehicleId) {
        try {
            String sql = "INSERT INTO product_vehicle_compat (product_id, vehicle_id) " +
                    "SELECT p.id, ? FROM products p WHERE p.sku = ?";
            jdbcTemplate.update(sql, vehicleId, sku);
        } catch (DuplicateKeyException e) {
            // Связь уже существует
        } catch (Exception e) {
            // Игнорируем ошибки связывания
        }
    }

    private String detectManufacturer(String productName) {
        if (productName == null) return "UNKNOWN";
        String upperName = productName.toUpperCase();
        for (String brand : KNOWN_BRANDS) {
            if (upperName.contains(brand)) {
                return brand;
            }
        }
        return "UNKNOWN";
    }

    private String extractOemCodes(String description) {
        if (description == null || description.isEmpty()) return null;

        String[] lines = description.split("[\\r\\n]+");
        Set<String> oemNumbers = new LinkedHashSet<>();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split("[;\\s,|]+");

            for (String part : parts) {
                part = part.trim();
                if (part.isEmpty()) continue;

                if (part.length() >= 4 && part.length() <= 25 &&
                        part.matches("^[A-Z0-9\\-]+$") &&
                        !part.matches("^[0-9]{1,3}$")) {

                    if (!part.equals("OEM") && !part.equals("DAF") &&
                            !part.equals("VOLVO") && !part.equals("MAN") &&
                            !part.equals("SCANIA") && !part.equals("MB") &&
                            !part.equals("MERCEDES") && !part.equals("BENZ") &&
                            !part.equals("RENAULT") && !part.equals("IVECO") &&
                            !part.equals("КАМАЗ") && !part.equals("МАЗ") &&
                            !part.equals("KAMAZ") && !part.equals("MAZ")) {
                        oemNumbers.add(part);
                    }
                }
            }
        }

        return oemNumbers.isEmpty() ? null : String.join(";", oemNumbers);
    }

    private String cleanProductName(String fullName, String manufacturer) {
        if (fullName == null) return null;

        String[] parts = fullName.split(" ");
        if (parts.length > 1 && parts[0].matches("^[A-Z0-9\\-\\.]+$")) {
            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < parts.length; i++) {
                sb.append(parts[i]).append(" ");
            }
            fullName = sb.toString().trim();
        }

        if (!manufacturer.equals("UNKNOWN")) {
            fullName = fullName.replace(manufacturer, "");
            fullName = fullName.replace(manufacturer.toUpperCase(), "");
            fullName = fullName.replace(manufacturer.toLowerCase(), "");
        }

        fullName = fullName.replaceAll("\\s+", " ").trim();
        return fullName;
    }

    private void saveProduct(String sku, String name, String description, BigDecimal price,
                             Long categoryId, Long manufacturerId, String oemCodes) {

        String sql = "INSERT INTO products (sku, name, description, price, category_id, manufacturer_id, oem_code) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?) " +
                "ON CONFLICT (sku) DO UPDATE SET " +
                "name = EXCLUDED.name, " +
                "description = EXCLUDED.description, " +
                "price = EXCLUDED.price, " +
                "category_id = EXCLUDED.category_id, " +
                "manufacturer_id = EXCLUDED.manufacturer_id, " +
                "oem_code = EXCLUDED.oem_code";

        jdbcTemplate.update(sql, sku, name, description, price, categoryId, manufacturerId, oemCodes);

        String stockSql = "INSERT INTO inventory (product_id, quantity, warehouse_id) " +
                "SELECT id, 0, 1 FROM products WHERE sku = ? " +
                "ON CONFLICT (product_id, warehouse_id) DO NOTHING";

        try {
            jdbcTemplate.update(stockSql, sku);
        } catch (Exception e) {
            // Игнорируем ошибки остатков
        }
    }

    private Long getOrCreateCategory(String name) {
        if (CATEGORY_MAP.containsKey(name)) {
            return CATEGORY_MAP.get(name);
        }

        try {
            Long id = jdbcTemplate.queryForObject(
                    "SELECT id FROM categories WHERE name = ?",
                    Long.class, name
            );
            CATEGORY_MAP.put(name, id);
            return id;
        } catch (Exception e) {
            jdbcTemplate.update("INSERT INTO categories (name) VALUES (?)", name);
            Long id = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
            CATEGORY_MAP.put(name, id);
            System.out.println("Создана категория: " + name);
            return id;
        }
    }

    private Long getOrCreateManufacturer(String name) {
        String upperName = name.toUpperCase();

        if (MANUFACTURER_MAP.containsKey(upperName)) {
            return MANUFACTURER_MAP.get(upperName);
        }

        try {
            Long id = jdbcTemplate.queryForObject(
                    "SELECT id FROM manufacturers WHERE UPPER(name) = ?",
                    Long.class, upperName
            );
            MANUFACTURER_MAP.put(upperName, id);
            return id;
        } catch (Exception e) {
            jdbcTemplate.update("INSERT INTO manufacturers (name) VALUES (?)", name);
            Long id = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
            MANUFACTURER_MAP.put(upperName, id);
            System.out.println("Создан производитель: " + name);
            return id;
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
                            .replace("руб", "");
                    yield str.isEmpty() ? BigDecimal.ZERO : new BigDecimal(str);
                }
                default -> BigDecimal.ZERO;
            };
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }
}