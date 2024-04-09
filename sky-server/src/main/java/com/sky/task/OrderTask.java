package com.sky.task;

import com.sky.config.RabbitMQConfig;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类，定时处理订单状态
 */
@Component
@Slf4j
public class OrderTask {
    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理超时订单方法
     */
//    @Scheduled(cron = "0 * * * * ?") // 每分钟触发一次
//    public void processTimeoutOrder() {
//        log.info("处理超时订单：{}", LocalDateTime.now());
//        LocalDateTime time = LocalDateTime.now().plusMinutes(-15);
//
//        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.PENDING_PAYMENT, time);
//
//        if (ordersList != null && !ordersList.isEmpty()) {
//            for (Orders orders : ordersList) {
//                orders.setStatus(Orders.CANCELLED);
//                orders.setCancelReason("订单超时，自动取消");
//                orders.setCancelTime(LocalDateTime.now());
//                orderMapper.update(orders);
//            }
//        }
//    }
    @RabbitListener(queues = RabbitMQConfig.DIRECT_QUEUE)
    public void processTimeoutOrder(Long orderId) {
        //通过从队列中获取的订单号，查询订单
        Orders orders = orderMapper.getById(orderId);

        if (orders.getStatus().equals(Orders.PENDING_PAYMENT)) {
            log.info("订单{}超时，已自动取消", orderId);
            orders.setStatus(Orders.CANCELLED);
            orders.setCancelReason("订单超时，自动取消");
            orders.setCancelTime(LocalDateTime.now());
            orderMapper.update(orders);
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ?") // 每天凌晨1点触发一次
    public void processDeliveryOrder() {
        log.info("处理一直处于派送中的订单：{}", LocalDateTime.now());
        LocalDateTime time = LocalDateTime.now().plusMinutes(-60);
        List<Orders> ordersList = orderMapper.getByStatusAndOrderTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if (ordersList != null && !ordersList.isEmpty()) {
            for (Orders orders : ordersList) {
                orders.setStatus(Orders.COMPLETED);
                orderMapper.update(orders);
            }
        }
    }
}
