package org.wzm.service;

import java.util.Map;

public interface VideoLikeService {
    void addVideoLike(Long videoId, Long userId);

    Long getVideoLike(Long videoId);
}
