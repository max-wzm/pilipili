package org.wzm.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.wzm.api.FollowController;
import org.wzm.api.UserContextHolder;
import org.wzm.domain.FollowingGroup;
import org.wzm.domain.PageResult;
import org.wzm.domain.UserFollowing;
import org.wzm.model.response.JsonResponse;
import org.wzm.service.FollowService;
import org.wzm.service.FollowingGroupService;

import java.util.List;

@RestController
public class FollowControllerImpl implements FollowController {
    @Autowired
    private UserContextHolder userContextHolder;
    @Autowired
    private FollowService     followService;
    @Autowired
    private FollowingGroupService followingGroupService;

    //添加关注信息
    @PostMapping("/user-followings")
    @Override
    public JsonResponse<String> addUserFollowings(@RequestBody UserFollowing userFollowing) {
        Long userId = userContextHolder.getCurrentUserId();
        userFollowing.setUserId(userId);
        followService.addUserFollowings(userFollowing);
        return JsonResponse.success();
    }

    @GetMapping("/user-followings")
    @Override
    public JsonResponse<FollowingGroup> getAllFollowings() {
        Long userId = userContextHolder.getCurrentUserId();
        FollowingGroup allFollowings = followService.getAllFollowings(userId);
        return new JsonResponse<>(allFollowings);
    }

    @GetMapping("/user-fans")
    @Override
    public JsonResponse<List<UserFollowing>> getFans() {
        Long userId = userContextHolder.getCurrentUserId();
        List<UserFollowing> userFans = followService.getUserFans(userId);
        return new JsonResponse<>(userFans);
    }

    @GetMapping("/page-user-fans")
    @Override
    public JsonResponse<PageResult<UserFollowing>> listFansByPage(@RequestParam("pageIndex") int pageIndex,
                                                                  @RequestParam("pageSize") int pageSize) {
        PageResult<UserFollowing> page = followService.listFansByPage(pageIndex, pageSize);
        return new JsonResponse<>(page);
    }

    //添加分组
    @PostMapping("/user-following-groups")
    @Override
    public JsonResponse<Long> addUserFollowingsGroups(@RequestBody FollowingGroup followingGroup) {
        Long userId = userContextHolder.getCurrentUserId();
        followingGroup.setUserId(userId);
        Long groupId = followService.addUserFollowingGroups(followingGroup);
        return new JsonResponse<>(groupId);
    }

    //查询用户分组
    @GetMapping("/user-following-groups")
    @Override
    public JsonResponse<List<FollowingGroup>> getUserFollowingGroups() {
        Long userId = userContextHolder.getCurrentUserId();
        List<FollowingGroup> list = followingGroupService.listByUserId(userId);
        return new JsonResponse<>(list);
    }

}
