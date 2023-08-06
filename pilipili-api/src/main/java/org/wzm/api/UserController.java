package org.wzm.api;

import org.springframework.web.bind.annotation.RequestBody;
import org.wzm.domain.Token;
import org.wzm.domain.User;
import org.wzm.domain.UserInfo;
import org.wzm.model.response.JsonResponse;

import javax.servlet.http.HttpServletRequest;

/**
 * @author wangzhiming
 */
public interface UserController {
    JsonResponse<String> getRsaPublicKey();

    JsonResponse<String> register(@RequestBody User user);

    JsonResponse<Token> login(@RequestBody User user);

    JsonResponse<String> refreshAccessToken(HttpServletRequest request) throws Exception;

    JsonResponse<String> logout(HttpServletRequest request);

    JsonResponse<String> updateUsers(@RequestBody User user);

    JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo);

    JsonResponse<User> getUser();

}
