package com.example.demo.dto;

import java.util.List;

public record UserDto(Long id, String username, String role, List<String> permissions) {
}
