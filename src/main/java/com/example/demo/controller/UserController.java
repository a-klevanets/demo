package com.example.demo.controller;

import com.example.demo.dto.UserDto;
import com.example.demo.entity.Permission;
import com.example.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @GetMapping("/users")
    @Transactional(readOnly = true)
    public List<UserDto> getUsers() {
        return userRepository.findAllWithRoleAndPermissions().stream()
                .map(user -> {
                    String roleName = user.getRole() != null ? user.getRole().getName() : null;
                    Long roleId = user.getRole() != null ? user.getRole().getId() : null;
                    List<String> perms = List.of();
                    if (user.getRole() != null && user.getRole().getPermissions() != null) {
                        perms = user.getRole().getPermissions().stream()
                                .map(Permission::getName)
                                .collect(Collectors.toList());
                    }
                    return new UserDto(user.getId(), user.getUsername(), roleId, roleName, perms);
                })
                .collect(Collectors.toList());
    }
}
