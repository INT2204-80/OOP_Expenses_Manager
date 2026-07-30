package core;

import java.util.Objects;

public class Category {
    private String name;
    private TransactionType type;

    public Category(String name, TransactionType type) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        this.name = name.trim();
        this.type = Objects.requireNonNull(type, "TransactionType cannot be null");
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        this.name = name.trim();
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = Objects.requireNonNull(type, "TransactionType cannot be null");
    }

    // @Override
    // public boolean equals(Object o) {
    //     if (this == o) return true;
    //     if (o == null || getClass() != o.getClass()) return false;
    //     Category category = (Category) o;
    //     return id == category.id || (Objects.equals(name.toLowerCase(), category.name.toLowerCase()) && type == category.type);
    // }

    // @Override
    // public int hashCode() {
    //     return Objects.hash(id, name.toLowerCase(), type);
    // }

    @Override
    public String toString() {
        return name + " (" + type + ")";
    }
}
