package com.example.webfluxplay.model;

import jakarta.validation.constraints.NotNull;

public class SomeEntity {
    private Long id;

    @NotNull
    private String name;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SomeEntity merge(SomeEntity existingEntity) {
        return this;
    }
}
