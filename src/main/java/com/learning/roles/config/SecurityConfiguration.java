package com.learning.roles.config;

import com.learning.roles.constant.SecurityConstant;
import com.learning.roles.filter.AuthEntryPoint;
import com.learning.roles.filter.JWTAccessDeniedHandler;
import com.learning.roles.filter.JWTAuthTokenFilter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final JWTAuthTokenFilter jwtAuthTokenFilter;
    private final JWTAccessDeniedHandler jwtAccessDeniedHandler;
    private final AuthEntryPoint authEntryPoint;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public SecurityConfiguration(
            JWTAuthTokenFilter jwtAuthTokenFilter,
            JWTAccessDeniedHandler jwtAccessDeniedHandler,
            AuthEntryPoint authEntryPoint,
            @Qualifier("UserDetailServicePostgresql") UserDetailsService userDetailsService,
            BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.jwtAuthTokenFilter = jwtAuthTokenFilter;
        this.jwtAccessDeniedHandler = jwtAccessDeniedHandler;
        this.authEntryPoint = authEntryPoint;
        this.userDetailsService = userDetailsService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
                .cors()
                .and()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests()
                    .antMatchers(SecurityConstant.PUBLIC_URLS).permitAll()
                    .anyRequest().authenticated()
                .and()
                .exceptionHandling()
                    .accessDeniedHandler(this.jwtAccessDeniedHandler)
                    .authenticationEntryPoint(this.authEntryPoint)
                .and()
                .addFilterBefore(this.jwtAuthTokenFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    @Override
    protected void configure(AuthenticationManagerBuilder auth)
            throws Exception {
        auth.userDetailsService(this.userDetailsService).passwordEncoder(bCryptPasswordEncoder);
    }
}
