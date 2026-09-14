package com.portfolio.HelpDesk_API.Controllers;

import com.portfolio.HelpDesk_API.Services.UserService;
import com.portfolio.HelpDesk_API.dtos.UserRequestDTO;
import com.portfolio.HelpDesk_API.dtos.UserResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> create (@RequestBody UserRequestDTO inputData){
        UserResponseDTO userResponseDTO = userService.create(inputData);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDTO);
    }
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listAll(){
        List<UserResponseDTO> users = userService.listAll();
        return ResponseEntity.ok(users);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> findById(@PathVariable Long id){
        UserResponseDTO userResponseDTO = userService.findById(id);
        return ResponseEntity.ok(userResponseDTO);
    }

}
