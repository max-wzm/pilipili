package org.wzm.service;

import org.wzm.domain.PageResult;
import org.wzm.domain.Video;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface VideoService {
    void addVideos(Video video);

    PageResult<Video> pageListVideos(Integer pageIndex, Integer pageSize);

    void viewVideoOnlineBySlices(HttpServletRequest request, HttpServletResponse response, String url);

    Video getById(Long videoId);

    void update(Video video);
}
