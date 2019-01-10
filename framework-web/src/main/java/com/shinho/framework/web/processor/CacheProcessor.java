package com.shinho.framework.web.processor;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.shinho.framework.base.store.Store;
import com.shinho.framework.base.utils.DigestUtils;
import com.shinho.framework.base.utils.StringUtils;
import com.shinho.framework.common.utils.JSONUtils;
import com.shinho.framework.web.annotation.Cache;
import com.shinho.framework.web.request.BodyReaderHttpServletRequestWrapper;
import com.shinho.framework.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by linxiaohui on 15/11/16.
 */
public class CacheProcessor {


    private Store store;

    private Map<Method, Cache> cacheTarget = Maps.newConcurrentMap();
    private Set<Method> viewCache = Sets.newConcurrentHashSet();

    private static final Logger LOG = LoggerFactory.getLogger(CacheProcessor.class);


    private CacheProcessor(){

    }

    public CacheProcessor(Store store,Collection<HandlerMethod> handlerMethods){
        this.store = store;
        init(handlerMethods);
    }


    protected String findCacheKey(Method method){

        HttpServletRequest request = WebUtils.getRequest();
        Cache cache = cacheTarget.get(method);

        if( null == cache ) return null;

        String prefix = method.toString() +":" + cache.cacheKey() + ":";
        switch ( cache.scope() ){

            case cookie:{
                Cookie cookie = org.springframework.web.util.WebUtils.getCookie(request,cache.cacheKey());
                if( null != cookie )
                    return prefix + cookie.getValue();
            }
            case request:
                return prefix + StringUtils.nullToEmpty(request.getAttribute(cache.cacheKey()));
            case session:
                return prefix + StringUtils.nullToEmpty(request.getSession().getAttribute( cache.cacheKey() ));
            case parameter:
                return prefix + StringUtils.nullToEmpty(request.getParameter(cache.cacheKey()));
            case header:
                return prefix + StringUtils.nullToEmpty(request.getHeader(cache.cacheKey()));
            case queryString:
                return prefix + WebUtils.getRequest().getQueryString();
            case requestBody:
                String body = BodyReaderHttpServletRequestWrapper.requestBody(request);
                return prefix + DigestUtils.digest(body, DigestUtils.Digest.MD5_32);

            default: return null;
        }
    }



    private boolean canCache(Method method){
        return  store!=null && cacheTarget.containsKey( method );
    }

    private void init(Collection<HandlerMethod> handlerMethods) {

        LOG.info("开始初始化缓存拦截器 缓存点!");

        cacheTarget.clear();
        if(!ObjectUtils.isEmpty(handlerMethods)){
            for(HandlerMethod handlerMethod : handlerMethods){

                Cache cache = handlerMethod.getMethodAnnotation(Cache.class);
                if( cache == null )
                    cache = handlerMethod.getBeanType().getAnnotation(Cache.class);

                if( null != cache && !handlerMethod.isVoid() ){
                    cacheTarget.put(handlerMethod.getMethod(), cache);
                    if( null == handlerMethod.getMethodAnnotation(ResponseBody.class) ){
                        viewCache.add( handlerMethod.getMethod() );
                    }
                }

            }
        }

        LOG.info("初始化缓存拦截器 缓存点完成!");

    }

    public Object methodValue(Method method) {

        if( !canCache(method) ){
            return null;
        }

        String key = findCacheKey(method);
        if( !Strings.isNullOrEmpty(key) ){

            try {
                Object value = store.get(key);
                Class cls = method.getReturnType();
                return JSONUtils.toObject(String.valueOf(value),cls);
            } catch (Exception e) {
                LOG.warn("",e);
            }
        }

        return null;
    }

    public void methodValue(Method method, Object value) {

        if(  null == method || null == value || !canCache(method)  ){
            return;
        }

        String key = findCacheKey(method);
        if( Strings.isNullOrEmpty(key) ) return;

        try {
            value = viewCache.contains(hashCode()) ? value : JSONUtils.toJSON(value);
            store.set(key, value);
            store.expire(key,cacheTarget.get(method).expire(), TimeUnit.MILLISECONDS);
        }catch (Exception e){
            LOG.warn("缓存 {} 结果 {} 失败",method,value,e);
        }
    }

}