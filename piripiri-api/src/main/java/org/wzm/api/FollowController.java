package org.wzm.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.wzm.domain.FollowingGroup;
import org.wzm.domain.PageResult;
import org.wzm.domain.UserFollowing;
import org.wzm.model.response.JsonResponse;

import java.util.List;

public interface FollowController {
    JsonResponse<String> addUserFollowings(@RequestBody UserFollowing userFollowing);

    JsonResponse<FollowingGroup> getAllFollowings();

    @GetMapping("/user-fans")
    JsonResponse<List<UserFollowing>> getFans();

    JsonResponse<PageResult<List<UserFollowing>>> listFansByPage(@RequestParam("pageIndex") int pageIndex, @RequestParam("pageSize") int pageSize);

    //添加分组
    @PostMapping("/user-following-groups")
    JsonResponse<Long> addUserFollowingsGroups(@RequestBody FollowingGroup followingGroup);

    //查询用户分组
    @GetMapping("/user-following-groups")
    JsonResponse<List<FollowingGroup>> getUserFollowingGroups();
}
