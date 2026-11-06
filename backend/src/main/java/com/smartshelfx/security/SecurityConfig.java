package com.smartshelfx.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig
 * 
 * Complete Spring Security configuration enforcing JWT Bearer token authentication
 * and Role-Based Access Control (RBAC).
 * 
 * Roles:
 * - ADMIN: Full privileges across users, products, categories, transactions, and analytics.
 * - WAREHOUSE_MANAGER: Manages inventory stock-in/out, product updates, and purchase order receiving.
 * - VENDOR: Limited access to view and manage assigned purchase orders and products.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthEntryPointJwt unauthorizedHandler;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter, AuthEntryPointJwt unauthorizedHandler) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public authentication endpoints (Login, Registration)
                        .requestMatchers("/api/auth/**").permitAll()

                        // Health check or error endpoints
                        .requestMatchers("/error").permitAll()

                        // User Management: strictly ADMIN
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // Product/Category creation and deletion: ADMIN or WAREHOUSE_MANAGER
                        .requestMatchers(HttpMethod.POST, "/api/products/**", "/api/categories/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.PUT, "/api/products/**", "/api/categories/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**", "/api/categories/**").hasRole("ADMIN")

                        // Stock transactions: ADMIN or WAREHOUSE_MANAGER
                        .requestMatchers("/api/transactions/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER")

                        // Purchase orders: ADMIN, WAREHOUSE_MANAGER, or VENDOR (vendors see only their own)
                        .requestMatchers("/api/purchase-orders/**").hasAnyRole("ADMIN", "WAREHOUSE_MANAGER", "VENDOR")

                        // All other API endpoints require authentication
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                );

        // Add our JWT authentication filter before the default Spring Security username/password filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
