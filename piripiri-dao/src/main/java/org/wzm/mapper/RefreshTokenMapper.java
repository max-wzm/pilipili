package org.wzm.mapper;

import org.wzm.domain.RefreshToken;

public interface RefreshTokenMapper {
    int removeByToken(String refreshToken);

    int save(RefreshToken refreshToken);

    RefreshToken getByToken(String refreshToken);

    int removeByUserId(Long userId);
}
