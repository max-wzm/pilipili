package org.wzm.user;

import org.wzm.domain.Token;
import org.wzm.domain.User;

public interface UserService {
    User getUser(Long userId);

    Token login(User user);

    void register(User user);

    int updateById(User user);

    void logout(String refreshToken, Long userId);

    String refreshAccessToken(String refreshToken, Long userId) throws Exception;
}
