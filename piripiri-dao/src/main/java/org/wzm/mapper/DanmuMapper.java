package org.wzm.mapper;

import org.apache.ibatis.annotations.Param;
import org.wzm.domain.Danmu;

import java.util.Date;
import java.util.List;

public interface DanmuMapper {
    List<Danmu> listByTime(@Param("videoId") Long videoId, @Param("start") Date start, @Param("end") Date end);

    void save(Danmu danmu);
}
