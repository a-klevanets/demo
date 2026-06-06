package com.example.demo.search;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.search.base.AbstractSpecificationBuilder;
import com.example.demo.search.base.StringToSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;

public class UserSpecificationBuilder extends AbstractSpecificationBuilder<User> {

    @Override
    protected Map<String, StringToSpecification<User>> buildFilterMap() {
        Map<String, StringToSpecification<User>> map = new HashMap<>();

        map.put("username", UserRepository::withUsernameLike);
        map.put("role.id",   UserRepository::withRoleId);

        return map;
    }

    @Override
    protected Map<String, Specification<User>> buildNonCollectionFetchMap() {
        Map<String, Specification<User>> map = new HashMap<>();

        map.put("role", UserRepository.fetchRole());

        return map;
    }

    @Override
    protected Map<String, Specification<User>> buildCollectionFetchMap() {
        Map<String, Specification<User>> map = new HashMap<>();

        map.put("permissions", UserRepository.fetchPermissions());

        return map;
    }
}
