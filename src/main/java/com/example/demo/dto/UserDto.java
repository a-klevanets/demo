package com.example.demo.dto;

import java.util.List;

public record UserDto(Long id, String username, Long roleId, String role, List<String> permissions) {
}
