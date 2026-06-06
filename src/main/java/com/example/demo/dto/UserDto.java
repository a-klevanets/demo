package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.demo.core.json.ConditionalJsonInclude;
import com.example.demo.core.rest.ExpandTree;
import com.example.demo.core.rest.ExpandableResponseAdvice;
import com.example.demo.entity.Permission;
import com.example.demo.entity.User;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@JsonFilter(ExpandableResponseAdvice.JSON_FILTER_PREFIX + "UserDto")
public class UserDto implements Serializable {

    private Long id;
    private String username;

    @ConditionalJsonInclude(expand = "role")
    private RoleDto role;

    @ConditionalJsonInclude(expand = "role")
    private Long roleId;

    @ConditionalJsonInclude(expand = "permissions")
    private List<String> permissions;

    @JsonIgnore
    private transient ExpandTree expandTree;

    public UserDto(@NonNull User entity, @NonNull ExpandTree expandTree) {
        this.expandTree = expandTree;

        this.id = entity.getId();
        this.username = entity.getUsername();

        setRole(entity, expandTree);
        setPermissions(entity, expandTree);
    }

    private void setRole(@NonNull User entity, @NonNull ExpandTree expandTree) {
        if (entity.getRole() == null) return;
        this.roleId = entity.getRole().getId();

        if (expandTree.has("role") && Hibernate.isInitialized(entity.getRole())) {
            this.role = new RoleDto(entity.getRole(), expandTree.get("role"));
        }
    }

    private void setPermissions(@NonNull User entity, @NonNull ExpandTree expandTree) {
        if (expandTree.has("permissions")
                && entity.getRole() != null
                && Hibernate.isInitialized(entity.getRole().getPermissions())) {
            this.permissions = entity.getRole().getPermissions().stream()
                    .map(Permission::getName)
                    .toList();
        }
    }
}
