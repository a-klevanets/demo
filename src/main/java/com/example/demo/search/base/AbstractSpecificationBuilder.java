package com.example.demo.search.base;

import com.example.demo.core.rest.RequestContext;
import com.example.demo.core.rest.RestUtil;
import org.springframework.data.jpa.domain.Specification;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Base class for building JPA Specifications based on request filters and expansions.
 * Subclasses define mappings between supported filter keys and corresponding specifications,
 * as well as which fetch specifications are available for expandable DTOs.
 *
 * @param <T> the entity type
 */
public abstract class AbstractSpecificationBuilder<T> {

    /**
     * Maps supported filter keys to specifications.
     * Should only include simple field-to-filter mappings.
     */
    protected final Map<String, StringToSpecification<T>> filterSpecificationMap;

    /**
     * Maps expandable property names to their corresponding fetch specifications.
     * Only fields explicitly requested via expand param will be used.
     */
    protected final Map<String, Specification<T>> nonCollectionFetchMap;

    /**
     * Maps expandable property names to their corresponding fetch specifications.
     * Only fields explicitly requested via expand param will be used.
     */
    protected final Map<String, Specification<T>> collectionFetchMap;

    protected AbstractSpecificationBuilder() {
        this.filterSpecificationMap = buildFilterMap();
        this.nonCollectionFetchMap = buildNonCollectionFetchMap();

        Map<String, Specification<T>> extendedMap = new HashMap<>(Map.copyOf(this.nonCollectionFetchMap));
        extendedMap.putAll(this.buildCollectionFetchMap());
        this.collectionFetchMap = Map.copyOf(extendedMap);
    }

    protected abstract Map<String, StringToSpecification<T>> buildFilterMap();

    /**
     * Include only non-collection relations here, like ManyToOne or OneToOne.
     * Collections should be included in the separate collectionFetchMap, as they break pagination.
     */
    protected abstract Map<String, Specification<T>> buildNonCollectionFetchMap();

    /**
     * Include only collection relations here, like OneToMany or ManyToMany.
     * Then you can use it in single-entity endpoints.
     */
    protected abstract Map<String, Specification<T>> buildCollectionFetchMap();

    /**
     * Override in subclasses to support complex filters (e.g. null checks, ranges, compound logic).
     */
    @SuppressWarnings("unused")
    protected Specification<T> customSpecification(Map<String, String> filters) {
        return Specification.unrestricted();
    }

    /**
     * Builds the full filter specification by combining mapped filters and custom logic.
     */
    public Specification<T> buildFiltersSpecification(Map<String, String> filters) {
        Specification<T> specification = Specification.unrestricted();

        if (filters != null) {
            for (Map.Entry<String, String> entry : filters.entrySet()) {
                var key = entry.getKey();
                var value = entry.getValue();
                var filterSpec = filterSpecificationMap.get(key);

                if (filterSpec != null && entry.getValue() != null) {
                    specification = specification.and(filterSpec.op(value));
                }
            }
        }

        return specification.and(customSpecification(filters));
    }

    /**
     * Builds the fetch specification based on which fields are expanded
     * and authorized in the current request context.
     *
     * @param dtoType the root DTO class used in the response
     * @param requestContext the current request context
     * @param nonCollectionOnly if true, only non-collection relations are considered, which are safe for pagination
     * @return a specification that joins only explicitly expanded fields
     */
    public Specification<T> buildFetchSpecification(
            Class<?> dtoType,
            RequestContext requestContext,
            boolean nonCollectionOnly
    ) {
        Set<String> expandedPaths = RestUtil.getAuthorizedExpandedPaths(dtoType, requestContext);

        var fetchMap = nonCollectionOnly ? nonCollectionFetchMap : collectionFetchMap;

        return expandedPaths.stream()
                .map(fetchMap::get)
                .filter(Objects::nonNull)
                .reduce(Specification::and)
                .orElse(Specification.unrestricted());
    }
}
