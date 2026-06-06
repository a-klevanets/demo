package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.example.demo.core.json.ConditionalJsonInclude;
import com.example.demo.core.rest.ExpandTree;
import com.example.demo.core.rest.ExpandableResponseAdvice;
import com.example.demo.entity.Permission;
import com.example.demo.entity.Role;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.Hibernate;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@JsonFilter(ExpandableResponseAdvice.JSON_FILTER_PREFIX + "RoleDto")
public class RoleDto implements Serializable {

    private Long id;
    private String name;

    @ConditionalJsonInclude(expand = "permissions")
    private List<String> permissions;

    @JsonIgnore
    private transient ExpandTree expandTree;

    public RoleDto(@NonNull Role entity, @NonNull ExpandTree expandTree) {
        this.expandTree = expandTree;

        this.id = entity.getId();
        this.name = entity.getName();

        setPermissions(entity, expandTree);
    }

    private void setPermissions(@NonNull Role entity, @NonNull ExpandTree expandTree) {
        if (expandTree.has("permissions") && Hibernate.isInitialized(entity.getPermissions())) {
            this.permissions = entity.getPermissions().stream()
                    .map(Permission::getName)
                    .toList();
        }
    }
}
