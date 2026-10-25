package com.example.ecommerce.security.config;

import com.example.ecommerce.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:3000}")
    private String allowedOrigins = "http://localhost:5173,http://localhost:3000";

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
                Arrays.stream(allowedOrigins.split(","))
                        .map(String::trim)
                        .filter(origin -> !origin.isBlank())
                        .toList()
        );
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        return http

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .cors(Customizer.withDefaults())
                .csrf(csrf->csrf.disable())
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(
                        auth-> auth
                                .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                                .requestMatchers(HttpMethod.POST, "/users/register").permitAll()
                                .requestMatchers(HttpMethod.POST, "/users").permitAll()
                                .requestMatchers(HttpMethod.POST, "/signup").permitAll()

                                .requestMatchers(HttpMethod.PUT,"/users/{id}").hasAuthority("ROLE_STORE")
                                .requestMatchers(HttpMethod.GET, "/roles").permitAll()
                                .requestMatchers(HttpMethod.GET,"/roles/{id}").hasAuthority("ROLE_STORE")
                                .requestMatchers(HttpMethod.GET, "/products").permitAll()
                                .requestMatchers(HttpMethod.GET, "/products/{id}").permitAll()
                                .requestMatchers(HttpMethod.GET, "/products/name").permitAll()

                                .requestMatchers(HttpMethod.POST, "/products").hasAuthority("ROLE_STORE")
                                .requestMatchers(HttpMethod.PUT, "/products/{id}").hasAuthority("ROLE_STORE")
                                .requestMatchers(HttpMethod.DELETE, "/products/{id}").hasAuthority("ROLE_STORE")
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/admin/migration/products/preview"
                                ).permitAll()
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/admin/migration/categories"
                                ).permitAll()
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/admin/migration/products"
                                ).permitAll()


                                .requestMatchers(HttpMethod.GET, "/roles").permitAll()
                                .requestMatchers(HttpMethod.GET, "/roles/{id}").hasAuthority("ROLE_STORE")


                                .requestMatchers(HttpMethod.POST, "/orders").hasAuthority("ROLE_CUSTOMER")
                                .requestMatchers(HttpMethod.PUT, "/orders/{id}").hasAuthority("ROLE_CUSTOMER")
                                .requestMatchers(HttpMethod.DELETE, "/orders/{id}").hasAuthority("ROLE_CUSTOMER")
                                .requestMatchers(HttpMethod.GET, "/categories").permitAll()
                                .requestMatchers(HttpMethod.POST, "/payments/webhook").permitAll()
                                .requestMatchers(HttpMethod.POST, "/payments/verify").authenticated()
                                .anyRequest().authenticated()
                )
                .build();

    }

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilterRegistration() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean =
                new FilterRegistrationBean<>(jwtAuthenticationFilter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }
}
