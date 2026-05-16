package com.autoparts.autoparts_system;

import com.autoparts.autoparts_system.util.StockImporter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

@SpringBootTest
public class StockImportTest {

    @Autowired
    private StockImporter stockImporter;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void testImportStockOnly() throws Exception {
        System.out.println("\n========================================");
        System.out.println("ТОЛЬКО ИМПОРТ ОСТАТКОВ");
        System.out.println("========================================\n");

        // Импорт остатков (использует MAIN_WAREHOUSE_ID = 1)
        System.out.println(">>> ИМПОРТ ОСТАТКОВ");
        System.out.println("----------------------------------------");
        stockImporter.importStockFromExcel(
                "C:\\Users\\User\\Desktop\\База\\Остатки товаров.xlsx"
        );

        // Статистика после импорта остатков
        printStockStats();
    }

    private void printStockStats() {
        System.out.println("\n📊 СТАТИСТИКА ОСТАТКОВ ПОСЛЕ ИМПОРТА");
        System.out.println("----------------------------------------");

        // Общее количество товаров в БД
        Integer totalProducts = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM products", Integer.class);
        System.out.println("📦 Всего товаров в БД: " + totalProducts);

        // Количество товаров с остатками > 0
        Integer productsWithStock = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT product_id) FROM inventory WHERE quantity > 0", Integer.class);
        System.out.println("📊 Товаров с остатком > 0: " + (productsWithStock != null ? productsWithStock : 0));

        // Общее количество единиц на складе
        Integer totalQuantity = jdbcTemplate.queryForObject(
                "SELECT SUM(quantity) FROM inventory", Integer.class);
        System.out.println("📦 Общее количество единиц на складе: " + (totalQuantity != null ? totalQuantity : 0));

        // Топ-10 товаров по остаткам
        System.out.println("\n🔝 ТОП-10 ТОВАРОВ ПО ОСТАТКАМ:");
        System.out.println("----------------------------------------");
        jdbcTemplate.query(
                "SELECT p.sku, p.name, i.quantity " +
                        "FROM inventory i " +
                        "JOIN products p ON p.id = i.product_id " +
                        "WHERE i.quantity > 0 " +
                        "ORDER BY i.quantity DESC " +
                        "LIMIT 10",
                (rs, rowNum) -> {
                    System.out.printf("  • %s - %d шт | %s%n",
                            rs.getString("sku"),
                            rs.getInt("quantity"),
                            rs.getString("name").length() > 50 ?
                                    rs.getString("name").substring(0, 50) + "..." :
                                    rs.getString("name"));
                    return null;
                }
        );

        // Проверка склада MAIN
        System.out.println("\n🏪 ИНФОРМАЦИЯ О СКЛАДЕ:");
        System.out.println("----------------------------------------");
        jdbcTemplate.query(
                "SELECT warehouse_id, COUNT(*) as product_count, SUM(quantity) as total_qty " +
                        "FROM inventory " +
                        "GROUP BY warehouse_id",
                (rs, rowNum) -> {
                    System.out.printf("  • Склад ID: %d | Товаров: %d | Всего единиц: %d%n",
                            rs.getInt("warehouse_id"),
                            rs.getInt("product_count"),
                            rs.getInt("total_qty"));
                    return null;
                }
        );
    }
    public void testCheckProductExists() {
        System.out.println("\n========================================");
        System.out.println("ПРОВЕРКА СУЩЕСТВОВАНИЯ ТОВАРОВ В БД");
        System.out.println("========================================\n");

        // Проверяем конкретный артикул, который вы видели в Excel
        String testSku = "57311";

        System.out.println("Проверка SKU: " + testSku);

        // Прямой запрос
        String sql = "SELECT id, sku, name FROM products WHERE sku = ?";
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql, testSku);
            System.out.println("  ✅ Найден товар: " + result);
        } catch (Exception e) {
            System.out.println("  ❌ Товар не найден: " + e.getMessage());
        }

        // Проверим все SKU в БД (первые 10)
        System.out.println("\nПервые 10 SKU в БД:");
        jdbcTemplate.query("SELECT sku FROM products LIMIT 10",
                (rs, rowNum) -> {
                    System.out.println("  " + rs.getString("sku"));
                    return null;
                }
        );

        // Проверим, есть ли в БД артикулы, похожие на 57311
        System.out.println("\nПоиск похожих на '57311':");
        jdbcTemplate.query("SELECT sku FROM products WHERE sku LIKE '%57311%' LIMIT 10",
                (rs, rowNum) -> {
                    System.out.println("  " + rs.getString("sku"));
                    return null;
                }
        );
    }
}