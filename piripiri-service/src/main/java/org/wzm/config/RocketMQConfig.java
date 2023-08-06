package org.wzm.config;

import lombok.Value;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.wzm.constant.RedisConstant;
import org.wzm.constant.RocketMQConstant;
import org.wzm.domain.Danmu;
import org.wzm.domain.DanmuWrapper;
import org.wzm.domain.UserFollowing;
import org.wzm.domain.UserMoments;
import org.wzm.service.DanmuService;
import org.wzm.service.FollowService;
import org.wzm.service.UserService;
import org.wzm.service.WebSocketService;
import org.wzm.utils.DateUtil;
import org.wzm.utils.JsonUtil;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Configuration
public class RocketMQConfig {

    @Value("${rocketmq.name.server.address}")
    private String nameServerAddress;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private FollowService followService;

    @Autowired
    private UserService  userService;
    @Autowired
    private DanmuService danmuService;

    @Autowired
    private RedisTemplate<String, UserMoments> userMomentsRedisTemplate;

    //用户动态生产者
    @Bean("momentsProducer")
    public DefaultMQProducer momentsProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.MOMENTS_GROUP);
        producer.setNamesrvAddr(nameServerAddress);
        producer.start();
        return producer;
    }

    @Bean("momentsConsumer")
    public DefaultMQPushConsumer momentsConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.MOMENTS_GROUP);
        consumer.setNamesrvAddr(nameServerAddress);
        consumer.subscribe(RocketMQConstant.MOMENTS_TOPIC, "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (list, consumeConcurrentlyContext) -> {
            MessageExt messageExt = list.get(0);
            if (Objects.isNull(messageExt)) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
            String body = new String(messageExt.getBody());
            UserMoments userMoments = JsonUtil.fromJson(body, UserMoments.class);
            Long userId = userMoments.getUserId();
            List<Long> fanIds = followService.getUserFans(userMoments.getUserId())
                    .stream()
                    .map(UserFollowing::getUserId)
                    .collect(Collectors.toList());
            if (fanIds.size() < 10000) {
                fanIds.forEach(fanId -> {
                    String key = RedisConstant.SUBSCRIBED_MOMENTS + fanId;
                    userMomentsRedisTemplate.opsForZSet().add(key, userMoments, DateUtil.getCurrentTimestamp());
                });
            } else {
                List<Long> activeFanIds = fanIds.stream().filter(userService::isActive).collect(Collectors.toList());
                activeFanIds.forEach(fanId -> {
                    String key = RedisConstant.SUBSCRIBED_MOMENTS + fanId;
                    userMomentsRedisTemplate.opsForZSet().add(key, userMoments, DateUtil.getCurrentTimestamp());
                });
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        return consumer;
    }

    @Bean("wbDanmusProducer")
    public DefaultMQProducer danmusProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.DANMU_GROUP);
        producer.setNamesrvAddr(nameServerAddress);
        producer.start();
        return producer;
    }

    @Bean("wbDanmusConsumer")
    public DefaultMQPushConsumer danmusConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.DANMU_GROUP);
        consumer.setNamesrvAddr(nameServerAddress);
        consumer.subscribe(RocketMQConstant.DANMU_TOPIC, "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (list, consumeConcurrentlyContext) -> {
            MessageExt messageExt = list.get(0);
            if (Objects.isNull(messageExt)) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
            String body = new String(messageExt.getBody());
            DanmuWrapper danmuWrapper = JsonUtil.fromJson(body, DanmuWrapper.class);
            String sessionId = danmuWrapper.getSessionId();
            String danmuMsg = danmuWrapper.getDanmuMsg();
            WebSocketService wb = WebSocketService.WEBSOCKET_MAP.get(sessionId);
            if (wb.getSession().isOpen()) {
                try {
                    wb.sendMsg(danmuMsg);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        return consumer;
    }

    @Bean("dbDanmuConsumer")
    public DefaultMQProducer dbDanmuProducer() throws MQClientException {
        DefaultMQProducer producer = new DefaultMQProducer(RocketMQConstant.MOMENTS_GROUP);
        producer.setNamesrvAddr(nameServerAddress);
        producer.start();
        return producer;
    }

    @Bean("dbDanmusConsumer")
    public DefaultMQPushConsumer dbDanmuConsumer() throws MQClientException {
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(RocketMQConstant.MOMENTS_GROUP);
        consumer.setNamesrvAddr(nameServerAddress);
        consumer.subscribe(RocketMQConstant.DANMU_DB_TOPIC, "*");
        consumer.registerMessageListener((MessageListenerConcurrently) (list, consumeConcurrentlyContext) -> {
            MessageExt messageExt = list.get(0);
            if (Objects.isNull(messageExt)) {
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
            String body = new String(messageExt.getBody());
            Danmu danmu = JsonUtil.fromJson(body, Danmu.class);
            danmuService.save(danmu);
            return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
        });
        consumer.start();
        return consumer;
    }
}