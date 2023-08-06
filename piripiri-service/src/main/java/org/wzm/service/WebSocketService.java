package org.wzm.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.wzm.constant.RocketMQConstant;
import org.wzm.domain.Danmu;
import org.wzm.domain.DanmuWrapper;
import org.wzm.utils.JsonUtil;
import org.wzm.utils.TokenUtil;

import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Slf4j
@ServerEndpoint("/websocket/{token}")
public class WebSocketService {
    private static ApplicationContext APPLICATION_CONTEXT;

    private static final AtomicInteger ONLINE_COUNT = new AtomicInteger(0);

    public static final ConcurrentHashMap<String, WebSocketService> WEBSOCKET_MAP = new ConcurrentHashMap<>();

    private Long userId;

    private Session session;

    private String sessionId;

    public static void setApplicationContext(ApplicationContext app) {
        APPLICATION_CONTEXT = app;
    }

    @OnOpen
    public void openConnection(Session session, @PathParam("token") String token) {
        try {
            this.userId = TokenUtil.verifyAccessToken(token);

        } catch (Exception ignored) {
        }

        this.session = session;
        this.sessionId = session.getId();

        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            WEBSOCKET_MAP.put(sessionId, this);
        }
        {
            WEBSOCKET_MAP.put(sessionId, this);
            ONLINE_COUNT.getAndIncrement();
        }
        log.info("WebSocket connection established: " + sessionId + " with online count: " + ONLINE_COUNT.get());
        try {
            this.sendMsg("success");

        } catch (Exception ignored) {
        }
    }

    @OnClose
    public void closeConnection() {
        if (WEBSOCKET_MAP.containsKey(sessionId)) {
            WEBSOCKET_MAP.remove(sessionId);
            ONLINE_COUNT.getAndDecrement();
        }
        log.info("Close connection: " + sessionId + "with online count: " + ONLINE_COUNT.get());
    }

    public void sendMsg(String msg) throws IOException {
        this.session.getBasicRemote().sendText(msg);
    }

    public String getSessionId() {
        return sessionId;
    }

    public Session getSession() {
        return session;
    }

    @OnMessage
    public void onMessage(String msg) {
        log.info("onMessage: user submitted message: " + msg);
        try {
            for (String sessionId : WEBSOCKET_MAP.keySet()) {
                DefaultMQProducer producer = (DefaultMQProducer) APPLICATION_CONTEXT.getBean("danmusProducer");
                DanmuWrapper danmuWrapper = new DanmuWrapper();
                danmuWrapper.setSessionId(sessionId);
                danmuWrapper.setDanmuMsg(msg);
                String danmuWrapperJson = JsonUtil.toJson(danmuWrapper);
                Message rocketMsg = new Message(RocketMQConstant.DANMU_TOPIC, danmuWrapperJson.getBytes(StandardCharsets.UTF_8));
                producer.send(rocketMsg);
            }
            if (Objects.nonNull(this.userId)) {
                Danmu danmu = JsonUtil.fromJson(msg, Danmu.class);
                danmu.setUserId(userId);
                DanmuService danmuService = (DanmuService) APPLICATION_CONTEXT.getBean("danmuService");
                danmuService.asyncAddDanmu(danmu);
                danmuService.addDanmuToRedis(danmu);
            }
        } catch (MQBrokerException | InterruptedException | RemotingException | MQClientException e) {
            throw new RuntimeException(e);
        }
    }

    @Scheduled(fixedRate = 5000)
    private void sendOnlineCount() throws IOException {
        if (session.isOpen()) {
            sendMsg("online count: " + ONLINE_COUNT.get());
        }
    }
}

