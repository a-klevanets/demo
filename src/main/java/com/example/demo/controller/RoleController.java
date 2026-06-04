package com.example.demo.controller;

import com.example.demo.dto.RoleDto;
import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

	private final RoleRepository roleRepository;

	@GetMapping
	@Transactional(readOnly = true)
	public List<RoleDto> listRoles() {
		return roleRepository.findAll().stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	@Transactional(readOnly = true)
	public RoleDto getRole(@PathVariable("id") Long id) {
		Role role = roleRepository.findById(id).orElseThrow();
		return toDto(role);
	}

	private RoleDto toDto(Role role) {
		List<String> perms = role.getPermissions().stream()
				.map(p -> p.getName())
				.collect(Collectors.toList());
		return new RoleDto(role.getId(), role.getName(), perms);
	}
}


