package com.sns.ss.auth.filter;

import com.sns.ss.auth.jwt.JwtTokenizer;
import com.sns.ss.auth.utils.CustomAuthorityUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class JwtVerificationFilter extends OncePerRequestFilter {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils customAuthorityUtils;

    public JwtVerificationFilter(JwtTokenizer jwtTokenizer, CustomAuthorityUtils customAuthorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.customAuthorityUtils = customAuthorityUtils;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        Map<String, Object> claims = verifyJws(request);
        setAuthenticationToContext(claims);
        filterChain.doFilter(request, response);
    }

    @Override
        protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
            String authorization = request.getHeader("Authorization");

            return authorization == null || !authorization.startsWith("Bearer");
        }

        private Map<String, Object> verifyJws(HttpServletRequest request) {
            String jws = request.getHeader("Authorization").replace("Bearer ", "");
            String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());
            Map<String, Object> claims = jwtTokenizer.getClaims(jws, base64EncodedSecretKey).getBody();

            return claims;
        }

        private void setAuthenticationToContext(Map<String, Object> claims) {
            String email = (String) claims.get("email");
            List<GrantedAuthority> authorities = customAuthorityUtils.createAuthorities((List)claims.get("roles"));
            Authentication authentication = new UsernamePasswordAuthenticationToken(email, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
}
