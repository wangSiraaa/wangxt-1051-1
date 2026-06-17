package com.river.sand.enums;

public enum Role {
    ENTERPRISE("企业用户"),
    AUDITOR("审核员"),
    ENFORCER("执法人员"),
    PENALTY_HANDLER("处罚线索处理人");

    private final String description;

    Role(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
