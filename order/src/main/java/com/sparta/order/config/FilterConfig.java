package com.sparta.order.config;

import com.sparta.order.filter.AuthenticationHeaderFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FilterConfig {

  private final AuthenticationHeaderFilter authenticationHeaderFilter;

  public FilterConfig(AuthenticationHeaderFilter authenticationHeaderFilter) {
    this.authenticationHeaderFilter = authenticationHeaderFilter;
  }

  @Bean
  public FilterRegistrationBean<AuthenticationHeaderFilter> registerAuthenticationFilter() {
    FilterRegistrationBean<AuthenticationHeaderFilter> registrationBean = new FilterRegistrationBean<>();
    registrationBean.setFilter(authenticationHeaderFilter);
    registrationBean.addUrlPatterns("/api/orders/*"); // 필터 적용 경로 설정
    registrationBean.setOrder(1); // 필터 순서 설정
    return registrationBean;
  }
}
