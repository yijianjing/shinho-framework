package com.shinho.framework.common.json;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.annotation.JsonValue;

import java.io.Serializable;

/**
 * Created by linxiaohui on 15/11/12.
 */
@SuppressWarnings("serial")
public class RawValue implements Serializable{

    private String json;

    public RawValue(){

    }

    public RawValue(String json){
        this.json = json;
    }

    @JsonRawValue
    @JsonValue
    public String json(){
        return json;
    }
}
