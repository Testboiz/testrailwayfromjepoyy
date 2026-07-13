package com.indivaragroup.jdt17wms.config;

import com.indivaragroup.jdt17wms.dto.utils.UserSecurityProjection;
import com.indivaragroup.jdt17wms.models.enums.UserRole;
import com.indivaragroup.jdt17wms.repositories.UserRepository;
import com.indivaragroup.jdt17wms.services.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getServletPath();
        String uri = request.getRequestURI();
        return (path != null && (path.startsWith("/api/v1/auth/") || path.startsWith("/auth/")))
                || (uri != null && (uri.startsWith("/api/v1/auth/") || uri.startsWith("/auth/")));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String token = authHeader.substring(7);

            if (!jwtService.isAccessToken(token)) {
                sendUnauthorizedError(response, "Invalid token type");
                return;
            }

            final String email = jwtService.getEmailFromToken(token);
            final String claimRole = jwtService.getRoleFromToken(token);

            UserSecurityProjection projection = userRepository.findUserSecurityProjectionByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            String userEmail = projection.getEmail();
            UserRole userRole = projection.getRole();
            Long priorCount = projection.getPriorCount();
            boolean isEarliest = (priorCount == 0);

            String roleToUse = (claimRole != null) ? claimRole : ("ROLE_" + userRole.name());
            if (isEarliest) {
                roleToUse = "ROLE_ADMIN";
            } else if ("ROLE_ADMIN".equals(roleToUse)) {
                roleToUse = "ROLE_USER";
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    userEmail,
                    null,
                    List.of(new SimpleGrantedAuthority(roleToUse))
            );
            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);

        } catch (ExpiredJwtException e) {
            sendUnauthorizedError(response, "Token expired");
            return;
        } catch (JwtException e) {
            sendUnauthorizedError(response, "Invalid token");
            return;
        } catch (Exception e) {
          System.out.println(e.getMessage());
            sendUnauthorizedError(response, "Authentication failed");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendUnauthorizedError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"" + message + "\",\"code\":401}");
    }
}
