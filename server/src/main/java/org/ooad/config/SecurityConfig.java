package org.ooad.server.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        // 1. Disable CSRF: Required for stateless JWT-based REST APIs.
        http.csrf(csrf -> csrf.disable());

        // 2. Configure Authorization Rules:
        http.authorizeHttpRequests(auth -> auth
                // Public Endpoints (accessible without token)
                .requestMatchers(
                        new AntPathRequestMatcher("/api/status"),
                        new AntPathRequestMatcher("/api/register"),
                        new AntPathRequestMatcher("/api/login")
                ).permitAll()

                // Allow all frontend files (HTML, JS, CSS) to be served (Crucial for vanilla JS)
                .requestMatchers(new AntPathRequestMatcher("/**")).permitAll()

                // All other API paths must be authenticated.
                .requestMatchers(new AntPathRequestMatcher("/api/**")).authenticated()
        );

        // Disable default login mechanisms as we will use a custom API endpoint
        http.httpBasic(httpBasic -> httpBasic.disable());
        http.formLogin(form -> form.disable());

        return http.build();
    }

    // 3. Password Encoder Bean: Required for securely hashing passwords.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}