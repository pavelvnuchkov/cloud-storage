package com.example.CloudStorage.config.jwt;

import com.example.CloudStorage.config.CustomUserDetails;
import com.example.CloudStorage.config.CustomUserServiceImpl;
import com.example.CloudStorage.repository.TokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {
    private JwtService service;
    private CustomUserServiceImpl userService;

    private TokenRepository tokenRepository;

    public JwtFilter(TokenRepository tokenRepository, CustomUserServiceImpl userService, JwtService service) {
        this.tokenRepository = tokenRepository;
        this.userService = userService;
        this.service = service;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request);
        if (token != null && service.validateToken(token) && tokenRepository.findById(service.getId(token)).isEmpty()) {
            setCustomUserToContextHolder(token);
        }
        filterChain.doFilter(request, response);
    }

    private void setCustomUserToContextHolder(String token) {
        String email = service.getLogin(token);
        String id = service.getId(token);
        CustomUserDetails customUserDetails = userService.loadUserByUsername(email);
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(customUserDetails,
                null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    }

    private String getToken(HttpServletRequest request) {
        String autoToken = request.getHeader(HttpHeaders.COOKIE);
        if (autoToken != null && autoToken.startsWith("auth-token=")) {
            String token = autoToken.substring(11);
            return token;
        }
        return null;
    }
}
