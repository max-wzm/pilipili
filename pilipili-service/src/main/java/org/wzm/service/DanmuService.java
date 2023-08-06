package org.wzm.service;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.wzm.domain.Danmu;

import java.util.List;

public interface DanmuService {
    void asyncAddDanmu(Danmu danmu)
            throws MQBrokerException, RemotingException, InterruptedException, MQClientException;

    void save(Danmu danmu);

    void addDanmuToRedis(Danmu danmu);

    List<Danmu> get(Long videoId, Long startTime, Long endTime);
}
