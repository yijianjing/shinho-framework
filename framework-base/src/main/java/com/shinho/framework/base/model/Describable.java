package com.shinho.framework.base.model;

public interface Describable {

    String getDescription();

    default KeyValue keyValue(){
        return new KeyValue(getDescription(),this);
    }
}