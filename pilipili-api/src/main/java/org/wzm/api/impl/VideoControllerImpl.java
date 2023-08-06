package org.wzm.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.wzm.api.UserContextHolder;
import org.wzm.api.VideoController;
import org.wzm.domain.PageResult;
import org.wzm.domain.Video;
import org.wzm.domain.VideoCollection;
import org.wzm.model.response.JsonResponse;
import org.wzm.service.VideoCollectionService;
import org.wzm.service.VideoLikeService;
import org.wzm.service.VideoService;

@RestController
public class VideoControllerImpl implements VideoController {
    @Autowired
    private UserContextHolder userContextHolder;
    @Autowired
    private VideoService      videoService;
    @Autowired
    private VideoLikeService       videoLikeService;
    @Autowired
    private VideoCollectionService videoCollectionService;

    @PostMapping("/videos")
    public JsonResponse<String> addVideos(@RequestBody Video video) {
        Long userId = userContextHolder.getCurrentUserId();
        video.setUserId(userId);
        videoService.addVideos(video);
        return JsonResponse.success();
    }

    //分页查询
    @GetMapping("/get-videos")
    public JsonResponse<PageResult<Video>> pageListVideos(Integer size, Integer page, String area) {
        PageResult<Video> result = videoService.pageListVideos(size, page);
        return new JsonResponse<>(result);
    }

    //视频点赞
    @PostMapping("/video-likes")
    public JsonResponse<String> addVideoLike(@RequestParam Long videoId) {
        Long userId = userContextHolder.getCurrentUserId();
        videoLikeService.addVideoLike(videoId, userId);
        return JsonResponse.success();
    }

    @GetMapping("video-likes")
    public JsonResponse<Long> getVideoLikes(@RequestParam Long videoId) {
        Long likes = videoLikeService.getVideoLike(videoId);
        return new JsonResponse<>(likes);
    }

    //收藏视频
    @PostMapping("/video-collections")
    public JsonResponse<String> addVideoCollection(@RequestBody VideoCollection videoCollection) {
        Long userId = userContextHolder.getCurrentUserId();
        videoCollectionService.addVideoCollection(videoCollection, userId);
        return JsonResponse.success();
    }

    //取消收藏视频
    @DeleteMapping("/video-collections")
    public JsonResponse<String> deleteVideoCollection(@RequestParam Long videoId){
        Long userId = userContextHolder.getCurrentUserId();
        videoCollectionService.deleteVideoCollections(videoId,userId);
        return JsonResponse.success();
    }

    //获取当前视频收藏量
    @GetMapping("/video-collections")
    public JsonResponse<Long> getVideoCollections(@RequestParam Long videoId){
        Long collections = videoCollectionService.getVideoCollections(videoId);
        return new JsonResponse<>(collections);
    }

}
