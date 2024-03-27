package com.banking.filter;

import com.banking.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper mapper;

    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, ObjectMapper mapper, UserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.mapper = mapper;
        this.userDetailsService = userDetailsService;
    }
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> errorDetails = new HashMap<>();

        String accessToken = jwtUtil.extractToken(request);
        if (accessToken == null ) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtUtil.parseJwtClaims(accessToken);

        if (claims == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = claims.getSubject();
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        if(jwtUtil.validateClaims(claims, userDetails)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(email,null,userDetails.getAuthorities());
            authentication
                    .setDetails(new WebAuthenticationDetailsSource().buildDetails(request)); // ??

            /**
             * Below is a way to indicate to Spring security that user is authenticated and authenticated user is set under
             * SecurityContextHolder's SecurityContext's Authentication
             * For the rest flow, Spring security will consider that user is authenticated and if
             * further code needs to check authenticated user or its authorities, it can get it from
             * SecurityContextHolder's SecurityContext's Authentication
             *
             * Above, we are intentionally passing credentials to be null & not the actual one  so that
             * further code does not get to log/print the actual password accidently
             */
            SecurityContextHolder.getContext().setAuthentication(authentication);

        }
        filterChain.doFilter(request, response);
    }
}
