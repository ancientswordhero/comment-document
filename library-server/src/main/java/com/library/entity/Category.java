package com.library.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(name = "sort_order")
    private int sortOrder;

    public Category() {}

    public Category(Long id, String name, Long parentId, int sortOrder) {
        this.id = id;
        this.name = name;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String name;
        private Long parentId;
        private int sortOrder;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder name(String name) { this.name = name; return this; }
        public Builder parentId(Long parentId) { this.parentId = parentId; return this; }
        public Builder sortOrder(int sortOrder) { this.sortOrder = sortOrder; return this; }
        public Category build() { return new Category(id, name, parentId, sortOrder); }
    }
}
