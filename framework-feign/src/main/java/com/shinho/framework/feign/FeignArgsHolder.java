package com.shinho.framework.feign;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.shinho.framework.base.model.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.PrioritizedParameterNameDiscoverer;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by linxiaohui on 2017/7/19.
 */
public class FeignArgsHolder {


    private static final Logger logger = LoggerFactory.getLogger(FeignArgsHolder.class);


    private static final ThreadLocal<KeyValue<Method,List<FeignMethodParameter>>> ARGS_MAP = new ThreadLocal();


    private static final PrioritizedParameterNameDiscoverer prioritizedParameterNameDiscoverer =
            new DefaultParameterNameDiscoverer();

    public static final Map<Method,List<Integer>> EXTENDED_PARAMETER_INDEX_MAP = Maps.newConcurrentMap();


    public static void put(Method method, Object [] args){

        if( null == args || null == method){
            return;
        }

        //获取参数名
        String[] names = prioritizedParameterNameDiscoverer.getParameterNames(method);
        if(null == names){
            logger.warn("获取到的参数名为空");
            return;
        }
        if( EXTENDED_PARAMETER_INDEX_MAP.isEmpty() ){
            logger.warn("EXTENDED_PARAMETER_INDEX_MAP 为空, 检测 ComplexFeignSpringMvcContract 是否注册");
        }
        //扩展参数索引
        List<Integer> index =  EXTENDED_PARAMETER_INDEX_MAP.get(method);

        if( null == index ){
            index = Collections.emptyList();
        }

        KeyValue<Method,List<FeignMethodParameter>> keyValue = new KeyValue(method,Lists.newArrayList());
        for(int i=0;i<names.length;i++){

            FeignMethodParameter feignMethodParameter =new FeignMethodParameter(index.contains(i),args[i],names[i]);
            keyValue.getValue().add( feignMethodParameter );
        }
        FeignArgsHolder.ARGS_MAP.set( keyValue );
    }

    public static void remove(){
        FeignArgsHolder.ARGS_MAP.remove();
    }

    public static KeyValue<Method,List<FeignMethodParameter>> get(){

        return FeignArgsHolder.ARGS_MAP.get();
    }


    public static synchronized void addExtendArgsIndex(Method method, Integer paramIndex){

        if( !EXTENDED_PARAMETER_INDEX_MAP.containsKey(method) ){
            EXTENDED_PARAMETER_INDEX_MAP.put(method, Lists.newArrayList());
        }
        EXTENDED_PARAMETER_INDEX_MAP.get(method).add(paramIndex);
        logger.debug("方法 {} ,增加扩展参数索引 {}",method,paramIndex);
    }



    public static class FeignMethodParameter {

        private boolean extended;
        private Object value;
        private String name;

        public FeignMethodParameter(boolean extended, Object value, String name) {
            this.extended = extended;
            this.value = value;
            this.name = name;
        }

        public boolean isExtended() {
            return extended;
        }

        public Object getValue() {
            return value;
        }

        public String getName() {
            return name;
        }

    }
}
