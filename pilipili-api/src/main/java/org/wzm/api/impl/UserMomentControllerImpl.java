package org.wzm.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.wzm.api.UserMomentController;
import org.wzm.api.UserContextHolder;
import org.wzm.domain.PageResult;
import org.wzm.domain.UserMoments;
import org.wzm.model.response.JsonResponse;
import org.wzm.service.UserMomentsService;

import java.util.List;

@RestController
public class UserMomentControllerImpl implements UserMomentController {
    @Autowired
    private UserContextHolder  userContextHolder;
    @Autowired
    private UserMomentsService userMomentsService;

    @Override
    public JsonResponse<String> addUserMoments(UserMoments userMoments) throws Exception {
        Long userId = userContextHolder.getCurrentUserId();
        userMoments.setUserId(userId);
        userMomentsService.addUserMoments(userMoments);
        return JsonResponse.success();
    }

    @Override
    public JsonResponse<List<UserMoments>> getUserSubscribedMoments() {
        Long userId = userContextHolder.getCurrentUserId();
        List<UserMoments> userSubscribedMoments = userMomentsService.getUserSubscribedMoments(userId);
        return new JsonResponse<>(userSubscribedMoments);
    }

    @Override
    public JsonResponse<PageResult<UserMoments>> pageUserSubscribedMoments(Double max, Integer offset, Integer count) {
        Long userId = userContextHolder.getCurrentUserId();
        PageResult<UserMoments> pageMomentsDTOPageResult = userMomentsService.pageUserSubscribedMoments(userId, max,
                                                                                                        offset, count);
        return new JsonResponse<>(pageMomentsDTOPageResult);
    }
}
