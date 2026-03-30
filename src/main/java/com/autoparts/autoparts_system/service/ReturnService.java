package com.autoparts.autoparts_system.service;

import com.autoparts.autoparts_system.model.Return;
import com.autoparts.autoparts_system.repository.OrderItemRepository;
import com.autoparts.autoparts_system.repository.ReturnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReturnService {

    @Autowired
    private ReturnRepository returnRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private StockService stockService;

    public List<Return> getAllReturns() {
        return returnRepository.findAll();
    }

    public Return getReturnById(Long id) {
        return returnRepository.findById(id);
    }

    public List<Return> getReturnsByOrderItemId(Long orderItemId) {
        return returnRepository.findByOrderItemId(orderItemId);
    }

    @Transactional
    public Return createReturnRequest(Long orderItemId, String reason) {
        Return returnObj = new Return();
        returnObj.setOrderItemId(orderItemId);
        returnObj.setReason(reason);
        returnObj.setStatus("REQUESTED");
        returnObj.setCreatedAt(LocalDateTime.now());

        returnRepository.save(returnObj);
        return returnObj;
    }

    @Transactional
    public Return approveReturn(Long returnId) {
        Return returnObj = returnRepository.findById(returnId);
        if (returnObj == null) {
            throw new IllegalArgumentException("Запрос на возврат не найден");
        }

        returnObj.setStatus("APPROVED");
        returnRepository.updateStatus(returnId, "APPROVED");

        // Возвращаем товар на склад
        // Нужно получить productId из order_item
        // Для упрощения оставляем заглушку

        return returnObj;
    }

    @Transactional
    public Return rejectReturn(Long returnId, String reason) {
        Return returnObj = returnRepository.findById(returnId);
        if (returnObj == null) {
            throw new IllegalArgumentException("Запрос на возврат не найден");
        }

        returnObj.setStatus("REJECTED");
        returnRepository.updateStatus(returnId, "REJECTED");

        return returnObj;
    }

    public void deleteReturn(Long id) {
        returnRepository.deleteById(id);
    }
}