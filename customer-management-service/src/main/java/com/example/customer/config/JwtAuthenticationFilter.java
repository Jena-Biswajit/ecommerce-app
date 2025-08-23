package com.example.customer.config;

import com.example.customer.util.JwtUtil;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        if (token != null) {
            logger.debug("JwtAuthenticationFilter: found token");
            if (jwtUtil.validateToken(token)) {
                String email = jwtUtil.extractEmail(token);
                Long customerId = jwtUtil.extractCustomerId(token);

                if (email != null) {
                    try {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // set principal in security context
                        org.springframework.security.core.context.SecurityContextHolder.getContext().setAuthentication(auth);

                        // also store the id for controllers to verify resource ownership
                        request.setAttribute("authenticatedCustomerId", customerId);

                        logger.debug("JwtAuthenticationFilter: authenticated user " + email + " cid=" + customerId);
                    } catch (Exception e) {
                        logger.debug("JwtAuthenticationFilter: failed to load user details: " + e.getMessage());
                        // do nothing — will be anonymous
                    }
                }
            } else {
                logger.debug("JwtAuthenticationFilter: token invalid");
            }
        } else {
            logger.debug("JwtAuthenticationFilter: no Authorization header");
        }

        chain.doFilter(request, response);
    }
}
