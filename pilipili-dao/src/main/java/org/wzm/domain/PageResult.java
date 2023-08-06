package org.wzm.domain;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private Integer total;

    private List<T> list;

    private Double  max;
    private Integer offset;
}
