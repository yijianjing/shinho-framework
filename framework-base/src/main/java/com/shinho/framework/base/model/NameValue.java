package com.shinho.framework.base.model;

import java.io.Serializable;

public class NameValue<V> implements Serializable {


    private String name;

    private V value;

    public NameValue(){

    }
    public NameValue(String name, V value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }


    public V getValue() {
        return value;
    }


}
