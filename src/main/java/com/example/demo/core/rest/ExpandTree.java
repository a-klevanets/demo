package com.example.demo.core.rest;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ExpandTree {
    private static final int MAX_DEPTH = 3;

    private final Map<String, ExpandTree> children = new HashMap<>();

    public static ExpandTree from(Set<String> expandParams) {
        ExpandTree root = new ExpandTree();

        for (String path : expandParams) {
            String[] parts = path.split("\\.");

            ExpandTree node = root;
            int depth = 0;

            for (String part : parts) {
                // Ignore too deep paths to avoid potential DoS attacks with circular expands
                if (++depth > MAX_DEPTH) {
                    break;
                }

                // Skip accidental ".."
                if (part != null && !part.isBlank()) {
                    node = node.children.computeIfAbsent(part, k -> new ExpandTree());
                }
            }
        }

        return root;
    }

    public boolean has(String key) {
        return children.containsKey(key);
    }

    public ExpandTree get(String key) {
        return children.get(key);
    }
}
