package org.wzm.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.wzm.domain.RefreshToken;
import org.wzm.mapper.RefreshTokenMapper;
import org.wzm.service.RefreshTokenService;

public class RefreshTokenServiceImpl implements RefreshTokenService {
    @Autowired
    private RefreshTokenMapper refreshTokenMapper;
    @Override
    public int removeByToken(String refreshToken) {
        return refreshTokenMapper.removeByToken(refreshToken);
    }

    @Override
    public int save(RefreshToken refreshToken) {
        return refreshTokenMapper.save(refreshToken);
    }

    @Override
    public RefreshToken getByToken(String refreshToken) {
        return refreshTokenMapper.getByToken(refreshToken);
    }

    @Override
    public int removeByUserId(Long userId) {
        return refreshTokenMapper.removeByUserId(userId);
    }
}
