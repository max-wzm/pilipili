package org.wzm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.wzm.interceptor.DeepPagerInterceptor;

@Configuration
public class MybatisPlusConfig {
    @Bean
    public DeepPagerInterceptor deepPagerInterceptor() {
        return new DeepPagerInterceptor();
    }
}
