package org.example.currencyexchange.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import jakarta.servlet.annotation.WebFilter;

@WebFilter("/*")
public class ResponseFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {


        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
