
package com.example.customer.config;

import com.example.customer.config.UserContext;
import com.example.customer.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Refactored:
 * - Extracted big "if" blocks into dedicated private methods (processToken, authenticateUser, buildAuthentication).
 * - No fully-qualified class names; proper imports used.
 * - Replaced request attribute hack with a request-scoped UserContext (injected via ObjectProvider).
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final ObjectProvider<UserContext> userContextProvider;

    public JwtAuthenticationFilter(JwtUtil jwtUtil,
                                   UserDetailsService userDetailsService,
                                   ObjectProvider<UserContext> userContextProvider) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userContextProvider = userContextProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = resolveToken(request);
        logger.info("JWT Filter triggered for request: " + request.getRequestURI());
        if (token != null) {
            logger.debug("JwtAuthenticationFilter: found token");
            processToken(token, request);
        } else {
            logger.debug("JwtAuthenticationFilter: no Authorization header");
        }

        chain.doFilter(request, response);
    }

    /** Extracts "Bearer ..." token from Authorization header. */
    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /** Validates the token and authenticates the user if valid. */
    private void processToken(String token, HttpServletRequest request) {
        if (!jwtUtil.validateToken(token)) {
            logger.debug("JwtAuthenticationFilter: token invalid");
            return;
        }
        authenticateUser(token, request);
    }

    /** Loads user details, sets SecurityContext, and populates the request-scoped UserContext. */
    private void authenticateUser(String token, HttpServletRequest request) {
        String email = jwtUtil.extractEmail(token);
        Long customerId = jwtUtil.extractCustomerId(token);
        request.setAttribute("authenticatedCustomerId", customerId);

        if (email == null) {
            logger.debug("JwtAuthenticationFilter: token has no email");
            return;
        }

        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            UsernamePasswordAuthenticationToken auth = buildAuthentication(userDetails, request);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // populate request-scoped UserContext (preferred over request attributes)
            UserContext userContext = userContextProvider.getObject();
            userContext.setCustomerId(customerId);
            userContext.setEmail(email);

            logger.debug("JwtAuthenticationFilter: authenticated user " + email + " cid=" + customerId);
        } catch (Exception e) {
            logger.debug("JwtAuthenticationFilter: failed to authenticate: " + e.getMessage());
            // leave unauthenticated; downstream can handle as anonymous
        }
    }

    /** Builds the Spring Security authentication token with request details. */
    private UsernamePasswordAuthenticationToken buildAuthentication(UserDetails userDetails,
                                                                    HttpServletRequest request) {
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return auth;
    }
}
