package com.example.demo.core.rest;

import com.example.demo.core.json.ConditionalJsonInclude;
import lombok.NonNull;
import org.springframework.data.domain.Page;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

public class RestUtil {

    private RestUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * This method determines the type of DTO based on the body content.
     * It might be a Page<DTO>, List<DTO>, or a single DTO.
     */
    public static Class<?> getDtoType(@NonNull Object body) {
        if (body instanceof Page<?> page && !page.getContent().isEmpty()) {
            return page.getContent().get(0).getClass();
        } else if (body instanceof List<?> list && !list.isEmpty()) {
            return list.get(0).getClass();
        } else {
            return body.getClass();
        }
    }

    public static boolean isFieldIncluded(
            @NonNull Field field,
            ExpandTree expandTree,
            @NonNull RequestContext context
    ) {
        ConditionalJsonInclude annotation = field.getAnnotation(ConditionalJsonInclude.class);

        // No annotation - always include
        if (annotation == null) {
            return true;
        }

        // Field has "expand" condition, but it is not requested - don't include
        if (!annotation.expand().isEmpty() && expandTree != null && !expandTree.has(annotation.expand())) {
            return false;
        }

        // Field has admin requirement, but user is not admin - don't include
        if (annotation.requireAdmin() && !context.isAdmin()) {
            return false;
        }

        // Lastly, check roles. Don't include if none of user's roles matches with required ones.
        return annotation.roles().length == 0
                || Arrays.stream(annotation.roles()).anyMatch(context.getRoles()::contains);
    }

    /**
     * Returns the element type of generic collection field (e.g. List<Foo> → Foo), or null if not resolvable.
     */
    public static Class<?> getCollectionElementType(@NonNull Field field) {
        if (!Collection.class.isAssignableFrom(field.getType())) {
            return null;
        }

        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType parameterizedType) {
            Type[] typeArgs = parameterizedType.getActualTypeArguments();
            if (typeArgs.length > 0 && typeArgs[0] instanceof Class<?> elementType) {
                return elementType;
            }
        }

        return null;
    }

    /**
     * Performs a deep scan of given DTO class and its fields
     * to determine which expand parameters are authorized for the current user.
     */
    public static Set<String> getAuthorizedExpandedPaths(@NonNull Class<?> dtoType, @NonNull RequestContext context) {
        Set<String> result = new HashSet<>();
        collectExpandedPaths("", dtoType, context.getExpandTree(), context, result);
        return result;
    }

    private static void collectExpandedPaths(
            @NonNull String prefix,
            @NonNull Class<?> dtoType,
            @NonNull ExpandTree tree,
            @NonNull RequestContext context,
            @NonNull Set<String> result
    ) {
        for (Field field : dtoType.getDeclaredFields()) {
            ExpandTree subTree = tree.get(field.getName());
            if (subTree == null || !RestUtil.isFieldIncluded(field, tree, context)) {
                continue;
            }

            String fullPath = prefix.isEmpty() ? field.getName() : prefix + "." + field.getName();
            result.add(fullPath);

            Class<?> fieldType = field.getType();
            Class<?> dtoInCollection = getCollectionElementType(field);

            Class<?> nestedType = Objects.requireNonNullElse(dtoInCollection, fieldType);

            collectExpandedPaths(fullPath, nestedType, subTree, context, result);
        }
    }
}
