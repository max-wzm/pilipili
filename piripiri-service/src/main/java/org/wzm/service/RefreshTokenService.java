package org.wzm.service;

import org.wzm.domain.RefreshToken;

public interface RefreshTokenService {
    int removeByToken(String refreshToken);

    int save(RefreshToken refreshToken);

    RefreshToken getByToken(String refreshToken);

    int removeByUserId(Long userId);
}
