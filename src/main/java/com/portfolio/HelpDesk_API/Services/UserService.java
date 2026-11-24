package com.portfolio.HelpDesk_API.Services;

import com.portfolio.HelpDesk_API.Exceptions.BusinessRuleException;
import com.portfolio.HelpDesk_API.Exceptions.ResourceNotFoundException;
import com.portfolio.HelpDesk_API.Repositories.UserRepository;
import com.portfolio.HelpDesk_API.dtos.UserRequestDTO;
import com.portfolio.HelpDesk_API.dtos.UserResponseDTO;
import com.portfolio.HelpDesk_API.dtos.UserUpdateDTO;
import com.portfolio.HelpDesk_API.entities.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponseDTO create(UserRequestDTO inputData) {
        if (userRepository.existsByEmail(inputData.email())) {
            throw new BusinessRuleException("Já existe um usuário cadastrado com este e-mail.");
        }

        String encodedPassword = passwordEncoder.encode(inputData.password());
        User user = new User(inputData, encodedPassword);
        User userSalvo = userRepository.save(user);
        return new UserResponseDTO(userSalvo);
    }

    public List<UserResponseDTO> listAll() {
        return userRepository.findAll().stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    public UserResponseDTO findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return new UserResponseDTO(user);
    }

    public UserResponseDTO update(Long id, UserUpdateDTO updateDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (updateDTO.name() != null && !updateDTO.name().isBlank()) {
            user.setName(updateDTO.name());
        }
        if (updateDTO.email() != null && !updateDTO.email().isBlank()) {
            if (!user.getEmail().equalsIgnoreCase(updateDTO.email()) && userRepository.existsByEmail(updateDTO.email())) {
                throw new BusinessRuleException("E-mail já está em uso por outro usuário.");
            }
            user.setEmail(updateDTO.email());
        }

        User updatedUser = userRepository.save(user);
        return new UserResponseDTO(updatedUser);
    }

    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        userRepository.delete(user);
    }
}
