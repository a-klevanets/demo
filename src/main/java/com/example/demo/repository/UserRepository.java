package com.example.demo.repository;

import com.example.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query("select u from User u left join fetch u.role where u.username = :username")
    Optional<User> findByUsernameWithRole(@Param("username") String username);

    @Query("select distinct u from User u left join fetch u.role r left join fetch r.permissions")
    java.util.List<User> findAllWithRoleAndPermissions();

}
