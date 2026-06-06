package com.example.demo.search.base;

import org.springframework.data.jpa.domain.Specification;

@FunctionalInterface
public interface StringToSpecification<T> {
    Specification<T> op(String value);
}
