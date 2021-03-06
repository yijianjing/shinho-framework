package com.shinho.framework.base.utils;

import java.util.Date;
import java.util.Objects;

/**
 * Created by linxiaohui on 2016/10/28.
 */
public class CompareUtils {

    public static int compare(Object a , Object b){

        if(Objects.equals( a, b ) ){
            return 0;
        }

        if( a instanceof Number && b instanceof Number ){
            return new Double(((Number)a).doubleValue()).compareTo(((Number)b).doubleValue() );
        }
        if( a instanceof Date && b instanceof Date ){
            return ((Date)a).compareTo((Date)b);
        }

        return String.valueOf(a).compareTo(String.valueOf(a));
    }


}
