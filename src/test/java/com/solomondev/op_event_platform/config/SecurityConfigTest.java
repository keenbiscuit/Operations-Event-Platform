package com.solomondev.op_event_platform.config;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.solomondev.op_event_platform.controller.OpEventPlatformController;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

@WebMvcTest(OpEventPlatformController.class)
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
@TestPropertySource(properties = "spring.security.oauth2.resourceserver.jwt.issuer-uri=https://issuer.example.test")
class SecurityConfigTest {
    @Autowired private MockMvc mockMvc;
    @MockitoBean private JwtDecoder jwtDecoder;

    @Test
    void rejectsRequestsWithoutBearerAuthentication() throws Exception {
        mockMvc.perform(get("/op-event-platform/health"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void acceptsAuthenticatedJwtRequests() throws Exception {
        mockMvc.perform(get("/op-event-platform/health").with(jwt()))
                .andExpect(status().isOk());
    }
}
