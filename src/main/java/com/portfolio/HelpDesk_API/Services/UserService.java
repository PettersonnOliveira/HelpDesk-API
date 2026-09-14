package com.portfolio.HelpDesk_API.Services;

import com.portfolio.HelpDesk_API.Exceptions.ResourceNotFoundException;
import com.portfolio.HelpDesk_API.Repositories.UserRepository;
import com.portfolio.HelpDesk_API.dtos.UserRequestDTO;
import com.portfolio.HelpDesk_API.dtos.UserResponseDTO;
import com.portfolio.HelpDesk_API.entities.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponseDTO create(UserRequestDTO inputData){
        User user = new User(inputData);
        User userSalva = userRepository.save(user);
        return new UserResponseDTO(userSalva);
    }
    public List<UserResponseDTO> listAll(){
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    public UserResponseDTO findById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("usuario nao encontrado"));
                return new UserResponseDTO(user);
    }
}
