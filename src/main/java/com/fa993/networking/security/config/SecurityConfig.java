package com.fa993.networking.security.config;

import com.fa993.networking.security.authentication.AuthenticationChecker;
import com.fa993.networking.security.authentication.AuthenticationFailureChecker;
import com.fa993.networking.security.authentication.TokenAuthenticationFilter;
import com.fa993.networking.security.authentication.TokenAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static java.util.Objects.requireNonNull;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final RequestMatcher PUBLIC_URLS = new OrRequestMatcher(new AntPathRequestMatcher("/public/**"));
    private static final RequestMatcher SECURED_URLS = new NegatedRequestMatcher(PUBLIC_URLS);

    @Autowired
    TokenAuthenticationProvider provider;

    public SecurityConfig(TokenAuthenticationProvider provider) {
        super();
        this.provider = requireNonNull(provider);
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(provider);
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().requestMatchers(PUBLIC_URLS);
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).
                and().exceptionHandling().defaultAuthenticationEntryPointFor(forbiddenEntryPoint(), SECURED_URLS).authenticationEntryPoint(authenticationEntryPoint()).
                and().authenticationProvider(provider).addFilterBefore(restAuthenticationFilter(), AnonymousAuthenticationFilter.class).authorizeRequests().requestMatchers(SECURED_URLS).authenticated().
                and().csrf().disable().formLogin().disable().httpBasic().disable().logout().disable();
    }

    public TokenAuthenticationFilter restAuthenticationFilter() throws Exception {
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(SECURED_URLS);
        filter.setAuthenticationManager(authenticationManager());
        filter.setAuthenticationSuccessHandler(successHandler());
        filter.setAuthenticationFailureHandler(authenticationFailureHandler());
        return filter;
    }

    private SimpleUrlAuthenticationSuccessHandler successHandler() {
        SimpleUrlAuthenticationSuccessHandler handler=  new SimpleUrlAuthenticationSuccessHandler();
        handler.setRedirectStrategy(new NoRedirectStrategy());
        return handler;
    }

    private AuthenticationChecker authenticationEntryPoint() {
        return new AuthenticationChecker();
    }

    private AuthenticationFailureHandler authenticationFailureHandler() {
        return new AuthenticationFailureChecker();
    }

    public AuthenticationEntryPoint forbiddenEntryPoint(){
        return new HttpStatusEntryPoint(HttpStatus.FORBIDDEN);
    }
}
