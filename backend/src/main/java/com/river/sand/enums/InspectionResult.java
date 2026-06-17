package com.river.sand.enums;

public enum InspectionResult {
    NORMAL("正常"),
    PARTIAL_ABNORMAL("部分异常"),
    ABNORMAL("异常");

    private final String description;

    InspectionResult(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
