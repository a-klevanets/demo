package com.example.demo.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher((request) -> {
            String uri = request.getRequestURI();
            // Only save OAuth2 authorize requests, ignore favicon and other resources
            return uri.startsWith("/oauth2/authorize");
        });
        
        http
                .securityMatcher("/oauth2/**", "/.well-known/**", "/userinfo", "/connect/**", "/login")
                .requestCache(cache -> cache.requestCache(requestCache))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2AuthorizationServer(authorizationServer ->
                        authorizationServer.oidc(Customizer.withDefaults())
                )
                .formLogin(form -> form
                        .successHandler((request, response, authentication) -> {
                            SavedRequest savedRequest = requestCache.getRequest(request, response);
                            if (savedRequest != null) {
                                response.sendRedirect(savedRequest.getRedirectUrl());
                            } else {
                                response.sendRedirect("http://localhost:3000");
                            }
                        })
                )
                .exceptionHandling(ex -> ex
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http, HttpSessionRequestCache requestCache) {
        http
                .cors(Customizer.withDefaults())
                .requestCache(cache -> cache.requestCache(requestCache))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/error", "/").permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .successHandler(createOauth2LoginSuccessHandler(requestCache))
                );

        return http.build();
    }

    @Bean
    public HttpSessionRequestCache defaultRequestCache() {
        HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        requestCache.setRequestMatcher((request) -> {
            String uri = request.getRequestURI();
            // Don't save requests for static resources or error pages
            return !uri.startsWith("/favicon.ico") && 
                   !uri.startsWith("/error") && 
                   !uri.startsWith("/default-ui.css") &&
                   !uri.startsWith("/static/");
        });
        return requestCache;
    }

    private AuthenticationSuccessHandler createOauth2LoginSuccessHandler(HttpSessionRequestCache requestCache) {
        return (request, response, authentication) -> {
            // Try saved request first
            SavedRequest savedRequest = requestCache.getRequest(request, response);
            if (savedRequest != null) {
                String targetUrl = savedRequest.getRedirectUrl();
                if (targetUrl.contains("/oauth2/authorize")) {
                    response.sendRedirect(targetUrl);
                    return;
                }
            }

            // Fallback: rebuild /oauth2/authorize from cookies set by Next.js /api/auth/login
            String codeChallenge = getCookieValue(request, "code_challenge");
            String state = getCookieValue(request, "oauth_state");

            if (codeChallenge != null && state != null) {
                String authorizeUrl = "http://localhost:8080/oauth2/authorize"
                        + "?response_type=code"
                        + "&client_id=nextjs-client"
                        + "&scope=openid+profile+users.read"
                        + "&redirect_uri=http://localhost:3000/api/auth/callback"
                        + "&code_challenge=" + codeChallenge
                        + "&code_challenge_method=S256"
                        + "&state=" + state;
                response.sendRedirect(authorizeUrl);
                return;
            }

            // Last resort
            response.sendRedirect("http://localhost:3000");
        };
    }

    private String getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) return cookie.getValue();
        }
        return null;
    }
}
