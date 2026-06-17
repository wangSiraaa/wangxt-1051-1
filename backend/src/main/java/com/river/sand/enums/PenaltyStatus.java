package com.river.sand.enums;

public enum PenaltyStatus {
    PENDING("待提交"),
    REVIEWING("审核中"),
    CONFIRMED("已确认"),
    REJECTED("已驳回"),
    PROCESSED("已处理");

    private final String description;

    PenaltyStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
