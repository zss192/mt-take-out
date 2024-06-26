package com.sky.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.CustomExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {
    /**
     * 队列
     */
    public static final String DIRECT_QUEUE = "queue.delayed";

    /**
     * 延迟交换机
     */
    public static final String DELAYED_EXCHANGE = "exchange.delayed";

    /**
     * 绑定的routing key
     */
    public static final String ROUTING_KEY = "routingKey.bind";

    @Bean
    public Queue directQueue() {
        return new Queue(DIRECT_QUEUE, true);
    }

    /**
     * 定义延迟交换机
     * 交换机的类型为 x-delayed-message
     */
    @Bean
    public CustomExchange delayedExchange() {
        Map<String, Object> map = new HashMap<>();
        map.put("x-delayed-type", "direct");
        return new CustomExchange(DELAYED_EXCHANGE, "x-delayed-message", true, false, map);
    }

    @Bean
    public Binding delayOrderBinding() {
        return BindingBuilder.bind(directQueue()).to(delayedExchange()).with(ROUTING_KEY).noargs();
    }
}
