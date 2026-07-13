package com.indivaragroup.jdt17wms.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      // 1. Allow all requests without authentication
      .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
      // 2. Disable CSRF so you can test POST/PUT requests easily via Postman or frontend
      .csrf(AbstractHttpConfigurer::disable)
      // 3. Disable the default login form UI
      .formLogin(AbstractHttpConfigurer::disable)
      // 4. Disable HTTP Basic auth popup
      .httpBasic(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
