package com.jhl.silver.union.biz.config;

import com.jhl.silver.union.biz.common.enums.UserAuthRoleEnum;
import com.jhl.silver.union.biz.user.service.auth.AuthService;
import com.jhl.silver.union.web.filter.JwtAuthenticationFilter;
import com.jhl.silver.union.web.handler.BizGlobalExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * @author: qingren
 * @create_time: 2025/3/18
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final String[] PERMIT_ALL_URL_PATTERNS =
            new String[] { "/api/auth/**", "/auth/**", "/api/error", "/error",
                    "/api/v3/api-docs/**", "/api/swagger-ui.html", "/api/doc.html**", "/api/webjars/**",
                    "/api/public/**",
                    "/api/public/third/**",
                    "/v3/api-docs/**", "/swagger-ui.html", "/doc.html**", "/webjars/**", "/public/**",
                    "/public/third/**",
                    "/api/cust/ensure-data-channel", "/cust/ensure-data-channel",
                    "/api/sys/cust/push-cust-info", "/sys/cust/push-cust-info",
                    "/api/ws/**", "/ws/**",
            };

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter)
            throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers(PERMIT_ALL_URL_PATTERNS).permitAll()
                                .requestMatchers("/sys/biz-cnf/**")
                                .hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER.role, UserAuthRoleEnum.ROLE_DEPT_INFO_ADMIN.role)
                                .requestMatchers("/sys/user/**")
                                .hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER.role, UserAuthRoleEnum.ROLE_USER_INFO_ADMIN.role)
                                .requestMatchers("/sys/dpt/list-dpt-info")
                                .hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER.role, UserAuthRoleEnum.ROLE_DEPT_INFO_ADMIN.role,
                                        UserAuthRoleEnum.ROLE_USER_INFO_ADMIN.role)
                                .requestMatchers("/sys/dpt/**")
                                .hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER.role, UserAuthRoleEnum.ROLE_DEPT_INFO_ADMIN.role)
                                .requestMatchers("/sys/cust/add-cust")
                                .hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER.role,
                                        UserAuthRoleEnum.ROLE_DEPT_INFO_ADMIN.role,
                                        UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN.role,
                                        UserAuthRoleEnum.ROLE_USER_INFO_ADMIN.role,
                                        UserAuthRoleEnum.ROLE_SALES.role)
                                .requestMatchers("/sys/cust/add-leader-remark")
                                .hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER.role, UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN.role)
                                .requestMatchers("/sys/cust/dispatch-cust")
                                .hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER.role, UserAuthRoleEnum.ROLE_DEPT_DATA_ADMIN.role)
                                .requestMatchers("/sys/**").hasAnyRole(UserAuthRoleEnum.ROLE_SUPPER.role)
                                .anyRequest().authenticated()
                        // .requestMatchers("/api/sys/dept/**").hasAnyRole(UserAuthRoleEnum.ROLE_DEPT_INFO_ADMIN.role)
                )
                // .oauth2Login(oauth2 -> oauth2
                //         .defaultSuccessUrl("/user", true)
                // )
                .sessionManagement(sessionManagementConfigurer ->
                        sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(csrfConfigurer -> csrfConfigurer.disable())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        ;
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(AuthService authService,
            BizGlobalExceptionHandler bizGlobalExceptionHandler) {
        return new JwtAuthenticationFilter(authService, bizGlobalExceptionHandler, PERMIT_ALL_URL_PATTERNS);
    }

}
