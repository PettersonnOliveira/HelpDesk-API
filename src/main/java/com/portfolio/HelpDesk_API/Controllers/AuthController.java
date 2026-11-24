package com.portfolio.HelpDesk_API.Controllers;

import com.portfolio.HelpDesk_API.Services.AuthService;
import com.portfolio.HelpDesk_API.dtos.AuthResponseDTO;
import com.portfolio.HelpDesk_API.dtos.LoginRequestDTO;
import com.portfolio.HelpDesk_API.dtos.UserRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@RequestBody @Valid UserRequestDTO requestDTO) {
        AuthResponseDTO response = authService.register(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody @Valid LoginRequestDTO requestDTO) {
        AuthResponseDTO response = authService.login(requestDTO);
        return ResponseEntity.ok(response);
    }
}

