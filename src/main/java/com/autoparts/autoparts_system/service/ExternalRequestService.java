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
    private JdbcTemplate jdbcTemplate;  // Оставляем для сложных запросов к другим таблицам

    @Autowired
    private ExternalRequestRepository externalRequestRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private StockService stockService;

    // Теперь используем репозиторий
    public List<ExternalRequest> getAllExternalRequests() {
        return externalRequestRepository.findAll();
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        // Используем репозиторий для получения order_id
        Long orderId = externalRequestRepository.findOrderIdById(id);

        // Используем репозиторий для обновления статуса
        externalRequestRepository.updateStatus(id, status);

        // Синхронизируем статус заказа (это бизнес-логика, остаётся в сервисе)
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

        // Уведомление пользователю (бизнес-логика)
        Long userId = externalRequestRepository.findUserIdById(id);

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
        ExternalRequest request = externalRequestRepository.findById(id);
        if (request == null) {
            throw new IllegalArgumentException("Запрос не найден");
        }

        String status = request.getStatus();
        if (!"PROCESSING".equals(status) && !"PENDING".equals(status)) {
            throw new IllegalArgumentException("Заказ уже обработан");
        }

        Long supplierId = request.getSupplierId();
        if (supplierId == null) {
            String findSupplierSql = "SELECT id FROM suppliers WHERE name = ?";
            supplierId = jdbcTemplate.queryForObject(findSupplierSql, Long.class, request.getSupplierName());
        }

        String insertOrderSql = "INSERT INTO purchase_orders (supplier_id, date, status, total) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(insertOrderSql, supplierId, LocalDate.now(), "ORDERED", request.getPrice());

        // Обновляем статус external_request через репозиторий
        externalRequestRepository.updateStatus(id, "ORDERED");

        // Обновляем статус заказа
        Long orderId = request.getOrderId();
        if (orderId != null) {
            String updateOrderSql = "UPDATE sales_orders SET status = 'PENDING_SUPPLIER' WHERE id = ?";
            jdbcTemplate.update(updateOrderSql, orderId);
        }

        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType("EXTERNAL_REQUEST_STATUS");
        notification.setTitle("Товар заказан у поставщика");
        notification.setMessage("Ваш запрос на товар " + request.getProductName() + " передан в заказ поставщику");
        notification.setLink("/profile?tab=external-requests");
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    @Transactional
    public Map<String, Object> addToStockFromRequest(Long id) {
        ExternalRequest request = externalRequestRepository.findById(id);
        if (request == null) {
            throw new IllegalArgumentException("Запрос не найден");
        }

        String status = request.getStatus();
        if (!"ORDERED".equals(status)) {
            throw new IllegalArgumentException("Товар еще не заказан у поставщика");
        }

        String factoryNumber = request.getFactoryNumber();
        String producerName = request.getProducer();

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
                    request.getProductName(),
                    request.getPrice(),
                    defaultCategoryId,
                    manufacturerId);
            productId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
        }

        // Добавляем на склад
        stockService.addStock(productId, 1, "MAIN");

        // Обновляем статус external_request через репозиторий
        externalRequestRepository.updateStatus(id, "COMPLETED");

        // Обновляем статус заказа на DELIVERED
        Long orderId = request.getOrderId();
        if (orderId != null) {
            String updateOrderSql = "UPDATE sales_orders SET status = 'DELIVERED' WHERE id = ?";
            jdbcTemplate.update(updateOrderSql, orderId);
        }

        Notification notification = new Notification();
        notification.setUserId(request.getUserId());
        notification.setType("EXTERNAL_REQUEST_STATUS");
        notification.setTitle("Товар поступил на склад");
        notification.setMessage("Запрошенный товар " + request.getProductName() + " теперь доступен для заказа");
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