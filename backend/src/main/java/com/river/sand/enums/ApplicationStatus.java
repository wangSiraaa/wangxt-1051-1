package com.river.sand.enums;

public enum ApplicationStatus {
    DRAFT("草稿"),
    PENDING_REVIEW("待审核"),
    RETURNED("已退回"),
    APPROVED("已通过"),
    CHANGED("已变更"),
    SUSPENDED("已暂停"),
    EXPIRED("已过期"),
    CANCELLED("已取消");

    private final String description;

    ApplicationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
