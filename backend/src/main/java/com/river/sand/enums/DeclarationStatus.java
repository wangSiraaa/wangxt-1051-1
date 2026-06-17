package com.river.sand.enums;

public enum DeclarationStatus {
    DRAFT("草稿"),
    SUBMITTED("已申报"),
    VERIFIED("已核验"),
    ABNORMAL("异常"),
    DETAINED("已暂扣"),
    CANCELLED("已取消");

    private final String description;

    DeclarationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
