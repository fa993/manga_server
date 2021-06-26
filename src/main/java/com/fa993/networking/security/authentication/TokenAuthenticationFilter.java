package com.fa993.networking.security.authentication;

import com.fa993.networking.Constants;
import com.fa993.networking.accounts.entities.Account;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String BEARER = "Bearer";

    public TokenAuthenticationFilter(RequestMatcher requiresAuth){
        super(requiresAuth);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String param = Optional.ofNullable(request.getHeader(HttpHeaders.AUTHORIZATION)).orElse(request.getParameter("t"));
        String token = Optional.ofNullable(param).map(t-> removeStart(t, BEARER)).orElseThrow(() -> new BadCredentialsException("Missing token"));
        return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(token, token));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        request.setAttribute(Constants.REQUESTER_ID, ((Account) authResult.getPrincipal()).getId());
        chain.doFilter(request, response);
    }

    private String removeStart(String target, String remove){
        target = target.trim();
        if(target.startsWith(remove)){
            target = target.substring(remove.length());
        }
        return target;
    }
}
