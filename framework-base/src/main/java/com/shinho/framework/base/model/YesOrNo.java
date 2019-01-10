package com.shinho.framework.base.model;

public enum YesOrNo implements Describable {

    N("否"), Y("是");
    private String description;

    YesOrNo(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}