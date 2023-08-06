package org.wzm.mapper;

import org.wzm.domain.Video;

import java.util.List;

public interface VideoMapper {

    int save(Video video);

    Video getById(Long id);

    List<Video> listByIds(List<Long> videoIds);

    void delete(Video video);

    List<Video> page(int pageIdx, int pageSize);
}
