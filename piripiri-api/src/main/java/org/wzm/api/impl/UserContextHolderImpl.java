package org.wzm.api.impl;

import com.auth0.jwt.exceptions.TokenExpiredException;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.wzm.api.UserContextHolder;
import org.wzm.model.exception.ApiException;
import org.wzm.utils.TokenUtil;

@Controller
public class UserContextHolderImpl implements UserContextHolder {
    @Override
    public Long getCurrentUserId() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String token = requestAttributes.getRequest().getHeader("token");
        Long userId;
        try {
            userId = TokenUtil.verifyAccessToken(token);
        } catch (TokenExpiredException e) {
            throw new ApiException("Token过期！");
        } catch (Exception e) {
            throw new ApiException("非法用户token！");
        }
        return userId;
    }
}
