package com.example.demo.search;

import com.example.demo.entity.Role;
import com.example.demo.repository.RoleRepository;
import com.example.demo.search.base.AbstractSpecificationBuilder;
import com.example.demo.search.base.StringToSpecification;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;

public class RoleSpecificationBuilder extends AbstractSpecificationBuilder<Role> {

    @Override
    protected Map<String, StringToSpecification<Role>> buildFilterMap() {
        Map<String, StringToSpecification<Role>> map = new HashMap<>();

        map.put("name", RoleRepository::withNameLike);

        return map;
    }

    @Override
    protected Map<String, Specification<Role>> buildNonCollectionFetchMap() {
        return Map.of();
    }

    @Override
    protected Map<String, Specification<Role>> buildCollectionFetchMap() {
        Map<String, Specification<Role>> map = new HashMap<>();

        map.put("permissions", RoleRepository.fetchPermissions());

        return map;
    }
}
