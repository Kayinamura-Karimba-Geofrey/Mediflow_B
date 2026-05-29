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

    @Test
    public void testRegisterUserUsernameTaken() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFullName("Test User");

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Username is already taken!"));
    }

    @Test
    public void testRegisterUserEmailTaken() throws Exception {
        SignupRequest signupRequest = new SignupRequest();
        signupRequest.setUsername("testuser");
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setFullName("Test User");

        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(signupRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Email is already in use!"));
    }

    @Test
    public void testAuthenticateUserSuccess() throws Exception {
        geofrey.project.Mediflow_B.payload.request.LoginRequest loginRequest = new geofrey.project.Mediflow_B.payload.request.LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        org.springframework.security.core.Authentication authentication = org.mockito.Mockito.mock(org.springframework.security.core.Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtils.generateJwtToken(authentication)).thenReturn("mock-jwt-token");

        geofrey.project.Mediflow_B.security.services.UserDetailsImpl userDetails = new geofrey.project.Mediflow_B.security.services.UserDetailsImpl(
                1L, "testuser", "test@example.com", "password123", true, java.util.List.of(new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_USER")));
        when(authentication.getPrincipal()).thenReturn(userDetails);

        mockMvc.perform(post("/api/auth/signin")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("mock-jwt-token"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    public void testVerifyOtpSuccess() throws Exception {
        geofrey.project.Mediflow_B.payload.request.VerifyOtpRequest verifyRequest = new geofrey.project.Mediflow_B.payload.request.VerifyOtpRequest();
        verifyRequest.setUsername("testuser");
        verifyRequest.setOtp("123456");

        geofrey.project.Mediflow_B.entity.User user = new geofrey.project.Mediflow_B.entity.User("testuser", "test@example.com", "password", "Test");
        user.setEnabled(false);
        user.setOtp("123456");
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Account verified successfully! You can now log in."));
    }

    @Test
    public void testVerifyOtpInvalid() throws Exception {
        geofrey.project.Mediflow_B.payload.request.VerifyOtpRequest verifyRequest = new geofrey.project.Mediflow_B.payload.request.VerifyOtpRequest();
        verifyRequest.setUsername("testuser");
        verifyRequest.setOtp("000000");

        geofrey.project.Mediflow_B.entity.User user = new geofrey.project.Mediflow_B.entity.User("testuser", "test@example.com", "password", "Test");
        user.setEnabled(false);
        user.setOtp("123456");
        user.setOtpExpiry(java.time.LocalDateTime.now().plusMinutes(5));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        mockMvc.perform(post("/api/auth/verify-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(verifyRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Error: Invalid OTP!"));
    }
}
