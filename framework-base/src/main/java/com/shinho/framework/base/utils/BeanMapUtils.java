package com.shinho.framework.base.utils;

import com.google.common.collect.Maps;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.collections.map.HashedMap;

import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class BeanMapUtils {

    public static Integer BEAN_TO_MAP_MAX_DEPTH = 8;

    public enum BeanToMapStyle {
        /** 集合转下标 列子 a[0]=xxx ,a[1]=xxx ... **/
        collectionToIndex   ,
        /** map转下标 列子 a[name]=xxx ,a[age]=xxx ... **/
        mapToIndex          ,
        /** 数组转下标 列子 a[0]=xxx ,a[1]=xxx ... **/
        arrayToIndex        ,
        /** 数组转集合  **/
        arrayToList
    }

    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap<Class<?>, Class<?>>(8);
    static {
        primitiveWrapperTypeMap.put(Boolean.class, boolean.class);
        primitiveWrapperTypeMap.put(Byte.class, byte.class);
        primitiveWrapperTypeMap.put(Character.class, char.class);
        primitiveWrapperTypeMap.put(Double.class, double.class);
        primitiveWrapperTypeMap.put(Float.class, float.class);
        primitiveWrapperTypeMap.put(Integer.class, int.class);
        primitiveWrapperTypeMap.put(Long.class, long.class);
        primitiveWrapperTypeMap.put(Short.class, short.class);
    }

    private static void notNull(Object obj ,String message){
        if( null == obj ){
            throw new IllegalArgumentException("Class must not be null");
        }
    }

    public static boolean isSimpleProperty(Class<?> clazz) {
        notNull(clazz,"Class must not be null");
        return isSimpleValueType(clazz) || clazz.isArray() && isSimpleValueType(clazz.getComponentType());
    }

    public static boolean isSimpleValueType(Class<?> clazz) {
        notNull(clazz,"Class must not be null");
        return isPrimitiveOrWrapper(clazz)
                || clazz.isEnum()
                || CharSequence.class.isAssignableFrom(clazz)
                || Number.class.isAssignableFrom(clazz)
                || Date.class.isAssignableFrom(clazz)
                || URI.class == clazz
                || URL.class == clazz
                || Locale.class == clazz
                || Class.class == clazz;
    }

    public static boolean isPrimitiveOrWrapper(Class<?> clazz) {
        notNull(clazz, "Class must not be null");
        return (clazz.isPrimitive() || isPrimitiveWrapper(clazz));
    }

    public static boolean isPrimitiveWrapper(Class<?> clazz) {
        notNull(clazz, "Class must not be null");
        return primitiveWrapperTypeMap.containsKey(clazz);
    }

    public static Map beanToMap(Object obj){
        return beanToMap(obj,false);
    }

    public static Map beanToMap(Object obj,boolean nested){
        return beanToMap(obj,nested,null);
    }

    public static Map beanToMap(Object obj,boolean nested,String name,BeanToMapStyle... styles){

        return beanToMap(obj,nested,name,null,styles);
    }

    public static Map beanToMap(Object obj, boolean nested, String name, AtomicInteger depth, BeanToMapStyle... styles){


        if( null == depth ){
            depth = new AtomicInteger();
        }


        if( null == name || name.trim().isEmpty() ){
            name = "";
        }else {
            name = name+".";
        }

        Map<Object,Object> beanMap = obj instanceof Map ? (Map)obj : new BeanMap(obj);

        Map result = Maps.newLinkedHashMap();
        for(Map.Entry entry : beanMap.entrySet()){

            Object key = entry.getKey();
            Object value = entry.getValue();

            String parameterName = name+key;
            if( value == null || Objects.equals(key,"class") || value.toString().isEmpty() ){  //不处理
                continue;
            }


            if( !nested || isSimpleValueType(value.getClass())){      //不嵌套和简单值,直接设置
                result.put(parameterName, value);
            } else if( value.getClass().isArray() ){    //数组

                if( contain(BeanToMapStyle.arrayToIndex, styles) ){
                    Map temp =arrayToIndex((Object[])value,nested,parameterName,depth,styles );
                    result.putAll(temp);
                }else if(contain(BeanToMapStyle.arrayToList, styles)){
                    result.put(parameterName, Arrays.asList((Object[])value));
                }else {
                    result.put(parameterName, value);
                }
            } else if( value instanceof Collection){    //集合

                if( contain(BeanToMapStyle.collectionToIndex, styles) ){
                    Map temp = listToIndex(new ArrayList((Collection) value),nested,parameterName,depth,styles);
                    result.putAll(temp);

                }else {
                    result.put(parameterName, value);
                }

            } else if( value instanceof Map){    //集合

                if( contain(BeanToMapStyle.mapToIndex, styles) ){
                    Map temp = mapToIndex((Map)value,nested,parameterName,depth,styles);
                    result.putAll(temp);
                }else {
                    result.put(parameterName, value);
                }

            }  else {

                Map temp = beanToMap(value,nested,parameterName,styles);
                if( null != temp ){
                    result.putAll(temp);
                }

            }
        }

        return result;
    }

    private static Map arrayToIndex(Object[] array, boolean nested, String prefix, AtomicInteger depth, BeanToMapStyle... styles){

        return listToIndex(Arrays.asList(array),nested,prefix,depth,styles);
    }

    private static Map listToIndex(List list, boolean nested, String prefix, AtomicInteger depth, BeanToMapStyle... styles){


        //层级检测
        if( depth.getAndAdd(1) >= BEAN_TO_MAP_MAX_DEPTH ){
            return null;
        }
        Map result = Maps.newLinkedHashMap();
        for( int i = 0 ;i<list.size() ; i++ ){
            Object _value = list.get(i);
            String _key = prefix+"["+i+"]";
            if( nested ){
                result.putAll(BeanMapUtils.beanToMap(_value,nested,_key,depth,styles));
            }else {
                result.put(_key,_value);
            }

        }
        return result;
    }

    private static Map mapToIndex(Map map,boolean nested, String prefix, AtomicInteger depth, BeanToMapStyle... styles){

        //层级检测
        if( depth.getAndAdd(1) >= BEAN_TO_MAP_MAX_DEPTH ){
            return null;
        }
        if( prefix.endsWith(".") ){
            prefix = new String(Arrays.copyOf(prefix.toCharArray(),prefix.length()-1));
        }

        Map result = Maps.newLinkedHashMap();
        for(Object key : map.keySet() ){

            String _key = prefix+"["+key+"]";
            Object _value = map.get(key);
            if( nested ){
                map.putAll(BeanMapUtils.beanToMap(_value,nested,_key,depth,styles));
            }else {
                map.put(_key,_value);
            }

        }
        return result;
    }

    private static boolean contain(BeanToMapStyle style,BeanToMapStyle [] beanToMapStyles){
        return Stream.of(beanToMapStyles).anyMatch(e -> e == style );
    }


}