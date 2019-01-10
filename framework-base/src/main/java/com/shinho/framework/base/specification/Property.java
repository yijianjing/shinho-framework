package com.shinho.framework.base.specification;

/**
 * Created by linxiaohui on 2017/4/11.
 */
public class Property {

    protected String propertyName;

    protected Property(String propertyName){

        if( null == propertyName || propertyName.trim().isEmpty() ){
            throw new IllegalArgumentException();
        }
        this.propertyName = propertyName.trim();

    }

    public String name() {
        return propertyName;
    }

    public static Property name(String propertyName){
        return new Property(propertyName);
    }


}
