package com.autoparts.autoparts_system.security;

import com.autoparts.autoparts_system.model.User;
import com.autoparts.autoparts_system.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        System.out.println("=== JWT FILTER START ===");
        System.out.println("Request URI: " + request.getRequestURI());
        System.out.println("Request Method: " + request.getMethod());

        final String authHeader = request.getHeader("Authorization");
        System.out.println("Authorization header: " + (authHeader != null ? authHeader.substring(0, Math.min(50, authHeader.length())) + "..." : "null"));

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("No Bearer token found, continuing filter chain");
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt = authHeader.substring(7);
        System.out.println("JWT token extracted (first 50 chars): " + jwt.substring(0, Math.min(50, jwt.length())) + "...");

        if (!jwtService.validateToken(jwt)) {
            System.out.println("JWT validation failed");
            filterChain.doFilter(request, response);
            return;
        }
        System.out.println("JWT validation SUCCESS");

        Long userId = jwtService.extractUserId(jwt);
        String login = jwtService.extractLogin(jwt);
        System.out.println("Extracted userId: " + userId);
        System.out.println("Extracted login: " + login);

        if (userId == null || login == null) {
            System.out.println("userId or login is null");
            filterChain.doFilter(request, response);
            return;
        }

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            System.out.println("User not found: " + login);
            filterChain.doFilter(request, response);
            return;
        }

        System.out.println("User found: " + user.getLogin());
        System.out.println("User role: " + user.getRole().name());
        System.out.println("User enabled: " + user.isEnabled());

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority(user.getRole().name())
        );

        UserDetails userDetails = org.springframework.security.core.userdetails.User
                .withUsername(user.getLogin())
                .password(user.getPassword())
                .authorities(authorities)
                .build();

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        Map<String, Object> details = new HashMap<>();
        details.put("userId", user.getId());
        authToken.setDetails(details);

        SecurityContextHolder.getContext().setAuthentication(authToken);
        System.out.println("Authentication set for user: " + user.getLogin());
        System.out.println("=== JWT FILTER END ===");

        filterChain.doFilter(request, response);
    }
}