package com.river.sand.enums;

public enum DetentionStatus {
    DETAINED("已暂扣"),
    RECTIFYING("整改中"),
    REVIEWED("已复查"),
    RELEASED("已解除"),
    CONFIRMED("已确认违规");

    private final String description;

    DetentionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
