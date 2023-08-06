package org.wzm.domain;

import lombok.Data;

import java.util.List;

@Data
public class RedisPageMomentsDTO {
    private List<UserMoments> userMoments;
    private Double            max;
    private Integer           offset;
}
