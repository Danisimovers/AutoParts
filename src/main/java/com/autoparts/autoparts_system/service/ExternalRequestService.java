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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
        return externalRequestRepository.findAll();
    }

    @Transactional
    public void updateStatus(Long id, String status) {
        Long orderId = externalRequestRepository.findOrderIdById(id);
        externalRequestRepository.updateStatus(id, status);

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

        externalRequestRepository.updateStatus(id, "ORDERED");

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
        try {
            System.out.println("=== НАЧАЛО addToStockFromRequest для id: " + id);

            ExternalRequest request = externalRequestRepository.findById(id);
            if (request == null) {
                System.err.println("Запрос не найден с id: " + id);
                throw new IllegalArgumentException("Запрос не найден");
            }
            System.out.println("Найден запрос: " + request.getProductName() + ", статус: " + request.getStatus());

            String status = request.getStatus();
            if (!"ORDERED".equals(status)) {
                System.err.println("Неверный статус. Ожидается ORDERED, текущий: " + status);
                throw new IllegalArgumentException("Товар еще не заказан у поставщика. Текущий статус: " + status);
            }

            String factoryNumber = request.getFactoryNumber();
            String producerName = request.getProducer();
            System.out.println("Артикул: " + factoryNumber + ", Производитель: " + producerName);

            // Получаем или создаём категорию "Без категории"
            Long defaultCategoryId;
            String checkCategorySql = "SELECT id FROM categories WHERE name = 'Без категории'";
            try {
                defaultCategoryId = jdbcTemplate.queryForObject(checkCategorySql, Long.class);
                System.out.println("Категория найдена: " + defaultCategoryId);
            } catch (Exception e) {
                System.out.println("Категория не найдена, создаём...");
                String insertCategorySql = "INSERT INTO categories (name) VALUES ('Без категории')";
                jdbcTemplate.update(insertCategorySql);
                defaultCategoryId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
                System.out.println("Категория создана: " + defaultCategoryId);
            }

            // Получаем или создаём производителя
            Long manufacturerId;
            String checkManufacturerSql = "SELECT id FROM manufacturers WHERE name = ?";
            try {
                manufacturerId = jdbcTemplate.queryForObject(checkManufacturerSql, Long.class, producerName);
                System.out.println("Производитель найден: " + manufacturerId);
            } catch (Exception e) {
                System.out.println("Производитель не найден, создаём...");
                String insertManufacturerSql = "INSERT INTO manufacturers (name) VALUES (?)";
                jdbcTemplate.update(insertManufacturerSql, producerName);
                manufacturerId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
                System.out.println("Производитель создан: " + manufacturerId);
            }

            // Получаем или создаём товар
            String checkProductSql = "SELECT id FROM products WHERE sku = ?";
            Long productId;
            try {
                productId = jdbcTemplate.queryForObject(checkProductSql, Long.class, factoryNumber);
                System.out.println("Товар найден: " + productId);
            } catch (Exception e) {
                System.out.println("Товар не найден, создаём...");
                String insertProductSql = "INSERT INTO products (sku, name, description, price, oem_code, category_id, manufacturer_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
                jdbcTemplate.update(insertProductSql,
                        factoryNumber,
                        request.getProductName(),
                        "Товар от поставщика: " + request.getProductName(),
                        request.getPrice(),
                        factoryNumber,
                        defaultCategoryId,
                        manufacturerId);
                productId = jdbcTemplate.queryForObject("SELECT LASTVAL()", Long.class);
                System.out.println("Товар создан: " + productId);
            }

            // Добавляем на склад (используем 1L как Long)
            int quantity = 1;
            System.out.println("Добавляем на склад: productId=" + productId + ", quantity=" + quantity);
            stockService.addStock(productId, quantity, 1L);  // ← ИСПРАВЛЕНО: 1L вместо 1
            System.out.println("На склад добавлено");

            // Обновляем статус external_request
            externalRequestRepository.updateStatus(id, "COMPLETED");
            System.out.println("Статус external_request обновлён на COMPLETED");

            // Обновляем статус заказа
            Long orderId = request.getOrderId();
            if (orderId != null) {
                String updateOrderSql = "UPDATE sales_orders SET status = 'DELIVERED' WHERE id = ?";
                jdbcTemplate.update(updateOrderSql, orderId);
                System.out.println("Статус заказа " + orderId + " обновлён на DELIVERED");
            }

            Notification notification = new Notification();
            notification.setUserId(request.getUserId());
            notification.setType("EXTERNAL_REQUEST_STATUS");
            notification.setTitle("Товар поступил на склад");
            notification.setMessage("Запрошенный товар " + request.getProductName() + " теперь доступен для заказа");
            notification.setLink("/catalog?search=" + factoryNumber);
            notification.setRead(false);
            notificationRepository.save(notification);
            System.out.println("Уведомление отправлено пользователю " + request.getUserId());

            System.out.println("=== УСПЕШНОЕ ЗАВЕРШЕНИЕ ===");
            return Map.of("productId", productId, "quantity", quantity);

        } catch (Exception e) {
            System.err.println("=== ОШИБКА В addToStockFromRequest ===");
            e.printStackTrace();
            throw new RuntimeException("Ошибка при добавлении товара на склад: " + e.getMessage(), e);
        }
    }

    // Пагинация для запросов
    public Page<ExternalRequest> getAllExternalRequests(Pageable pageable) {
        return externalRequestRepository.findAll(pageable);
    }

    // Поиск запросов с пагинацией
    public Page<ExternalRequest> searchExternalRequests(String search, Pageable pageable) {
        return externalRequestRepository.search(search, pageable);
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