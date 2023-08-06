package org.wzm.mapper;

import org.wzm.domain.VideoLike;

public interface VideoLikeMapper {

    VideoLike get(Long userId, Long videoId);

    int save(VideoLike videoLike);

    int delete(Long userId, Long videoId);
}
