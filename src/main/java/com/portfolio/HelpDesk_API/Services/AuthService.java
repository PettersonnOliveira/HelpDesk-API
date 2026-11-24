package com.portfolio.HelpDesk_API.Services;

import com.portfolio.HelpDesk_API.Exceptions.BusinessRuleException;
import com.portfolio.HelpDesk_API.Repositories.UserRepository;
import com.portfolio.HelpDesk_API.Security.TokenService;
import com.portfolio.HelpDesk_API.dtos.AuthResponseDTO;
import com.portfolio.HelpDesk_API.dtos.LoginRequestDTO;
import com.portfolio.HelpDesk_API.dtos.UserRequestDTO;
import com.portfolio.HelpDesk_API.dtos.UserResponseDTO;
import com.portfolio.HelpDesk_API.entities.Role;
import com.portfolio.HelpDesk_API.entities.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    public AuthService(UserService userService,
                       UserRepository userRepository,
                       AuthenticationManager authenticationManager,
                       TokenService tokenService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    public AuthResponseDTO register(UserRequestDTO requestDTO) {
        UserRequestDTO publicRegistration = new UserRequestDTO(
                requestDTO.name(), requestDTO.email(), requestDTO.password(), Role.CLIENT);
        UserResponseDTO userDTO = userService.create(publicRegistration);
        User user = userRepository.findById(userDTO.id())
                .orElseThrow(() -> new BusinessRuleException("Erro ao carregar usuário recém-criado."));
        String token = tokenService.generateToken(user);
        return new AuthResponseDTO(token, userDTO);
    }

    public AuthResponseDTO login(LoginRequestDTO requestDTO) {
        var authToken = new UsernamePasswordAuthenticationToken(requestDTO.email(), requestDTO.password());
        authenticationManager.authenticate(authToken);

        User user = userRepository.findByEmail(requestDTO.email())
                .orElseThrow(() -> new BusinessRuleException("Usuário não encontrado."));

        String token = tokenService.generateToken(user);
        return new AuthResponseDTO(token, new UserResponseDTO(user));
    }
}

