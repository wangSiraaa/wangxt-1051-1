package com.river.sand.enums;

public enum PermitStatus {
    ACTIVE("有效"),
    SUSPENDED("暂停"),
    EXPIRED("已过期"),
    CANCELLED("已注销");

    private final String description;

    PermitStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
