package com.shinho.framework.feign;

import com.google.common.collect.Maps;
import feign.Feign;
import feign.MethodMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.netflix.feign.AnnotatedParameterProcessor;
import org.springframework.core.convert.ConversionService;
import org.springframework.web.bind.annotation.RequestBody;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;


public class ComplexFeignSpringMvcContract extends org.springframework.cloud.netflix.feign.support.SpringMvcContract {

	private static final Logger LOGGER = LoggerFactory.getLogger(ComplexFeignSpringMvcContract.class);

	private final Map<String, Method> processedMethods = Maps.newHashMap();
	private final Map<Method, Boolean> requestBodyMethods = Maps.newHashMap();


	public ComplexFeignSpringMvcContract() {

	}

	public ComplexFeignSpringMvcContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors) {
		super(annotatedParameterProcessors);
	}

	public ComplexFeignSpringMvcContract(List<AnnotatedParameterProcessor> annotatedParameterProcessors, ConversionService conversionService) {
		super(annotatedParameterProcessors, conversionService);
	}

	@Override
	public MethodMetadata parseAndValidateMetadata(Class<?> targetType, Method method) {

		String configKey = Feign.configKey(targetType, method);
		LOGGER.debug( "方法 {} ,configKey {}" ,method,configKey);
		this.processedMethods.put(configKey, method);

		if( method.getParameterCount() < 1 ){
			requestBodyMethods.put(method,false);
		}else {
			boolean isRequestBodyMethod = Stream.of(method.getParameters())
					.anyMatch( parameter -> null != parameter.getAnnotation(RequestBody.class) );
			requestBodyMethods.put(method,isRequestBodyMethod);
			if( isRequestBodyMethod ){
				LOGGER.debug( "方法 {} ,configKey {} 中含有 @RequestBody 注解" ,method,configKey);
			}
		}

		return super.parseAndValidateMetadata(targetType,method);
	}

	@Override
	protected boolean processAnnotationsOnParameter(MethodMetadata data, Annotation[] annotations, int paramIndex) {

		//获取方法
		Method method = this.processedMethods.get(data.configKey());


		//@RequestBody 注解不处理
		if( Stream.of(annotations).anyMatch( annotation -> annotation.annotationType() == RequestBody.class  ) ){
			return false;
		}

		//是否含有 @PathVariable,@RequestHeader,@RequestParam 其中一个注解
		if( super.processAnnotationsOnParameter(data,annotations,paramIndex) ){
			return true;
		}


		//判断注解是否使用
		if(Objects.equals(requestBodyMethods.get(method),true) ){
			String message = MessageFormat.format("方法 {0} 使用了 @RequestBody " +
					",其他参数必须含有 @PathVariable,@RequestHeader,@RequestParam 其中一个注解!",method);
			throw new Error(message);
		}

		//获取参数
		Parameter parameter = method.getParameters()[paramIndex];

		//基本数据类型且支持
		if( BeanUtils.isSimpleProperty( parameter.getType() ) ){

			if(!parameter.isNamePresent() ){
				throw new Error(method+" 不支持参数名称,请使用 @RequestParam 注解,或在编译时增加 -parameters 参数");
			}
		}


		FeignArgsHolder.addExtendArgsIndex(method,paramIndex);
		return true;
	}

}
