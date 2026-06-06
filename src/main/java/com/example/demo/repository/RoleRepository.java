package com.example.demo.repository;

import com.example.demo.entity.Role;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface RoleRepository extends JpaRepository<Role, Long>, JpaSpecificationExecutor<Role> {

    static Specification<Role> withNameLike(String value) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("name")), "%" + value.toLowerCase() + "%");
    }

    static Specification<Role> withId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    static Specification<Role> fetchPermissions() {
        return (root, query, cb) -> {
            root.fetch("permissions", JoinType.LEFT);
            return cb.conjunction();
        };
    }
}

