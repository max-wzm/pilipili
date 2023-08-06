package org.wzm.service;

import org.wzm.domain.Token;
import org.wzm.domain.User;
import org.wzm.domain.UserAuthInfo;

public interface UserService {
    User getUser(Long userId);

    Token login(User user);

    void register(User user);

    int updateUser(User user);

    void logout(String refreshToken, Long userId);

    String refreshAccessToken(String refreshToken, Long userId) throws Exception;

    UserAuthInfo getUserAuthorities(Long userId);

    boolean isActive(Long userId);

    void sign(Long userId);
}
