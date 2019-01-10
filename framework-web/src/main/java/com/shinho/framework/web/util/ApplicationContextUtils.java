package com.shinho.framework.web.util;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by linxiaohui on 15/11/13.
 */
public class ApplicationContextUtils {

    private static final Map<ApplicationContext,Map<RequestMappingInfo, HandlerMethod>> CONTEXT_MAP = new WeakHashMap();

    private ApplicationContextUtils(){

    }

    public static Collection<HandlerMethod> findHandlerMethods(ApplicationContext context){

        Map<RequestMappingInfo, HandlerMethod>  map = findHandlerMethodsMap(context);
        if( map != null )
            return map.values();
        return null;
    }

    public static synchronized Map<RequestMappingInfo, HandlerMethod> findHandlerMethodsMap(ApplicationContext context){


        if( !CONTEXT_MAP.containsKey(context) ){

            Map<RequestMappingInfo, HandlerMethod> handlerMappingMap = find(context);
            CONTEXT_MAP.put(context,handlerMappingMap);
        }

        return CONTEXT_MAP.get(context);
    }

    private static Map<RequestMappingInfo, HandlerMethod> find(ApplicationContext context){


        if( context instanceof WebApplicationContext) {
            //获取映射处理器
            Map<String, HandlerMapping> handlerMappingMap = context.getBeansOfType(HandlerMapping.class);

            // 常见映射处理器
            // 1 RequestMappingHandlerMapping
            // 2 BeanNameUrlHandlerMapping
            // 3 SimpleUrlHandlerMapping
            // 4 DefaultAnnotationHandlerMapping{不建议使用,被 RequestMappingHandlerMapping 替代} )
            //遍历
            for (Map.Entry<String, HandlerMapping> entry : handlerMappingMap.entrySet()) {

                if (entry.getValue() instanceof RequestMappingHandlerMapping) {

                    RequestMappingHandlerMapping handlerMapping = (RequestMappingHandlerMapping) entry.getValue();

                    return handlerMapping.getHandlerMethods();
                }
            }
        }


        if( context.getParent() != context   ){
            return find( context.getParent() );
        }

        return Collections.EMPTY_MAP;
    }

}
