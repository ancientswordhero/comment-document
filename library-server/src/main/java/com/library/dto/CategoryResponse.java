package com.library.dto;

import java.util.List;

public class CategoryResponse {
    private Long id;
    private String name;
    private int sortOrder;
    private List<CategoryResponse> children;

    public CategoryResponse() {}

    public CategoryResponse(Long id, String name, int sortOrder, List<CategoryResponse> children) {
        this.id = id;
        this.name = name;
        this.sortOrder = sortOrder;
        this.children = children;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public List<CategoryResponse> getChildren() { return children; }
    public void setChildren(List<CategoryResponse> children) { this.children = children; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private int sortOrder;
        private List<CategoryResponse> children;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder sortOrder(int sortOrder) { this.sortOrder = sortOrder; return this; }
        public Builder children(List<CategoryResponse> children) { this.children = children; return this; }
        public CategoryResponse build() { return new CategoryResponse(id, name, sortOrder, children); }
    }
}
