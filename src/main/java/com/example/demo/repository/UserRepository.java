package com.example.demo.repository;

import com.example.demo.entity.User;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    @Query("select u from User u left join fetch u.role where u.username = :username")
    Optional<User> findByUsernameWithRole(@Param("username") String username);

    static Specification<User> withUsernameLike(String value) {
        return (root, query, cb) -> cb.like(cb.lower(root.get("username")), "%" + value.toLowerCase() + "%");
    }

    static Specification<User> withRoleId(String value) {
        return (root, query, cb) -> cb.equal(root.get("role").get("id"), Long.parseLong(value));
    }

    static Specification<User> withId(Long id) {
        return (root, query, cb) -> cb.equal(root.get("id"), id);
    }

    static Specification<User> fetchRole() {
        return (root, query, cb) -> {
            root.fetch("role", JoinType.LEFT);
            return cb.conjunction();
        };
    }

    static Specification<User> fetchPermissions() {
        return (root, query, cb) -> {
            root.fetch("role", JoinType.LEFT).fetch("permissions", JoinType.LEFT);
            return cb.conjunction();
        };
    }
}
