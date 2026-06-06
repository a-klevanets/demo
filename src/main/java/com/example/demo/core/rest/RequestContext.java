package com.example.demo.core.rest;

import com.example.demo.entity.User;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Component
@Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {
    private Set<String> expand = Set.of();
    private ExpandTree expandTree = new ExpandTree();

    private Set<String> roles = Set.of();
    private User currentUser;
    private boolean isAdmin = false;

    @PostConstruct
    public void init() {
        HttpServletRequest req = getCurrentRequest();
        if (req != null) {
            var expandParams = Optional.ofNullable(req.getParameterValues("expand")).stream()
                    .flatMap(Arrays::stream)
                    .flatMap(p -> Arrays.stream(p.split(",")))
                    .collect(Collectors.toSet());

            expand = expandParams;
            expandTree = ExpandTree.from(expandParams);
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            roles = user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
            currentUser = user;
            isAdmin = roles.contains("ROLE_ADMIN");
        }
    }

    private HttpServletRequest getCurrentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes servletAttrs) {
            return servletAttrs.getRequest();
        }
        return null;
    }
}
