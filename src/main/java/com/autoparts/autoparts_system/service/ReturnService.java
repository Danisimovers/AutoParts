package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Notification;
import com.autoparts.autoparts_system.model.OrderItem;
import com.autoparts.autoparts_system.model.Return;
import com.autoparts.autoparts_system.model.Product;
import com.autoparts.autoparts_system.repository.NotificationRepository;
import com.autoparts.autoparts_system.repository.OrderItemRepository;
import com.autoparts.autoparts_system.repository.ReturnRepository;
import com.autoparts.autoparts_system.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Service
public class ReturnService {

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private NotificationRepository notificationRepository;

    // Получить все заявки (для админа/менеджера)
    public List<Return> getAllReturns() {
        return returnRepository.findAll();
    }

    // Получить заявки пользователя
    public List<Return> getUserReturns(Long userId) {
        return returnRepository.findByUserId(userId);
    }

    // Получить заявку по ID
    public Return getReturnById(Long id) {
        return returnRepository.findById(id);
    }

    public Page<Return> getAllReturns(Pageable pageable) {
        return returnRepository.findAll(pageable);
    }

    public Page<Return> searchReturns(String search, Pageable pageable) {
        return returnRepository.search(search, pageable);
    }

    public Page<Return> getReturnsByStatus(String status, Pageable pageable) {
        return returnRepository.findByStatus(status, pageable);
    }

    public Page<Return> searchReturnsWithStatus(String search, String status, Pageable pageable) {
        return returnRepository.searchWithStatus(search, status, pageable);
    }

    // Создать заявку на возврат
    @Transactional
    public void createReturn(Long orderItemId, Long userId, String reason) {
        // Проверяем, существует ли позиция заказа
        OrderItem orderItem = orderItemRepository.findById(orderItemId);
        if (orderItem == null) {
            throw new IllegalArgumentException("Позиция заказа не найдена");
        }

        // Проверяем, не было ли уже возврата по этой позиции
        List<Return> existingReturns = returnRepository.findByOrderItemId(orderItemId);
        if (!existingReturns.isEmpty()) {
            throw new IllegalArgumentException("Возврат по этой позиции уже оформлен");
        }

        Return returnObj = new Return();
        returnObj.setOrderItemId(orderItemId);
        returnObj.setUserId(userId);
        returnObj.setReason(reason);
        returnObj.setStatus("PENDING");

        returnRepository.save(returnObj);
    }



    // Одобрить возврат
    @Transactional
    public void approveReturn(Long returnId) {
        Return returnObj = returnRepository.findById(returnId);
        if (returnObj == null) {
            throw new IllegalArgumentException("Заявка на возврат не найдена");
        }

        if (!"PENDING".equals(returnObj.getStatus())) {
            throw new IllegalArgumentException("Заявка уже обработана");
        }

        // Получаем позицию заказа
        OrderItem orderItem = orderItemRepository.findById(returnObj.getOrderItemId());
        if (orderItem == null) {
            throw new IllegalArgumentException("Позиция заказа не найдена");
        }

        // Возвращаем товар на склад
        Product product = productService.getProductById(orderItem.getProductId());
        if (product != null) {
            stockService.addStock(product.getId(), orderItem.getQuantity(), "MAIN");
        }

        // Обновляем статус возврата
        returnRepository.updateStatus(returnId, "APPROVED");

        // Уведомление пользователю
        Notification notification = new Notification();
        notification.setUserId(returnObj.getUserId());
        notification.setType("RETURN_STATUS");
        notification.setTitle("Заявка на возврат одобрена");
        notification.setMessage("Ваша заявка на возврат одобрена. Деньги будут возвращены в ближайшее время.");
        notification.setLink("/profile?tab=returns");
        notification.setRead(false);
        notificationRepository.save(notification);
    }

    // Отклонить возврат
    @Transactional
    public void rejectReturn(Long returnId, String rejectReason) {
        Return returnObj = returnRepository.findById(returnId);
        if (returnObj == null) {
            throw new IllegalArgumentException("Заявка на возврат не найдена");
        }

        if (!"PENDING".equals(returnObj.getStatus())) {
            throw new IllegalArgumentException("Заявка уже обработана");
        }

        returnRepository.updateStatus(returnId, "REJECTED");

        // Уведомление пользователю
        Notification notification = new Notification();
        notification.setUserId(returnObj.getUserId());
        notification.setType("RETURN_STATUS");
        notification.setTitle("Заявка на возврат отклонена");
        notification.setMessage("Ваша заявка на возврат отклонена. Причина: " + rejectReason);
        notification.setLink("/profile?tab=returns");
        notification.setRead(false);
        notificationRepository.save(notification);
    }
}