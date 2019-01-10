package com.shinho.framework.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;


import com.shinho.framework.base.model.Response;
import com.shinho.framework.base.store.LocalStore;
import com.shinho.framework.base.store.Store;
import com.shinho.framework.base.utils.TemplateUtils;
import com.shinho.framework.common.utils.JSONUtils;
import com.shinho.framework.common.utils.SpringContextUtils;
import com.shinho.framework.web.processor.CacheProcessor;
import com.shinho.framework.web.processor.ResponseProcessor;
import com.shinho.framework.web.request.BodyReaderHttpServletRequestWrapper;
import com.shinho.framework.web.util.ApplicationContextUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by linxiaohui on 15/11/12.
 */
public class HandlerAdapter extends RequestMappingHandlerAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(HandlerAdapter.class);

    private boolean includeDefaults = true;

    @Autowired(required = false)
    private ResponseProcessor responseProcessor = null;
    @Autowired(required = false)
    private Store store = new LocalStore();

    //默认的缓存处理器
    @Autowired(required = false)
    private CacheProcessor cacheProcessor = null;
    //初始化结果判断集合(@ResponseBody)
    private Set<Method> responseBodySet = Sets.newConcurrentHashSet();
    //初始化请求体判断集合
    private Set<Method> requestBodySet = Sets.newConcurrentHashSet();
    //初始化跳过方法集合
    private List<String> skipCustomHandlerAdapterWithMethod = new ArrayList<String>();


	public HandlerAdapter(){

        //初始化默认排序
        setOrder(Ordered.LOWEST_PRECEDENCE -1);
    }


    @Override
    protected ModelAndView handleInternal(HttpServletRequest request, HttpServletResponse response, HandlerMethod handlerMethod) throws Exception {

        //requestBody 注解请求
        if( requestBodySet.contains(handlerMethod.getMethod())
                && !(request instanceof BodyReaderHttpServletRequestWrapper)){
            return super.handleInternal( new BodyReaderHttpServletRequestWrapper(request) , response, handlerMethod);
        }

        //非GET 的欣和框架扩展feign请求
        if( request.getHeader(com.shinho.framework.base.feign.Constants.IS_FEIGN_EXTENDED) != null
                && !Objects.equals(request.getMethod().toUpperCase(),"GET")
                && !(request instanceof BodyReaderHttpServletRequestWrapper) ){
            return super.handleInternal( new BodyReaderHttpServletRequestWrapper(request) , response, handlerMethod);
        }

        return super.handleInternal(request,response,handlerMethod);
    }

    protected ServletInvocableHandlerMethod createInvocableHandlerMethod(final HandlerMethod handlerMethod) {
    	if(skipCustomHandlerAdapterWithMethod.contains(getHandlerMethodFullName(handlerMethod))){
    		return super.createInvocableHandlerMethod(handlerMethod);
    	}

        return new ServletInvocableHandlerMethod(handlerMethod){

            public Object invokeForRequest(NativeWebRequest request, ModelAndViewContainer mavContainer, Object... providedArgs) throws Exception {

                Object value = null;
                Exception exception = null;
                Method method = handlerMethod.getMethod();

                try {
                    //获取缓存结果
                    value = cacheProcessor.methodValue(method);

                    if (null != value) { //获取缓存结果成功
                        saveResultAndType(request,value,ResultType.cache);
                    }else { //没有缓存结果
                        value = super.invokeForRequest(request, mavContainer, providedArgs);
                        saveResultAndType(request,value,ResultType.execute);

                        if( !(value instanceof Response) || ((Response<?>)value).isSuccess()){
                            cacheProcessor.methodValue(method, value);
                        }
                    }
                } catch (Exception e) {

                    //保存异常信息到请求域
                    request.setAttribute(Constants.EXCEPTION_KEY,e, RequestAttributes.SCOPE_REQUEST);
                    exception = e;
                }finally {

                    //获取响应结果类型
                    ResponseType type = responseBodySet.contains(method)
                            ? ResponseType.responseBody
                            : ResponseType.viewAndModel;
                    //保存到 request 作用域
                    request.setAttribute(Constants.RESPONSE_TYPE_KEY, type, RequestAttributes.SCOPE_REQUEST);


					if (null != responseProcessor) { // 结果转换

						value = (null == exception)
								? responseProcessor.processor(value, type)
								: responseProcessor.processorForException(exception, type);
					}

                }

                request.setAttribute(Constants.RESULT_FINAL_KEY, value, RequestAttributes.SCOPE_REQUEST);
                //返回结果
                return value;
            }
        };
    }

    public Store getStore() {
        return store;
    }

    public void setStore(Store store) {
        this.store = store;
    }

    public boolean isIncludeDefaults() {
        return includeDefaults;
    }

    public void setIncludeDefaults(boolean includeDefaults) {
        this.includeDefaults = includeDefaults;
    }

    public void setSkipCustomHandlerAdapterWithMethod(
            List<String> skipCustomHandlerAdapterWithMethod) {
        this.skipCustomHandlerAdapterWithMethod = skipCustomHandlerAdapterWithMethod;
    }

    private Object saveResultAndType(NativeWebRequest request, Object value, ResultType type){
        request.setAttribute(Constants.RESULT_KEY, value, RequestAttributes.SCOPE_REQUEST);
        request.setAttribute(Constants.RESULT_TYPE_KEY, type, RequestAttributes.SCOPE_REQUEST);
        return value;
    }

    public ResponseProcessor getResponseProcessor() {
        return responseProcessor;
    }

    public void setResponseProcessor(ResponseProcessor responseProcessor) {
        this.responseProcessor = responseProcessor;
    }

    protected void initApplicationContext(ApplicationContext context) {

        super.initApplicationContext(context);

        initDefaultConfigByApplication(context);
    }

    private void initDefaultConfigByApplication(ApplicationContext context){

        //获取HandlerMethods
        Collection<HandlerMethod> handlerMethods = ApplicationContextUtils.findHandlerMethods(context);

        if( null == responseProcessor ){
            responseProcessor = findBean(ResponseProcessor.class);
        }

        if( null == cacheProcessor ){
            cacheProcessor = findBean(CacheProcessor.class);
        }

        if( null == cacheProcessor ){
            cacheProcessor = new CacheProcessor(store,handlerMethods);
        }

        LOG.info("缓存处理器使用 {}",store.getClass());

        try {
            if(!ObjectUtils.isEmpty(handlerMethods)){

                for(HandlerMethod handlerMethod : handlerMethods){
                    if( null != handlerMethod.getMethodAnnotation(ResponseBody.class)
                            || null != AnnotationUtils.findAnnotation(handlerMethod.getBeanType(),ResponseBody.class) ){
                        responseBodySet.add(handlerMethod.getMethod());
                    }
                }
            }
        }catch (Exception e){
            LOG.warn("",e);
        }


        try {
            if(!ObjectUtils.isEmpty(handlerMethods)){
                for(HandlerMethod handlerMethod : handlerMethods){
                    if( hasAnnotation(RequestBody.class,handlerMethod,true) ){
                        requestBodySet.add(handlerMethod.getMethod());
                    }
                }
            }
        }catch (Exception e){
            LOG.warn("",e);
        }



        try {
            Map<String,RequestMappingHandlerAdapter> map = context.getBeansOfType(RequestMappingHandlerAdapter.class);
            for(RequestMappingHandlerAdapter adapter : map.values()){
                if( adapter.getClass() == RequestMappingHandlerAdapter.class ){

                    this.setWebBindingInitializer(adapter.getWebBindingInitializer());
                    this.setInitBinderArgumentResolvers(adapter.getInitBinderArgumentResolvers());
                    this.setMessageConverters(adapter.getMessageConverters());
                    if( includeDefaults ){
                        this.setCustomArgumentResolvers(adapter.getCustomArgumentResolvers());
                        this.setCustomReturnValueHandlers( adapter.getCustomReturnValueHandlers() );
                    }
                    break;
                }
            }

        }catch (Exception e){
            LOG.warn("",e);
        }

        Optional<HttpMessageConverter<?>> optional = getMessageConverters()
                .stream()
                .filter(converter -> converter instanceof MappingJackson2HttpMessageConverter)
                .findFirst();



        for(HttpMessageConverter converter : getMessageConverters() ){

            if( converter instanceof MappingJackson2HttpMessageConverter ){

                MappingJackson2HttpMessageConverter jackson = (MappingJackson2HttpMessageConverter)converter;

                jackson.setObjectMapper(JSONUtils.objectMapperCopy());

                try {
                    //覆盖 mapper( 如果获取不到会出异常阻断执行 )
                    jackson.setObjectMapper(context.getBean(ObjectMapper.class));
                }catch (Exception e){

                }
                break;
            }
        }

    }


    @SuppressWarnings({ "unchecked", "rawtypes" })
	private boolean hasAnnotation(Class annotation , HandlerMethod handlerMethod, boolean includeParameters){

        if( null != handlerMethod.getMethodAnnotation(annotation) ){
            return true;
        }

        if( includeParameters ){
            MethodParameter[] methodParameter = handlerMethod.getMethodParameters();
            if( null != methodParameter ){
                for ( MethodParameter parameter : methodParameter ){
                    if( parameter.hasParameterAnnotation(annotation) ){
                        return true;
                    }
                }
            }
        }
        return false;
    }



	private String getHandlerMethodFullName(HandlerMethod handlerMethod){
		if(null != handlerMethod){
			Method method = handlerMethod.getMethod();
			StringBuilder sb = new StringBuilder(method.getDeclaringClass().getTypeName());
			sb.append(".");
			sb.append(method.getName());
			return sb.toString();
		}
		return "";
	}

	private <T> T findBean(Class<T> cls){
	    try {
	        return SpringContextUtils.getBean(cls);
        }catch (Exception e){
	        return null;
        }
    }
}
