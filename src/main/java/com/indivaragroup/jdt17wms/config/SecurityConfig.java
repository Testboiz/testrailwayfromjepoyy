package com.indivaragroup.jdt17wms.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.indivaragroup.jdt17wms.constants.AppConstants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final ObjectMapper objectMapper;


  public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, ObjectMapper objectMapper) {
    this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    this.objectMapper = objectMapper;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(AbstractHttpConfigurer::disable)
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/refresh",
                         "/auth/login", "/auth/register", "/auth/refresh", "/error").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/v1/products").hasAnyRole(AppConstants.USER_ROLE, AppConstants.ADMIN_ROLE)
        .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole(AppConstants.ADMIN_ROLE)
        .requestMatchers("/api/v1/admin-dashboard").hasRole(AppConstants.ADMIN_ROLE)
        .requestMatchers("/api/v1/audit", "/api/v1/audit/**").hasRole(AppConstants.ADMIN_ROLE)
        .requestMatchers("/api/v1/users", "/api/v1/users/**").hasRole(AppConstants.ADMIN_ROLE)
        .requestMatchers("/api/v1/me/**", "/me/**").hasRole(AppConstants.USER_ROLE)
        .anyRequest().authenticated()
      )
      .exceptionHandling(exceptions -> exceptions
        .authenticationEntryPoint((request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(AppConstants.ERROR_UNAUTHORIZED));
        })
        .accessDeniedHandler((request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write(objectMapper.writeValueAsString(AppConstants.ERROR_FORBIDDEN));
        })
      )
      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .formLogin(AbstractHttpConfigurer::disable)
      .httpBasic(AbstractHttpConfigurer::disable);

    return http.build();
  }
}
