package com.example.customer.config;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomerFilter implements Filter {

    @Override
    public void doFilter(
            jakarta.servlet.ServletRequest request,
            jakarta.servlet.ServletResponse response,
            FilterChain chain
    ) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // 🚨 Hardcoded value for now (replace with real auth later)
        Long dummyCustomerId = 1L;

        // Set attribute so controllers can read it
        httpRequest.setAttribute("authenticatedCustomerId", dummyCustomerId);

        chain.doFilter(httpRequest, httpResponse);
    }
}
