package com.solomondev.op_event_platform.service;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {

    public String requireSubject() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuthentication
                && jwtAuthentication.getToken().getSubject() != null
                && !jwtAuthentication.getToken().getSubject().isBlank()) {
            return jwtAuthentication.getToken().getSubject();
        }

        throw new AccessDeniedException("A valid OIDC subject is required");
    }
}
