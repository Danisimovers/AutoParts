package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.ExternalRequest;
import com.autoparts.autoparts_system.model.Notification;
import com.autoparts.autoparts_system.repository.ExternalRequestRepository;
import com.autoparts.autoparts_system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class ExternalRequestService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ExternalRequestRepository externalRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private StockService stockService;

    public List<ExternalRequest> getAllExternalRequests() {
        String sql = "SELECT * FROM external_requests ORDER BY created_at DESC";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ExternalRequest req = new ExternalRequest();
            req.setId(rs.getLong("id"));
            req.setUserId(rs.getLong("user_id"));
            req.setOrderId(rs.getLong("order_id"));
            req.setProductName(rs.getString("product_name"));
            req.setFactoryNumber(rs.getString("factory_number"));
            req.setProducer(rs.getString("producer"));
            req.setSupplierName(rs.getString("supplier_name"));
            req.setSupplierId(rs.getLong("supplier_id"));
            req.setPrice(rs.getDouble("price"));
            req.setStatus(rs.getString("status"));
            req.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
            return req;
        });
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        // Получаем order_id перед обновлением статуса
        String selectOrderSql = "SELECT order_id FROM external_requests WHERE id = ?";
        Long orderId = null;
        try {
            orderId = jdbcTemplate.queryForObject(selectOrderSql, Long.class, id);
        } catch (Exception e) {
            // Может не быть order_id у старых записей
        }

        String sql = "UPDATE external_requests SET status = ? WHERE id = ?";
        jdbcTemplate.update(sql, status, id);

        // Синхронизируем статус заказа
        if (orderId != null) {
            String orderStatus = null;
            switch (status) {
                case "ORDERED":
                    orderStatus = "PENDING_SUPPLIER";
                    break;
                case "COMPLETED":
                    orderStatus = "DELIVERED";
                    break;
                case "REJECTED":
                    orderStatus = "CANCELLED";
                    break;
            }
            if (orderStatus != null) {
                String updateOrderSql = "UPDATE sales_orders SET status = ? WHERE id = ?";
                jdbcTemplate.update(updateOrderSql, orderStatus, orderId);
            }
        }

        // Уведомление пользователю
        String selectUserSql = "SELECT user_id FROM external_requests WHERE id = ?";
        Long userId = jdbcTemplate.queryForObject(selectUserSql, Long.class, id);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("EXTERNAL_REQUEST_STATUS");
        notification.setTitle("Статус вашего запроса изменен");
        notification.setMessage("Статус запроса на товар изменен на: " + getStatusText(status));
        notification.setLink("/profile?tab=external-requests");
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Transactional
    public void orderFromRequest(Long id) {
        String selectSql = "SELECT * FROM external_requests WHERE id = ?";
        Map<String, Object> request = jdbcTemplate.queryForMap(selectSql, id);

        String status = (String) request.get("status");
        if (!"PROCESSING".equals(status) && !"PENDING".equals(status)) {
            throw new IllegalArgumentException("Заказ уже обработан");
        }

        Object supplierIdObj = request.get("supplier_id");
        Long supplierId;
        if (supplierIdObj == null) {
            String supplierName = (String) request.get("supplier_name");
            String findSupplierSql = "SELECT id FROM suppliers WHERE name = ?";
            supplierId = jdbcTemplate.queryForObject(findSupplierSql, Long.class, supplierName);
        } else {
            supplierId = ((Number) supplierIdObj).longValue();
        }

        String insertOrderSql = "INSERT INTO purchase_orders (supplier_id, date, status, total) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(insertOrderSql, supplierId, LocalDate.now(), "ORDERED", request.get("price"));

        // Обновляем статус external_request
        String updateSql = "UPDATE external_requests SET status = 'ORDERED' WHERE id = ?";
        jdbcTemplate.update(updateSql, id);

        // Обновляем статус заказа
        Object orderIdObj = request.get("order_id");
        if (orderIdObj != null) {
            Long orderId = ((Number) orderIdObj).longValue();
            String updateOrderSql = "UPDATE sales_orders SET status = 'PENDING_SUPPLIER' WHERE id = ?";
            jdbcTemplate.update(updateOrderSql, orderId);
        }

        Long userId = ((Number) request.get("user_id")).longValue();

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("EXTERNAL_REQUEST_STATUS");
        notification.setTitle("Товар заказан у поставщика");
        notification.setMessage("Ваш запрос на товар " + request.get("product_name") + " передан в заказ поставщику");
        notification.setLink("/profile?tab=external-requests");
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Transactional
    public Map<String, Object> addToStockFromRequest(Long id) {
        String selectSql = "SELECT * FROM external_requests WHERE id = ?";
        Map<String, Object> request = jdbcTemplate.queryForMap(selectSql, id);

        String status = (String) request.get("status");
        if (!"ORDERED".equals(status)) {
            throw new IllegalArgumentException("Товар еще не заказан у поставщика");
        }

        String factoryNumber = (String) request.get("factory_number");
        String producerName = (String) request.get("producer");

        // Получаем или создаём категорию "Без категории"
        Long defaultCategoryId;
        String checkCategorySql = "SELECT id FROM categories WHERE name = 'Без категории'";
        try {
            defaultCategoryId = jdbcTemplate.queryForObject(checkCategorySql, Long.class);
        } catch (Exception e) {
            String insertCategorySql = "INSERT INTO categories (name) VALUES ('Без категории')";
            jdbcTemplate.update(insertCategorySql);
            defaultCategoryId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
        }

        // Получаем или создаём производителя
        Long manufacturerId;
        String checkManufacturerSql = "SELECT id FROM manufacturers WHERE name = ?";
        try {
            manufacturerId = jdbcTemplate.queryForObject(checkManufacturerSql, Long.class, producerName);
        } catch (Exception e) {
            String insertManufacturerSql = "INSERT INTO manufacturers (name) VALUES (?)";
            jdbcTemplate.update(insertManufacturerSql, producerName);
            manufacturerId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
        }

        // Получаем или создаём товар
        String checkProductSql = "SELECT id FROM products WHERE sku = ?";
        Long productId;
        try {
            productId = jdbcTemplate.queryForObject(checkProductSql, Long.class, factoryNumber);
        } catch (Exception e) {
            String insertProductSql = "INSERT INTO products (sku, name, price, category_id, manufacturer_id) VALUES (?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertProductSql,
                    factoryNumber,
                    request.get("product_name"),
                    request.get("price"),
                    defaultCategoryId,
                    manufacturerId);
            productId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
        }

        // Добавляем на склад
        stockService.addStock(productId, 1, "MAIN");

        // Обновляем статус
        String updateSql = "UPDATE external_requests SET status = 'COMPLETED' WHERE id = ?";
        jdbcTemplate.update(updateSql, id);

        // Обновляем статус заказа на DELIVERED
        Object orderIdObj = request.get("order_id");
        if (orderIdObj != null) {
            Long orderId = ((Number) orderIdObj).longValue();
            String updateOrderSql = "UPDATE sales_orders SET status = 'DELIVERED' WHERE id = ?";
            jdbcTemplate.update(updateOrderSql, orderId);
        }

        Long userId = ((Number) request.get("user_id")).longValue();

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType("EXTERNAL_REQUEST_STATUS");
        notification.setTitle("Товар поступил на склад");
        notification.setMessage("Запрошенный товар " + request.get("product_name") + " теперь доступен для заказа");
        notification.setLink("/catalog?search=" + factoryNumber);
        notification.setRead(false);
        notificationRepository.save(notification);

        return Map.of("productId", productId);
    }

    private String getStatusText(String status) {
        switch (status) {
            case "PENDING": return "Ожидает обработки";
            case "PROCESSING": return "В обработке";
            case "ORDERED": return "Заказан у поставщика";
            case "COMPLETED": return "Выполнен";
            case "REJECTED": return "Отклонен";
            default: return status;
        }
    }
}