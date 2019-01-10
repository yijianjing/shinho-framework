package com.shinho.framework.feign;

import com.fasterxml.jackson.databind.JsonNode;
import com.shinho.framework.base.feign.Constants;
import com.shinho.framework.common.utils.JSONUtils;
import com.shinho.framework.web.request.BodyReaderHttpServletRequestWrapper;
import com.shinho.framework.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Conventions;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Objects;

public class FeignArgumentResolver implements HandlerMethodArgumentResolver {

	private static final Logger logger = LoggerFactory.getLogger(FeignArgumentResolver.class);
	private static final String FEIGN_ARGUMENT_JSON_OBJECT="FEIGN_ARGUMENT_JSON_OBJECT";


	@Override
	public boolean supportsParameter(MethodParameter parameter) {

		//跳过处理含有 @RequestBody 注解的参数
		if( parameter.hasMethodAnnotation(RequestBody.class) ){
			return false;
		}

		HttpServletRequest request = WebUtils.getRequest();
		String isFeign = request.getHeader(Constants.IS_FEIGN_EXTENDED);
		
		return !Objects.equals(request.getMethod().toUpperCase(),"GET") && isFeign != null ;
	}
	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {


		//获取参数名
		String name = Conventions.getVariableNameForParameter(parameter);
		//获取request
		HttpServletRequest _webRequest = ((ServletWebRequest)webRequest).getRequest();
		//获取数据
		String json = BodyReaderHttpServletRequestWrapper.requestBody(_webRequest);

		if( null == json ){
			logger.warn("无法获取requestBody中的数据,请检查是否开启 BodyReaderHttpServletRequestWrapper " +
					", 推荐开启 HandlerAdapter ,HandlerAdapter 默认开启 BodyReaderHttpServletRequestWrapper 相关功能 ");
			return null;
		}

		JsonNode jsonNode = (JsonNode)_webRequest.getAttribute(FEIGN_ARGUMENT_JSON_OBJECT);
		if( null == jsonNode ){
			jsonNode = JSONUtils.toJSON(json);
			_webRequest.setAttribute(FEIGN_ARGUMENT_JSON_OBJECT,jsonNode);
		}
		String argJson = keyNodeJson(jsonNode, name);

		Class argClass = parameter.getParameterType();
		if( parameter.getParameterType().isAssignableFrom(Collection.class) ){
			return JSONUtils.toList(argJson, argClass);
		}else {
			return JSONUtils.toObject(argJson, argClass);
		}
	}

	private String keyNodeJson(JsonNode jsonNode, String key) {
		JsonNode keyNode = keyNode(jsonNode, key);
		String json = keyNode.textValue();
		if(null == json || com.google.common.base.Objects.equal("null", json)) {
			json = keyNode.toString();
		}

		return json;
	}
	private JsonNode keyNode(JsonNode jsonNode, String key) {
		JsonNode keyNode = jsonNode.path(key);
		if(null != keyNode) {
			keyNode = jsonNode.get(key);
		}

		return keyNode;
	}
}