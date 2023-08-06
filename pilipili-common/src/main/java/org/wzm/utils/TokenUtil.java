package org.wzm.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;

import java.util.Date;

public class TokenUtil {
    private TokenUtil() {
    }

    public static final String ISSUER = "PILIPILI";

    public static String generateToken(Long userId) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Date expireTime = DateUtil.getTimeAfter(DateUtil.MINUTE, 30);
        return JWT.create()
                .withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(expireTime)
                .sign(algorithm);
    }

    public static Long verifyAccessToken(String token) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT jwt = verifier.verify(token);
        String userId = jwt.getKeyId();
        return Long.valueOf(userId);
    }

    public static String generateRefreshToken(Long userId) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        Date expireTime = DateUtil.getTimeAfter(DateUtil.DAY_OF_MONTH, 7);
        return JWT.create()
                .withKeyId(String.valueOf(userId))
                .withIssuer(ISSUER)
                .withExpiresAt(expireTime)
                .sign(algorithm);
    }

    public static Date verifyRefreshToken(String refreshToken) throws Exception {
        Algorithm algorithm = Algorithm.RSA256(RSAUtil.getPublicKey(), RSAUtil.getPrivateKey());
        JWTVerifier verifier = JWT.require(algorithm).build();

        DecodedJWT jwt = verifier.verify(refreshToken);
        return jwt.getExpiresAt();
    }
}
