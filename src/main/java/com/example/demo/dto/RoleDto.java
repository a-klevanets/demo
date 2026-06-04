package com.example.demo.dto;

import java.util.List;

/**
 * DTO for exposing role details over REST.
 */
public record RoleDto(Long id, String name, List<String> permissions) {
}


