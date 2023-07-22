package org.wzm.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.wzm.api.UserMomentController;
import org.wzm.api.UserSupportApi;
import org.wzm.domain.PageResult;
import org.wzm.domain.RedisPageMomentsDTO;
import org.wzm.domain.UserMoments;
import org.wzm.model.response.JsonResponse;
import org.wzm.user.UserMomentsService;

import java.util.List;

// todo
public class UserMomentControllerImpl implements UserMomentController {
    @Autowired
    private UserSupportApi     userSupportApi;
    @Autowired
    private UserMomentsService userMomentsService;

    @Override
    public JsonResponse<String> addUserMoments(UserMoments userMoments) throws Exception {
        Long userId = userSupportApi.getCurrentUserId();
        userMoments.setUserId(userId);
        userMomentsService.addUserMoments(userMoments);
        return JsonResponse.success();
    }

    @Override
    public JsonResponse<List<UserMoments>> getUserSubscribedMoments() {
        Long userId = userSupportApi.getCurrentUserId();
        List<UserMoments> userSubscribedMoments = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(userSubscribedMoments);
    }

    @Override
    public JsonResponse<PageResult<UserMoments>> pageUserSubscribedMoments(Double max, Integer offset,
                                                                                   Integer count) {
        Long userId = userSupportApi.getCurrentUserId();
        PageResult<UserMoments> pageMomentsDTOPageResult = userMomentsService.pageUserSubscribedMoments(userId,
                                                                                                                max,
                                                                                                                offset,
                                                                                                                count);

    }
}
