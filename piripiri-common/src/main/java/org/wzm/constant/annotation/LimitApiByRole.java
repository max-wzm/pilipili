package org.wzm.constant.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author wangzhiming
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@Documented
@Component
public @interface LimitApiByRole {
    String[] limitedRoleCodeList() default {};
}
