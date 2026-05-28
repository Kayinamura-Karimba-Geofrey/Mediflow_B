package geofrey.project.Mediflow_B.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import geofrey.project.Mediflow_B.entity.ERole;
import geofrey.project.Mediflow_B.entity.Role;
import geofrey.project.Mediflow_B.payload.request.SignupRequest;
import geofrey.project.Mediflow_B.repository.MessageRepository;
import geofrey.project.Mediflow_B.repository.RoleRepository;
import geofrey.project.Mediflow_B.repository.UserRepository;
import geofrey.project.Mediflow_B.security.jwt.JwtUtils;
import geofrey.project.Mediflow_B.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private MessageRepository messageRepository;

    @MockBean
    private MailService mailService;

    @MockBean
    private PasswordEncoder encoder;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtUtils jwtUtils;

    @MockBean
    private geofrey.project.Mediflow_B.config.DataSeeder dataSeeder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testRegisterUserSuccess() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFullName("Test User");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(roleRepository.findByName(ERole.ROLE_USER)).thenReturn(Optional.of(new Role(ERole.ROLE_USER)));
        when(encoder.encode(any())).thenReturn("hashed_password");

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("User registered successfully")));
    }
}
