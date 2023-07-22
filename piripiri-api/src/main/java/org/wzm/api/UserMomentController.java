package org.wzm.api;

import org.springframework.web.bind.annotation.RequestBody;
import org.wzm.domain.PageResult;
import org.wzm.domain.UserMoments;
import org.wzm.model.response.JsonResponse;

import java.util.List;

public interface UserMomentController {
    JsonResponse<String> addUserMoments(@RequestBody UserMoments userMoments) throws Exception;

    JsonResponse<List<UserMoments>> getUserSubscribedMoments();

    JsonResponse<PageResult<UserMoments>> pageUserSubscribedMoments(Double max, Integer offset, Integer count);
}
