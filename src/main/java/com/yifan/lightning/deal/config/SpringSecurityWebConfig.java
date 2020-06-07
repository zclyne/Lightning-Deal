package com.yifan.lightning.deal.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yifan.lightning.deal.filter.UsernamePasswordJsonFilter;
import com.yifan.lightning.deal.response.CommonReturnType;
import com.yifan.lightning.deal.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Spring Security配置
 * 指定了匿名用户所允许访问的url，以及自定义的认证filter和EntryPoint
 */

@Configuration
@EnableWebSecurity
public class SpringSecurityWebConfig extends WebSecurityConfigurerAdapter {

    @Autowired
    UserServiceImpl userService;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userService).passwordEncoder(new BCryptPasswordEncoder());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/")
                .permitAll()
                .and()
                .formLogin()
                .loginProcessingUrl("/login")
                .and()
                .logout()
                .permitAll()
                .and()
                .csrf()
                .disable()
                .exceptionHandling()
                .authenticationEntryPoint(new AuthenticationEntryPoint() {
                    @Override
                    public void commence(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
                        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        PrintWriter out = httpServletResponse.getWriter();
                        CommonReturnType apiResponse = CommonReturnType.create("Please login！", "fail");
                        out.write(new ObjectMapper().writeValueAsString(apiResponse));
                        out.flush();
                        out.close();
                    }
                });
        http.addFilterAt(getTelephonePasswordAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
    }

    @Bean
    protected UsernamePasswordJsonFilter getTelephonePasswordAuthenticationFilter() throws Exception {
        UsernamePasswordJsonFilter filter = new UsernamePasswordJsonFilter();
        filter.setAuthenticationSuccessHandler(new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                PrintWriter out = response.getWriter();
                CommonReturnType apiResponse = CommonReturnType.create("Successfully logged in!");
                out.write(new ObjectMapper().writeValueAsString(apiResponse));
                out.flush();
                out.close();
            }
        });
        filter.setAuthenticationFailureHandler(new AuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                PrintWriter out = response.getWriter();
                CommonReturnType apiResponse = CommonReturnType.create("Failed to login! Please try again", "fail");
                out.write(new ObjectMapper().writeValueAsString(apiResponse));
                out.flush();
                out.close();
            }
        });
        // 直接使用WebSecurityConfigurerAdapter提供的AuthenticationManager
        filter.setAuthenticationManager(authenticationManagerBean());
        return filter;
    }

    @Bean
    @Override
    protected AuthenticationManager authenticationManager() throws Exception {
        return super.authenticationManager();
    }
}
