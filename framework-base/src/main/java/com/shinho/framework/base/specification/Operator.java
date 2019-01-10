package com.shinho.framework.base.specification;


import com.google.common.base.Strings;
import com.shinho.framework.base.utils.CompareUtils;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;

public enum Operator {
        /** equals   **/
        EQ {
            @Override
            boolean evaluate(Object a, Object b) {
                return Objects.equals(a,b);
            }
        },
        /** not equals   **/
        NEQ {
            @Override
            boolean evaluate(Object a, Object b) {
                return !EQ.evaluate(a,b);
            }
        },
        /** less   **/
        LT {
            @Override
            boolean evaluate(Object a, Object b) {

                return CompareUtils.compare(a,b) < 0;
            }
        },
        /** less or equal   **/
        LE {
            @Override
            boolean evaluate(Object a, Object b) {

                return CompareUtils.compare(a,b) <= 0;
            }
        },
        /** greater   **/
        GT {
            @Override
            boolean evaluate(Object a, Object b) {
                return CompareUtils.compare(a,b) > 0;
            }
        },
        /** greater or equal   **/
        GE {
            @Override
            boolean evaluate(Object a, Object b) {
                return CompareUtils.compare(a,b) >= 0;
            }
        },
        /** contain in  **/
        CI {
            @Override
            boolean evaluate(Object a, Object b) {
                return String.valueOf(a).contains(String.valueOf(b));
            }
        },
        /** not contain in **/
        NCI {
            @Override
            boolean evaluate(Object a, Object b) {
                return !CI.evaluate(a,b);
            }
        },
        /** begin with **/
        BW {
            @Override
            boolean evaluate(Object a, Object b) {
                return String.valueOf(a).startsWith(String.valueOf(b));
            }
        },
        /** not begin with**/
        NBW {
            @Override
            boolean evaluate(Object a, Object b) {
                return !BW.evaluate(a,b);
            }
        },
        /** end with**/
        EW {
            @Override
            boolean evaluate(Object a, Object b) {
                return String.valueOf(a).endsWith(String.valueOf(b));
            }
        },
        /** not end with**/
        NEW {
            @Override
            boolean evaluate(Object a, Object b) {
                return !EW.evaluate(a,b);
            }
        },
        /** in **/
        IN {
            @Override
            boolean evaluate(Object a, Object b) {
                if( null == a || null == b){
                    return false;
                }
                if( b instanceof String ){
                    return Arrays.asList(((String) b).split(",")).contains(String.valueOf(a));
                }
                if( b instanceof Collection){
                    return ((Collection) b).contains(a);
                }
                if( b.getClass().isArray()){
                    Object [] array = (Object[]) b;
                    return Arrays.asList(array).contains(a);
                }
                return false;
            }
        },
        /** not in **/
        NIN {
            @Override
            boolean evaluate(Object a, Object b) {
                return !IN.evaluate(a,b);
            }
        },
        /** is null **/
        NU {
            @Override
            boolean evaluate(Object a, Object b) {
                return null == a;
            }
        },
        /** is not null **/
        NNU {
            @Override
            boolean evaluate(Object a, Object b) {
                return null != a;
            }
        },
        EMP {
            @Override
            boolean evaluate(Object a, Object b) {
                if( null == a ){
                    return true;
                }
                if( a instanceof String ){
                    return Strings.isNullOrEmpty((String)a);
                }
                if( a instanceof Collection ){
                    return null == a || ((Collection) a).isEmpty();
                }
                if( a.getClass().isArray() ){
                    return ((Object[])a).length == 0;
                }
                return false;
            }
        },
        NEMP {
            @Override
            boolean evaluate(Object a, Object b) { return !EMP.evaluate(a,b); }
        };

        abstract boolean evaluate(Object a, Object b);
    }