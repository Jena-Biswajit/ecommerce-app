package com.example.customer.config;

import com.example.customer.repository.CustomerRepository;
import com.example.customer.domain.Customer;
import com.example.customer.util.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collections;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomerRepository customerRepository;
    private final JwtUtil jwtUtil;

    public SecurityConfig(CustomerRepository customerRepository, JwtUtil jwtUtil) {
        this.customerRepository = customerRepository;
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Customer c = customerRepository.findByEmailIgnoreCase(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            return User.withUsername(c.getEmail())
                    .password(c.getPasswordHash())
                    .authorities("ROLE_USER")
                    .build();
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // NOTE: bean name "jwtAuthFilter" instead of "jwtAuthenticationFilter" to avoid name collisions
    @Bean
    public JwtAuthenticationFilter jwtAuthFilter(UserDetailsService uds) {
        return new JwtAuthenticationFilter(jwtUtil, uds);
    }

    @Bean
    public SecurityFilterChain filterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                                           UserDetailsService uds) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/customers/signup", "/api/customers/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter(uds), org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        UrlBasedCorsConfigurationSource s = new UrlBasedCorsConfigurationSource();
        s.registerCorsConfiguration("/**", config);
        return s;
    }
}
