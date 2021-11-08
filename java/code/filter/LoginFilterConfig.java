package com.technology.filter;

import com.google.common.collect.Lists;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 *
 */
@Configuration
public class LoginFilterConfig {

    @Bean
    public FilterRegistrationBean loginFilterRegistrationBean() {
        LoginFilter loginFilter = new LoginFilter();
        FilterRegistrationBean<LoginFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setName("loginFilter");
        registrationBean.setFilter(loginFilter);
        registrationBean.setOrder(-100);
        registrationBean.setUrlPatterns(Lists.newArrayList("/login"));
        return registrationBean;
    }

}
