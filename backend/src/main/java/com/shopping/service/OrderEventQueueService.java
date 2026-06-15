package com.shopping.service;

import com.shopping.entity.OrderEvent;
import com.shopping.repository.OrderEventRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
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
@RequiredArgsConstructor
public class OrderEventQueueService {

    private final OrderEventRepository eventRepository;

    // 监控指标
    private final AtomicLong totalPublished = new AtomicLong(0);
    private final AtomicLong totalConsumed = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalRetried = new AtomicLong(0);
    private final ConcurrentHashMap<String, Long> statusChangeCounter = new ConcurrentHashMap<>();

    private static final int MAX_RETRY_COUNT = 3;
    private static final int BATCH_SIZE = 50;

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup() {
        long pending = eventRepository.countByStatus("PENDING");
        if (pending > 0) {
            log.info("[事件队列] 启动恢复: 发现{}条未处理事件，将在下次轮询时处理", pending);
        }
    }

    /**
     * 发布订单状态变更事件（持久化到数据库）
     */
    @Transactional
    public void publishStatusChangeEvent(String orderNo, Integer fromStatus, Integer toStatus,
                                          String operator, String reason) {
        OrderEvent event = new OrderEvent();
        event.setOrderNo(orderNo);
        event.setFromStatus(fromStatus);
        event.setToStatus(toStatus);
        event.setOperator(operator);
        event.setReason(reason);
        event.setRetryCount(0);
        event.setStatus("PENDING");
        eventRepository.save(event);

        totalPublished.incrementAndGet();
        String key = fromStatus + "->" + toStatus;
        statusChangeCounter.merge(key, 1L, Long::sum);
        log.info("[事件队列] 发布状态变更事件: orderNo={}, {}→{}, operator={}",
                orderNo, getStatusName(fromStatus), getStatusName(toStatus), operator);
    }

    /**
     * 消费事件队列（定时轮询数据库）
     */
    @Scheduled(fixedDelay = 2000)
    @Transactional
    public void consumeEvents() {
        List<OrderEvent> batch = eventRepository.findByStatusOrderByCreatedAtAsc("PENDING", PageRequest.of(0, BATCH_SIZE));

        for (OrderEvent event : batch) {
            try {
                processEvent(event);
                event.setStatus("PROCESSED");
                event.setProcessedAt(LocalDateTime.now());
                eventRepository.save(event);
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
        long pendingCount = eventRepository.countByStatus("PENDING");
        long deadCount = eventRepository.countByStatus("DEAD");

        if (pendingCount > 5000) {
            triggerAlert("QUEUE_BACKLOG",
                    String.format("事件队列积压告警: 当前%d条待处理", pendingCount));
        }
        if (deadCount > 10) {
            triggerAlert("DEAD_LETTER_OVERFLOW",
                    String.format("死信队列告警: 当前%d条", deadCount));
        }
    }

    public MonitoringData getMonitoringData() {
        MonitoringData data = new MonitoringData();
        data.setQueueSize((int) eventRepository.countByStatus("PENDING"));
        data.setDeadLetterSize((int) eventRepository.countByStatus("DEAD"));
        data.setTotalPublished(totalPublished.get());
        data.setTotalConsumed(totalConsumed.get());
        data.setTotalFailed(totalFailed.get());
        data.setTotalRetried(totalRetried.get());
        data.setStatusChangeStats(new ConcurrentHashMap<>(statusChangeCounter));
        return data;
    }

    public List<OrderStatusEvent> getDeadLetterEvents(int limit) {
        List<OrderEvent> deadEvents = eventRepository.findByStatusOrderByCreatedAtAsc("DEAD", PageRequest.of(0, limit));
        List<OrderStatusEvent> result = new ArrayList<>();
        for (OrderEvent e : deadEvents) {
            OrderStatusEvent dto = new OrderStatusEvent();
            dto.setOrderNo(e.getOrderNo());
            dto.setFromStatus(e.getFromStatus());
            dto.setToStatus(e.getToStatus());
            dto.setOperator(e.getOperator());
            dto.setReason(e.getReason());
            dto.setTimestamp(e.getCreatedAt());
            dto.setRetryCount(e.getRetryCount());
            dto.setLastError(e.getLastError());
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public int retryDeadLetterEvents() {
        List<OrderEvent> deadEvents = eventRepository.findByStatusOrderByCreatedAtAsc("DEAD");
        int retried = 0;
        for (OrderEvent event : deadEvents) {
            event.setRetryCount(0);
            event.setStatus("PENDING");
            event.setLastError(null);
            eventRepository.save(event);
            retried++;
            totalRetried.incrementAndGet();
        }
        log.info("[事件队列] 手动重试死信队列: {}条事件重新入队", retried);
        return retried;
    }

    // ===== 内部方法 =====

    private void processEvent(OrderEvent event) {
        log.info("[事件消费] 处理状态变更: orderNo={}, {}→{}, operator={}, timestamp={}",
                event.getOrderNo(),
                getStatusName(event.getFromStatus()),
                getStatusName(event.getToStatus()),
                event.getOperator(),
                event.getCreatedAt());

        notifyInventorySystem(event);
        notifyNotificationSystem(event);
        notifyAnalyticsSystem(event);
    }

    private void notifyInventorySystem(OrderEvent event) {
        if (event.getToStatus() == 6 || event.getToStatus() == 5) {
            log.info("[库存通知] 订单取消/退款，触发库存恢复: orderNo={}", event.getOrderNo());
        }
    }

    private void notifyNotificationSystem(OrderEvent event) {
        switch (event.getToStatus()) {
            case 1 -> log.info("[消息通知] 通知用户支付成功: orderNo={}", event.getOrderNo());
            case 2 -> log.info("[消息通知] 通知用户已发货: orderNo={}", event.getOrderNo());
            case 6 -> log.info("[消息通知] 通知用户退款成功: orderNo={}", event.getOrderNo());
            case 7 -> log.info("[消息通知] 通知用户订单已过期: orderNo={}", event.getOrderNo());
            case 100 -> log.info("[消息通知] 通知用户会员等级晋升: orderNo={}, reason={}",
                    event.getOrderNo(), event.getReason());
        }
    }

    private void notifyAnalyticsSystem(OrderEvent event) {
        log.debug("[数据分析] 上报状态变更事件: orderNo={}, transition={}→{}",
                event.getOrderNo(), event.getFromStatus(), event.getToStatus());
    }

    private void handleEventFailure(OrderEvent event, Exception e) {
        event.setRetryCount(event.getRetryCount() + 1);
        event.setLastError(e.getMessage());

        if (event.getRetryCount() >= MAX_RETRY_COUNT) {
            event.setStatus("DEAD");
            totalFailed.incrementAndGet();
            log.error("[事件队列] 事件处理失败已达最大重试次数，移入死信: orderNo={}, error={}",
                    event.getOrderNo(), e.getMessage());
            triggerAlert("EVENT_PROCESSING_FAILED",
                    "事件处理失败: orderNo=" + event.getOrderNo() + ", error=" + e.getMessage());
        } else {
            totalRetried.incrementAndGet();
            log.warn("[事件队列] 事件处理失败，将重试(第{}次): orderNo={}, error={}",
                    event.getRetryCount(), event.getOrderNo(), e.getMessage());
        }
        eventRepository.save(event);
    }

    private void triggerAlert(String alertType, String message) {
        log.error("[监控告警] type={}, message={}", alertType, message);
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
