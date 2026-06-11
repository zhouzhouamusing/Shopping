package com.shopping.service;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 订单状态变更事件队列服务
 *
 * 功能：
 * 1. 异步消息队列：解耦订单状态变更通知（内存队列模拟MQ）
 * 2. 事件发布/消费模式：状态变更时发布事件，消费者异步处理
 * 3. 监控告警：追踪失败事件、死信队列、处理延迟
 * 4. 重试机制：失败事件自动重试3次
 */
@Slf4j
@Service
public class OrderEventQueueService {

    // 事件队列（生产环境替换为 RabbitMQ/Kafka）
    private final BlockingQueue<OrderStatusEvent> eventQueue = new LinkedBlockingQueue<>(10000);

    // 死信队列：重试多次仍失败的事件
    private final BlockingQueue<OrderStatusEvent> deadLetterQueue = new LinkedBlockingQueue<>(1000);

    // 监控指标
    private final AtomicLong totalPublished = new AtomicLong(0);
    private final AtomicLong totalConsumed = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalRetried = new AtomicLong(0);
    private final ConcurrentHashMap<String, Long> statusChangeCounter = new ConcurrentHashMap<>();

    // 告警阈值
    private static final int QUEUE_SIZE_ALERT_THRESHOLD = 5000;
    private static final int DEAD_LETTER_ALERT_THRESHOLD = 10;
    private static final int MAX_RETRY_COUNT = 3;

    /**
     * 发布订单状态变更事件
     */
    public void publishStatusChangeEvent(String orderNo, Integer fromStatus, Integer toStatus,
                                          String operator, String reason) {
        OrderStatusEvent event = new OrderStatusEvent();
        event.setOrderNo(orderNo);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setOperator(operator);
        event.setReason(reason);
        event.setTimestamp(LocalDateTime.now());
        event.setRetryCount(0);

        boolean offered = eventQueue.offer(event);
        if (offered) {
            totalPublished.incrementAndGet();
            String key = fromStatus + "->" + toStatus;
            statusChangeCounter.merge(key, 1L, Long::sum);
            log.info("[事件队列] 发布状态变更事件: orderNo={}, {}→{}, operator={}",
                    orderNo, getStatusName(fromStatus), getStatusName(toStatus), operator);
        } else {
            totalFailed.incrementAndGet();
            log.error("[事件队列] 队列已满，事件丢失: orderNo={}, {}→{}",
                    orderNo, fromStatus, toStatus);
            triggerAlert("EVENT_QUEUE_FULL", "事件队列已满，状态变更事件丢失: " + orderNo);
        }
    }

    /**
     * 消费事件队列（定时轮询）
     */
    @Scheduled(fixedDelay = 2000)
    public void consumeEvents() {
        List<OrderStatusEvent> batch = new ArrayList<>();
        eventQueue.drainTo(batch, 50);

        for (OrderStatusEvent event : batch) {
            try {
                processEvent(event);
                totalConsumed.incrementAndGet();
            } catch (Exception e) {
                handleEventFailure(event, e);
            }
        }
    }

    /**
     * 监控告警检查
     */
    @Scheduled(fixedRate = 30000)
    public void monitorHealth() {
        int queueSize = eventQueue.size();
        int deadLetterSize = deadLetterQueue.size();

        if (queueSize > QUEUE_SIZE_ALERT_THRESHOLD) {
            triggerAlert("QUEUE_BACKLOG",
                    String.format("事件队列积压告警: 当前%d条, 阈值%d条", queueSize, QUEUE_SIZE_ALERT_THRESHOLD));
        }

        if (deadLetterSize > DEAD_LETTER_ALERT_THRESHOLD) {
            triggerAlert("DEAD_LETTER_OVERFLOW",
                    String.format("死信队列告警: 当前%d条, 阈值%d条", deadLetterSize, DEAD_LETTER_ALERT_THRESHOLD));
        }

        long failed = totalFailed.get();
        long total = totalPublished.get();
        if (total > 100 && (double) failed / total > 0.1) {
            triggerAlert("HIGH_FAILURE_RATE",
                    String.format("事件处理失败率过高: %.1f%% (%d/%d)", (double) failed / total * 100, failed, total));
        }
    }

    /**
     * 获取监控数据
     */
    public MonitoringData getMonitoringData() {
        MonitoringData data = new MonitoringData();
        data.setQueueSize(eventQueue.size());
        data.setDeadLetterSize(deadLetterQueue.size());
        data.setTotalPublished(totalPublished.get());
        data.setTotalConsumed(totalConsumed.get());
        data.setTotalFailed(totalFailed.get());
        data.setTotalRetried(totalRetried.get());
        data.setStatusChangeStats(new ConcurrentHashMap<>(statusChangeCounter));
        return data;
    }

    /**
     * 获取死信队列中的事件
     */
    public List<OrderStatusEvent> getDeadLetterEvents(int limit) {
        List<OrderStatusEvent> events = new ArrayList<>();
        int count = Math.min(limit, deadLetterQueue.size());
        for (int i = 0; i < count; i++) {
            OrderStatusEvent event = deadLetterQueue.peek();
            if (event != null) events.add(event);
        }
        return events;
    }

    /**
     * 手动重试死信队列事件
     */
    public int retryDeadLetterEvents() {
        List<OrderStatusEvent> events = new ArrayList<>();
        deadLetterQueue.drainTo(events);
        int retried = 0;
        for (OrderStatusEvent event : events) {
            event.setRetryCount(0);
            if (eventQueue.offer(event)) {
                retried++;
                totalRetried.incrementAndGet();
            } else {
                deadLetterQueue.offer(event);
            }
        }
        log.info("[事件队列] 手动重试死信队列: {}条事件重新入队", retried);
        return retried;
    }

    // ===== 内部方法 =====

    private void processEvent(OrderStatusEvent event) {
        // 模拟事件处理：通知相关系统
        log.info("[事件消费] 处理状态变更: orderNo={}, {}→{}, operator={}, timestamp={}",
                event.getOrderNo(),
                getStatusName(event.getFromStatus()),
                getStatusName(event.getToStatus()),
                event.getOperator(),
                event.getTimestamp());

        // 模拟通知下游系统
        notifyInventorySystem(event);
        notifyNotificationSystem(event);
        notifyAnalyticsSystem(event);
    }

    private void notifyInventorySystem(OrderStatusEvent event) {
        if (event.getToStatus() == 6 || event.getToStatus() == 5) {
            log.info("[库存通知] 订单取消/退款，触发库存恢复: orderNo={}", event.getOrderNo());
        }
    }

    private void notifyNotificationSystem(OrderStatusEvent event) {
        switch (event.getToStatus()) {
            case 1 -> log.info("[消息通知] 通知用户支付成功: orderNo={}", event.getOrderNo());
            case 2 -> log.info("[消息通知] 通知用户已发货: orderNo={}", event.getOrderNo());
            case 6 -> log.info("[消息通知] 通知用户退款成功: orderNo={}", event.getOrderNo());
            case 7 -> log.info("[消息通知] 通知用户订单已过期: orderNo={}", event.getOrderNo());
            case 100 -> log.info("[消息通知] 通知用户会员等级晋升: orderNo={}, reason={}",
                    event.getOrderNo(), event.getReason());
        }
    }

    private void notifyAnalyticsSystem(OrderStatusEvent event) {
        log.debug("[数据分析] 上报状态变更事件: orderNo={}, transition={}→{}",
                event.getOrderNo(), event.getFromStatus(), event.getToStatus());
    }

    private void handleEventFailure(OrderStatusEvent event, Exception e) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setLastError(e.getMessage());

        if (event.getRetryCount() >= MAX_RETRY_COUNT) {
            deadLetterQueue.offer(event);
            totalFailed.incrementAndGet();
            log.error("[事件队列] 事件处理失败已达最大重试次数，移入死信队列: orderNo={}, error={}",
                    event.getOrderNo(), e.getMessage());
            triggerAlert("EVENT_PROCESSING_FAILED",
                    "事件处理失败: orderNo=" + event.getOrderNo() + ", error=" + e.getMessage());
        } else {
            eventQueue.offer(event);
            totalRetried.incrementAndGet();
            log.warn("[事件队列] 事件处理失败，重新入队(第{}次): orderNo={}, error={}",
                    event.getRetryCount(), event.getOrderNo(), e.getMessage());
        }
    }

    private void triggerAlert(String alertType, String message) {
        log.error("[监控告警] type={}, message={}", alertType, message);
        // 生产环境：发送到告警平台（钉钉/企微/PagerDuty等）
    }

    private String getStatusName(Integer status) {
        if (status == null) return "null";
        return switch (status) {
            case 0 -> "待付款";
            case 1 -> "待发货";
            case 2 -> "待收货";
            case 3 -> "待评价";
            case 4 -> "已完成";
            case 5 -> "已取消";
            case 6 -> "已退款";
            case 7 -> "已过期";
            default -> "未知(" + status + ")";
        };
    }

    // ===== 数据模型 =====

    @Data
    public static class OrderStatusEvent {
        private String orderNo;
        private Integer fromStatus;
        private Integer toStatus;
        private String operator;
        private String reason;
        private LocalDateTime timestamp;
        private int retryCount;
        private String lastError;
    }

    @Data
    public static class MonitoringData {
        private int queueSize;
        private int deadLetterSize;
        private long totalPublished;
        private long totalConsumed;
        private long totalFailed;
        private long totalRetried;
        private ConcurrentHashMap<String, Long> statusChangeStats;
    }
}
