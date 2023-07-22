package org.wzm.api.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.wzm.api.UserApi;
import org.wzm.api.UserSupportApi;
import org.wzm.domain.Token;
import org.wzm.domain.User;
import org.wzm.domain.UserAuthInfo;
import org.wzm.domain.UserInfo;
import org.wzm.model.response.JsonResponse;
import org.wzm.user.UserInfoService;
import org.wzm.user.UserService;
import org.wzm.utils.RSAUtil;

import javax.servlet.http.HttpServletRequest;

@RestController
@Slf4j
public class UserApiImpl implements UserApi {
    @Autowired
    private UserSupportApi  userSupportApi;
    @Autowired
    private UserService     userService;
    @Autowired
    private UserInfoService userInfoService;

    @Override
    @GetMapping("/users")
    public JsonResponse<User> getUser() {
        Long userId = userSupportApi.getCurrentUserId();
        User user = userService.getUser(userId);
        return new JsonResponse<>(user);
    }

    //登录 生成token
    @Override
    @PostMapping("/login")
    public JsonResponse<Token> login(@RequestBody User user) {
        Token token = userService.login(user);
        return new JsonResponse<>(token);
    }

    @Override
    @PostMapping("/users")
    public JsonResponse<String> register(@RequestBody User user) {
        userService.register(user);
        return JsonResponse.success();
    }

    @Override
    @GetMapping("/rsa-pks")
    public JsonResponse<String> getRsaPublicKey() {
        String publicKeyStr = RSAUtil.getPublicKeyStr();
        return JsonResponse.success(publicKeyStr);
    }

    @Override
    @PutMapping("/users")
    public JsonResponse<String> updateUsers(@RequestBody User user) {
        Long userId = userSupportApi.getCurrentUserId();
        user.setId(userId);
        userService.updateUser(user);
        return JsonResponse.success();
    }

    @Override
    @PutMapping("/user-infos")
    public JsonResponse<String> updateUserInfos(@RequestBody UserInfo userInfo) {
        Long userId = userSupportApi.getCurrentUserId();
        userInfo.setUserId(userId);
        userInfoService.updateUserInfos(userInfo);
        return JsonResponse.success();
    }

    @Override
    @DeleteMapping("/logout")
    public JsonResponse<String> logout(HttpServletRequest request) {
        String refreshToken = request.getHeader("refreshToken");
        Long userId = userSupportApi.getCurrentUserId();
        userService.logout(refreshToken, userId);
        return JsonResponse.success();
    }

    @Override
    @PostMapping("/access-tokens")
    public JsonResponse<String> refreshAccessToken(HttpServletRequest request) throws Exception {
        Long userId = userSupportApi.getCurrentUserId();
        String refreshToken = request.getHeader("refreshToken");
        String accessToken = userService.refreshAccessToken(refreshToken, userId);
        return new JsonResponse<>(accessToken);
    }

    @GetMapping("/user-authorities")
    public JsonResponse<UserAuthInfo> getUserAuthorities() {
        Long userId = userSupportApi.getCurrentUserId();
        UserAuthInfo userAuthorities = userService.getUserAuthorities(userId);
        return new JsonResponse<>(userAuthorities);
    }
}
