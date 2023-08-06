package org.wzm.service;

import org.wzm.domain.VideoCollection;

public interface VideoCollectionService {
    void addVideoCollection(VideoCollection videoCollection, Long userId);

    void deleteVideoCollections(Long videoId, Long userId);

    Long getVideoCollections(Long videoId);
}
