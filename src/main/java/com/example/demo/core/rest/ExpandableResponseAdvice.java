package com.example.demo.core.rest;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import tools.jackson.databind.ser.FilterProvider;
import tools.jackson.databind.ser.std.SimpleBeanPropertyFilter;
import tools.jackson.databind.ser.std.SimpleFilterProvider;

import java.lang.reflect.Field;
import java.util.*;

import static com.example.demo.core.rest.RestUtil.getCollectionElementType;

@ControllerAdvice
public class ExpandableResponseAdvice implements ResponseBodyAdvice<Object> {

    public static final String JSON_FILTER_PREFIX = "expandable:";

    @Autowired
    RequestContext requestContext;

    @Override
    public boolean supports(
            MethodParameter returnType,
            @NonNull Class<? extends HttpMessageConverter<?>> converterType
    ) {
        return returnType.hasMethodAnnotation(Expandable.class)
                && JacksonJsonHttpMessageConverter.class.isAssignableFrom(converterType);
    }

    @Override
    public @Nullable Object beforeBodyWrite(
            @Nullable Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
            @NonNull ServerHttpRequest request,
            @NonNull ServerHttpResponse response
    ) {
        return body;
    }

    @Override
    public @Nullable Map<String, Object> determineWriteHints(
            @Nullable Object body,
            @NonNull MethodParameter returnType,
            @NonNull MediaType selectedContentType,
            @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType
    ) {
        if (body == null) {
            return null;
        }

        Class<?> rootDtoType = RestUtil.getDtoType(body);
        if (rootDtoType == null) {
            return null;
        }

        FilterProvider filters = createFilters(rootDtoType);

        return Map.of(FilterProvider.class.getName(), filters);
    }

    private FilterProvider createFilters(Class<?> rootDtoType) {
        Map<Class<?>, ExpandTree> expandedDTOMap = new HashMap<>();
        collectExpandedDtoTypes(rootDtoType, requestContext.getExpandTree(), expandedDTOMap);

        SimpleFilterProvider filters = new SimpleFilterProvider();

        for (Map.Entry<Class<?>, ExpandTree> entry : expandedDTOMap.entrySet()) {
            Class<?> dtoType = entry.getKey();
            ExpandTree subtree = entry.getValue();

            String filterId = JSON_FILTER_PREFIX + dtoType.getSimpleName();

            Set<String> includedFields = getContextFilteredFields(
                    dtoType,
                    requestContext,
                    subtree
            );

            filters.addFilter(
                    filterId,
                    SimpleBeanPropertyFilter.filterOutAllExcept(includedFields)
            );
        }

        return filters;
    }

    private void collectExpandedDtoTypes(
            Class<?> type,
            ExpandTree expandTree,
            Map<Class<?>, ExpandTree> result
    ) {
        if (type == null || result.containsKey(type)) {
            return;
        }

        if (type.isAnnotationPresent(JsonFilter.class)) {
            result.put(type, expandTree);
        }

        for (Field field : type.getDeclaredFields()) {
            ExpandTree subTree = expandTree.get(field.getName());
            if (subTree == null) {
                continue;
            }

            Class<?> fieldType = field.getType();
            Class<?> dtoInCollection = getCollectionElementType(field);

            Class<?> nestedType = Objects.requireNonNullElse(dtoInCollection, fieldType);

            collectExpandedDtoTypes(nestedType, subTree, result);
        }
    }

    private Set<String> getContextFilteredFields(
            @NonNull Class<?> dtoType,
            @NonNull RequestContext context,
            ExpandTree expandTree
    ) {
        Set<String> result = new HashSet<>();

        for (Field field : dtoType.getDeclaredFields()) {
            if (RestUtil.isFieldIncluded(field, expandTree, context)) {
                result.add(field.getName());
            }
        }

        return result;
    }
}
