package com.example.demo.controller;

import com.example.demo.core.rest.Expandable;
import com.example.demo.core.rest.RequestContext;
import com.example.demo.dto.UserDto;
import com.example.demo.repository.UserRepository;
import com.example.demo.search.UserSpecificationBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {

    private final RequestContext requestContext;
    private final UserRepository userRepository;

    @GetMapping("/users")
    @Expandable
    public Page<UserDto> getUsers(
            @RequestParam Map<String, String> filters,
            Pageable pageable
    ) {
        var builder = new UserSpecificationBuilder();
        var specification = Specification.allOf(
                builder.buildFiltersSpecification(filters),
                builder.buildFetchSpecification(UserDto.class, requestContext, true)
        );

        return userRepository.findAll(specification, pageable)
                .map(entity -> new UserDto(entity, requestContext.getExpandTree()));
    }

    @GetMapping("/users/{id}")
    @Expandable
    public UserDto getUserById(@PathVariable Long id) {
        var builder = new UserSpecificationBuilder();
        var user = userRepository.findOne(Specification.allOf(
                    UserRepository.withId(id),
                    builder.buildFetchSpecification(UserDto.class, requestContext, false)
                ))
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserDto(user, requestContext.getExpandTree());
    }
}
