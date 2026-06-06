package com.example.demo.controller;

import com.example.demo.core.rest.Expandable;
import com.example.demo.core.rest.RequestContext;
import com.example.demo.dto.RoleDto;
import com.example.demo.repository.RoleRepository;
import com.example.demo.search.RoleSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RequestContext requestContext;
    private final RoleRepository roleRepository;

    @GetMapping
    @Expandable
    public Page<RoleDto> listRoles(
            @RequestParam Map<String, String> filters,
            Pageable pageable
    ) {
        var builder = new RoleSpecificationBuilder();
        var specification = Specification.allOf(
                builder.buildFiltersSpecification(filters),
                builder.buildFetchSpecification(RoleDto.class, requestContext, true)
        );

        return roleRepository.findAll(specification, pageable)
                .map(entity -> new RoleDto(entity, requestContext.getExpandTree()));
    }

    @GetMapping("/{id}")
    @Expandable
    public RoleDto getRole(@PathVariable("id") Long id) {
        var builder = new RoleSpecificationBuilder();
        var role = roleRepository.findOne(Specification.allOf(
                    RoleRepository.withId(id),
                    builder.buildFetchSpecification(RoleDto.class, requestContext, false)
                ))
                .orElseThrow(() -> new RuntimeException("Role not found"));

        return new RoleDto(role, requestContext.getExpandTree());
    }
}
