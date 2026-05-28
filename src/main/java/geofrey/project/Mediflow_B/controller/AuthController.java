package geofrey.project.Mediflow_B.controller;

import geofrey.project.Mediflow_B.entity.ERole;
import geofrey.project.Mediflow_B.entity.Role;
import geofrey.project.Mediflow_B.entity.User;
import geofrey.project.Mediflow_B.payload.request.LoginRequest;
import geofrey.project.Mediflow_B.payload.request.SignupRequest;
import geofrey.project.Mediflow_B.payload.request.VerifyOtpRequest;
import geofrey.project.Mediflow_B.payload.response.JwtResponse;
import geofrey.project.Mediflow_B.payload.response.MessageResponse;
import geofrey.project.Mediflow_B.repository.RoleRepository;
import geofrey.project.Mediflow_B.repository.UserRepository;
import geofrey.project.Mediflow_B.security.jwt.JwtUtils;
import geofrey.project.Mediflow_B.security.services.UserDetailsImpl;
import geofrey.project.Mediflow_B.service.MailService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Endpoints for user login and registration")
public class AuthController {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @Autowired
    MailService mailService;

    @Operation(summary = "Authenticate user", description = "Verify credentials and return a JWT token for further requests")
    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @Operation(summary = "Register new user", description = "Create a new user account with specific roles (admin, doctor, nurse, etc.)")
    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()),
                signUpRequest.getFullName());
        
        if (signUpRequest.getDob() != null) {
            user.setDob(signUpRequest.getDob());
            user.setAge(java.time.Period.between(signUpRequest.getDob(), java.time.LocalDate.now()).getYears());
        }

        Set<String> strRoles = signUpRequest.getRole();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAnonymous = auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal());

        if (strRoles != null) {
            for (String role : strRoles) {
                if ("patient".equals(role)) {
                    boolean hasPatientPermission = isAnonymous;
                    if (!isAnonymous) {
                        for (org.springframework.security.core.GrantedAuthority authority : auth.getAuthorities()) {
                            if (authority.getAuthority().equals("ROLE_ADMIN") || authority.getAuthority().equals("ROLE_RECEPTIONIST")) {
                                hasPatientPermission = true;
                                break;
                            }
                        }
                    }
                    if (!hasPatientPermission) {
                        return ResponseEntity.status(403).body(new MessageResponse("Error: Only Receptionists, Admins or Patients themselves can register a patient!"));
                    }
                } else if (!"user".equals(role)) {
                    boolean isAdmin = false;
                    if (!isAnonymous) {
                        for (org.springframework.security.core.GrantedAuthority authority : auth.getAuthorities()) {
                            if (authority.getAuthority().equals("ROLE_ADMIN")) {
                                isAdmin = true;
                                break;
                            }
                        }
                    }
                    if (!isAdmin) {
                        return ResponseEntity.status(403).body(new MessageResponse("Error: Only Admins can register role: " + role + "!"));
                    }
                }
            }
        }

        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin":
                        Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(adminRole);

                        break;
                    case "doctor":
                        Role doctorRole = roleRepository.findByName(ERole.ROLE_DOCTOR)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(doctorRole);

                        break;
                    case "nurse":
                        Role nurseRole = roleRepository.findByName(ERole.ROLE_NURSE)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(nurseRole);

                        break;
                    case "patient":
                        Role patientRole = roleRepository.findByName(ERole.ROLE_PATIENT)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(patientRole);

                        break;
                    case "receptionist":
                        Role receptionistRole = roleRepository.findByName(ERole.ROLE_RECEPTIONIST)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(receptionistRole);

                        break;
                    default:
                        Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
                        roles.add(userRole);
                }
            });
        }

        user.setRoles(roles);

        // Generate OTP
        String otp = String.format("%06d", new Random().nextInt(999999));
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        user.setEnabled(false); // User must verify OTP first

        userRepository.save(user);

        // Send OTP Email
        mailService.sendOtpEmail(user.getEmail(), otp);

        return ResponseEntity.ok(new MessageResponse("User registered successfully! Please verify your email using the OTP: " + otp));
    }

    @Operation(summary = "Verify OTP", description = "Verify the 6-digit code sent to your email to enable your account.")
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
        return userRepository.findByUsername(verifyOtpRequest.getUsername())
                .map(user -> {
                    if (user.isEnabled()) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Error: Account is already verified!"));
                    }
                    if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Error: OTP has expired!"));
                    }
                    if (!user.getOtp().equals(verifyOtpRequest.getOtp())) {
                        return ResponseEntity.badRequest().body(new MessageResponse("Error: Invalid OTP!"));
                    }

                    user.setEnabled(true);
                    user.setOtp(null);
                    user.setOtpExpiry(null);
                    userRepository.save(user);

                    return ResponseEntity.ok(new MessageResponse("Account verified successfully! You can now log in."));
                })
                .orElse(ResponseEntity.badRequest().body(new MessageResponse("Error: User not found!")));
    }
}
