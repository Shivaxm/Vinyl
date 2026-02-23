package com.shivam.store.filters;

import com.shivam.store.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@AllArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtService jwtService;
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        var token = authHeader.replace("Bearer ", "");
        var jwt = jwtService.parseToken(token);
       if(jwt == null || jwt.isExpired()) {
            // OWASP A09: log rejected bearer token usage without exposing token contents.
            log.warn("security_event=auth_token_rejected reason={} path={}",
                    jwt == null ? "invalid_signature_or_format" : "expired", request.getRequestURI());
            filterChain.doFilter(request, response);
            return;
       }
       var role = jwt.getRole();
       var userId = jwt.getUserId();
       var authentication = new UsernamePasswordAuthenticationToken(
               userId,
               null,
               List.of(new SimpleGrantedAuthority("ROLE_"+role)));
       authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
       SecurityContextHolder.getContext().setAuthentication(authentication);
       filterChain.doFilter(request, response);

    }
}
