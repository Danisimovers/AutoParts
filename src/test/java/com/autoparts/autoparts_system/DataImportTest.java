package com.autoparts.autoparts_system;

import com.autoparts.autoparts_system.util.DataImporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class DataImportTest {

    @Autowired
    private DataImporter dataImporter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testImportAllFiles() throws Exception {
        System.out.println("\n========================================");
        System.out.println("НАЧИНАЮ ТЕСТИРОВАНИЕ ИМПОРТА");
        System.out.println("========================================\n");

        // 1. Импорт амортизаторов
        System.out.println(">>> ШАГ 1: ИМПОРТ АМОРТИЗАТОРОВ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Амортизаторы\\Амортизаторы.xls",
                "Амортизаторы"
        );
        printStats("После импорта амортизаторов");

        // 2. Импорт масел и фильтров
        System.out.println("\n>>> ШАГ 2: ИМПОРТ МАСЕЛ И ФИЛЬТРОВ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\.Масло  фильтра  Химия\\.Масло  фильтра  Химия.xls",
                "Масла и фильтры"
        );
        printStats("После импорта масел и фильтров");

        // 3. Импорт тормозной системы
        System.out.println("\n>>> ШАГ 3: ИМПОРТ ТОРМОЗНОЙ СИСТЕМЫ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\1 Тормозная система\\1 Тормозная система.xls",
                "Тормозная система"
        );
        printStats("После импорта тормозной системы");

        // 4. Импорт подвески
        System.out.println("\n>>> ШАГ 4: ИМПОРТ ПОДВЕСКИ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\2 Подвеска\\2 Подвеска.xls",
                "Подвеска"
        );
        printStats("После импорта подвески");

        // 5. Импорт рулевого управления
        System.out.println("\n>>> ШАГ 5: ИМПОРТ РУЛЕВОГО УПРАВЛЕНИЯ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\3 Рулевое управление\\3 Рулевое управление.xls",
                "Рулевое управление"
        );
        printStats("После импорта рулевого управления");

        // 6. Импорт воздушной системы
        System.out.println("\n>>> ШАГ 6: ИМПОРТ ВОЗДУШНОЙ СИСТЕМЫ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\4 Воздушная система\\4 Воздушная система.xls",
                "Воздушная система"
        );
        printStats("После импорта воздушной системы");

        // 7. Импорт болтов и гаек
        System.out.println("\n>>> ШАГ 7: ИМПОРТ БОЛТОВ И ГАЕК");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Болты.гайки\\Болты.гайки.xls",
                "Болты и гайки"
        );
        printStats("После импорта болтов и гаек");

        // 8. Импорт выхлопной системы
        System.out.println("\n>>> ШАГ 8: ИМПОРТ ВЫХЛОПНОЙ СИСТЕМЫ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Выхлопная система\\Выхлопная система.xls",
                "Выхлопная система"
        );
        printStats("После импорта выхлопной системы");

        // 9. Импорт карданного вала
        System.out.println("\n>>> ШАГ 9: ИМПОРТ КАРДАННОГО ВАЛА");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Карданый вал\\Карданый вал.xls",
                "Карданный вал"
        );
        printStats("После импорта карданного вала");

        // 10. Импорт подшипников
        System.out.println("\n>>> ШАГ 10: ИМПОРТ ПОДШИПНИКОВ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Подшипники\\Подшипники.xls",
                "Подшипники"
        );
        printStats("После импорта подшипников");

        // 11. Импорт подъема кабины
        System.out.println("\n>>> ШАГ 11: ИМПОРТ ПОДЪЕМА КАБИНЫ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Подъем кабины\\Подъем кабины.xls",
                "Подъем кабины"
        );
        printStats("После импорта подъема кабины");

        // 12. Импорт реактивной тяги
        System.out.println("\n>>> ШАГ 12: ИМПОРТ РЕАКТИВНОЙ ТЯГИ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Реактивная тяга\\Реактивная тяга.xls",
                "Реактивная тяга"
        );
        printStats("После импорта реактивной тяги");

        // 13. Импорт ременного привода
        System.out.println("\n>>> ШАГ 13: ИМПОРТ РЕМЕННОГО ПРИВОДА");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Ременный привод\\Ременный привод.xls",
                "Ременный привод"
        );
        printStats("После импорта ременного привода");

        // 14. Импорт рессор
        System.out.println("\n>>> ШАГ 14: ИМПОРТ РЕССОР");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Рессоры\\Рессоры.xls",
                "Рессоры"
        );
        printStats("После импорта рессор");

        // 15. Импорт системы кондиционирования
        System.out.println("\n>>> ШАГ 15: ИМПОРТ СИСТЕМЫ КОНДИЦИОНИРОВАНИЯ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Система кондиционирования\\Система кондиционирования.xls",
                "Система кондиционирования"
        );
        printStats("После импорта системы кондиционирования");

        // 16. Импорт системы охлаждения
        System.out.println("\n>>> ШАГ 16: ИМПОРТ СИСТЕМЫ ОХЛАЖДЕНИЯ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Система охлаждения\\Система охлаждения.xls",
                "Система охлаждения"
        );
        printStats("После импорта системы охлаждения");

        // 17. Импорт системы сцепления
        System.out.println("\n>>> ШАГ 17: ИМПОРТ СИСТЕМЫ СЦЕПЛЕНИЯ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Система сцепления\\Система сцепления.xls",
                "Система сцепления"
        );
        printStats("После импорта системы сцепления");

        // 18. Импорт стремянок
        System.out.println("\n>>> ШАГ 18: ИМПОРТ СТРЕМЯНОК");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Стремянки\\Стремянки.xls",
                "Стремянки"
        );
        printStats("После импорта стремянок");

        // 19. Импорт ступиц
        System.out.println("\n>>> ШАГ 19: ИМПОРТ СТУПИЦ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Ступицы\\Ступицы.xls",
                "Ступицы"
        );
        printStats("После импорта ступиц");

        // 20. Импорт топливной системы
        System.out.println("\n>>> ШАГ 20: ИМПОРТ ТОПЛИВНОЙ СИСТЕМЫ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Топливная система\\Топливная система.xls",
                "Топливная система"
        );
        printStats("После импорта топливной системы");

        // 21. Импорт фитингов
        System.out.println("\n>>> ШАГ 21: ИМПОРТ ФИТИНГОВ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Фитинги\\Фитинги.xls",
                "Фитинги"
        );
        printStats("После импорта фитингов");

        // 22. Импорт шкворней
        System.out.println("\n>>> ШАГ 22: ИМПОРТ ШКВОРНЕЙ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Шкворня\\Шкворня.xls",
                "Шкворни"
        );
        printStats("После импорта шкворней");

        // 23. Импорт электрики
        System.out.println("\n>>> ШАГ 23: ИМПОРТ ЭЛЕКТРИКИ");
        System.out.println("----------------------------------------");
        dataImporter.importFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Электрика\\Электрика.xls",
                "Электрика"
        );
        printStats("После импорта электрики");

        // Финальная проверка
        printStats("\n=== ИТОГОВАЯ СТАТИСТИКА ===");

        // Детальная проверка совместимостей
        checkCompatibilities();

        // Проверка производителей с UNKNOWN
        checkUnknownManufacturers();
    }

    private void checkUnknownManufacturers() {
        System.out.println("\n⚠️ ПРОИЗВОДИТЕЛИ С UNKNOWN:");
        System.out.println("----------------------------------------");

        jdbcTemplate.query(
                "SELECT p.sku, p.name, p.manufacturer_id " +
                        "FROM products p " +
                        "JOIN manufacturers m ON p.manufacturer_id = m.id " +
                        "WHERE m.name = 'UNKNOWN' " +
                        "LIMIT 20",
                (rs, rowNum) -> {
                    System.out.printf("  • SKU: %s | %s%n",
                            rs.getString("sku"),
                            rs.getString("name").length() > 60 ?
                                    rs.getString("name").substring(0, 60) + "..." :
                                    rs.getString("name"));
                    return null;
                }
        );

        // Счетчик UNKNOWN производителей
        Integer unknownCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products p JOIN manufacturers m ON p.manufacturer_id = m.id WHERE m.name = 'UNKNOWN'",
                Integer.class);
        System.out.println("\n📊 Всего товаров с UNKNOWN производителем: " + unknownCount);
    }

    private void printStats(String title) {
        System.out.println("\n📊 " + title);
        System.out.println("----------------------------------------");

        Integer products = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", Integer.class);
        System.out.println("📦 Товаров: " + products);

        Integer vehicles = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM vehicles", Integer.class);
        System.out.println("🚗 Автомобилей: " + vehicles);

        Integer categories = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM categories", Integer.class);
        System.out.println("📂 Категорий: " + categories);

        Integer manufacturers = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM manufacturers", Integer.class);
        System.out.println("🏭 Производителей: " + manufacturers);

        Integer compat = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM product_vehicle_compat", Integer.class);
        System.out.println("🔗 Связей товар-авто: " + compat);
    }

    private void checkCompatibilities() {
        System.out.println("\n🔍 ПРОВЕРКА СОВМЕСТИМОСТЕЙ:");
        System.out.println("----------------------------------------");

        // Проверяем товары с совместимостями
        jdbcTemplate.query(
                "SELECT p.sku, p.name, COUNT(pvc.vehicle_id) as compat_count " +
                        "FROM products p " +
                        "LEFT JOIN product_vehicle_compat pvc ON p.id = pvc.product_id " +
                        "GROUP BY p.id " +
                        "HAVING COUNT(pvc.vehicle_id) > 0 " +
                        "ORDER BY compat_count DESC " +
                        "LIMIT 10",
                (rs, rowNum) -> {
                    System.out.printf("  • %s - %d совместимостей | SKU: %s%n",
                            rs.getString("name").length() > 50 ?
                                    rs.getString("name").substring(0, 50) + "..." :
                                    rs.getString("name"),
                            rs.getInt("compat_count"),
                            rs.getString("sku"));
                    return null;
                }
        );

        // Проверяем марки автомобилей
        System.out.println("\n🚘 МАРКИ АВТОМОБИЛЕЙ В БАЗЕ:");
        jdbcTemplate.query(
                "SELECT make, COUNT(*) as count FROM vehicles GROUP BY make ORDER BY make",
                (rs, rowNum) -> {
                    System.out.printf("  • %s (%d авто)%n",
                            rs.getString("make"), rs.getInt("count"));
                    return null;
                }
        );

        // Проверяем категории
        System.out.println("\n📁 КАТЕГОРИИ:");
        jdbcTemplate.query(
                "SELECT name, COUNT(*) as count FROM categories GROUP BY name ORDER BY name",
                (rs, rowNum) -> {
                    System.out.printf("  • %s (%d товаров)%n",
                            rs.getString("name"), rs.getInt("count"));
                    return null;
                }
        );
    }
}