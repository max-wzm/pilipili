package org.wzm.service.impl;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.wzm.constant.RedisConstant;
import org.wzm.constant.RocketMQConstant;
import org.wzm.domain.Danmu;
import org.wzm.mapper.DanmuMapper;
import org.wzm.service.DanmuService;
import org.wzm.service.RedisCacheService;
import org.wzm.utils.DateUtil;
import org.wzm.utils.JsonUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class DanmuServiceImpl implements DanmuService {
    @Autowired
    private DanmuMapper       danmuMapper;
    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    private DefaultMQProducer dbDanmuProducer;

    @Override
    public void asyncAddDanmu(Danmu danmu)
            throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        Message rocketMessage = new Message(RocketMQConstant.DANMU_DB_TOPIC, JsonUtil.toJson(danmu).getBytes());
        dbDanmuProducer.send(rocketMessage);
    }

    @Override
    public void save(Danmu danmu){
        danmuMapper.save(danmu);
    }

    @Override
    public void addDanmuToRedis(Danmu danmu) {
        String key = RedisConstant.DANMU_KEY + danmu.getId();
        redisCacheService.setValueRand(key, danmu, 30, TimeUnit.MINUTES);
    }

    @Override
    public List<Danmu> get(Long videoId, Long startTime, Long endTime) {
        return danmuMapper.listByTime(videoId, DateUtil.parseDate(startTime), DateUtil.parseDate(endTime));
    }
}
