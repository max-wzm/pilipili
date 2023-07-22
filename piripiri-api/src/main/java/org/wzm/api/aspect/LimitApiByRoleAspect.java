package org.wzm.api.aspect;

import org.aopalliance.intercept.Joinpoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.wzm.api.UserSupportApi;
import org.wzm.constant.annotation.LimitApiByRole;
import org.wzm.domain.UserInfo;
import org.wzm.model.exception.ApiException;
import org.wzm.user.UserInfoService;

import java.util.Arrays;
import java.util.List;

@Order(1)
@Component
@Aspect
public class LimitApiByRoleAspect {

    @Autowired
    private UserSupportApi userSupportApi;

    @Autowired
    private UserInfoService userInfoService;

    @Pointcut("@annotation(org.wzm.constant.annotation.LimitApiByRole)")
    public void check() {
    }

    @Before("check() && @annotation(LimitApiByRole)")
    public void doBefore(Joinpoint joinpoint, LimitApiByRole limitApiByRole) {
        List<String> limitedRoleCodes = Arrays.asList(limitApiByRole.limitedRoleCodeList());
        if (CollectionUtils.isEmpty(limitedRoleCodes)) {
            return;
        }
        Long userId = userSupportApi.getCurrentUserId();
        UserInfo userInfo = userInfoService.getUserInfoByUserId(userId);
        String userRoleCode = userInfo.getRoleCode();
        boolean noAuth = limitedRoleCodes.contains(userRoleCode);
        if (noAuth) {
            throw new ApiException("No authorization!");
        }
    }
}
