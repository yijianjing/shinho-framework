package com.shinho.framework.common.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.google.common.base.Objects;
import com.shinho.framework.base.utils.AssertUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by linxiaohui on 15/11/12.
 */
public class JSONUtils {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(JSONUtils.class);

    static {
    	objectMapper.setSerializationInclusion(Include.NON_NULL);
    	objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper objectMapperCopy(){
        return objectMapper.copy();
    }
    
    public static ObjectMapper getObjectMapper() {
    	return objectMapper;
    }

    /**
     * 对象转 json String
     * @param obj   要转的对象
     * @return
     * @throws JsonProcessingException
     */
    public static String toJSON(Object obj) throws JsonProcessingException {
    	
        return objectMapper.writeValueAsString(obj);
    }

    /**
     * 对象转 json String
     * @param obj           要转的对象
     * @param defaultValue  默认值(转换失败,或要转的对象不存在时返回)
     * @return
     */
    public static String toJSON(Object obj,String defaultValue)  {

    	if( null == obj ) 
    		return null;
    	try {
            return objectMapper.writeValueAsString(obj);
		} catch (Exception e) {
			if( logger.isDebugEnabled() ){ logger.debug("",e); }
			return defaultValue;
		}
    }

    /**
     * json 字符串转指定类型的
     * @param json          json String
     * @param resultClass   要转为的类型
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T toObject(String json,Class<T> resultClass) throws IOException {

        AssertUtils.notNull(resultClass,"参数 resultClass 不能为空!");
        return objectMapper.readValue(json,resultClass);
    }

    /**
     * json 字符串转为对象
     * @param json          json String
     * @param resultClass   要转为的类型
     * @param pns           命名策略
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T toObject(String json,Class<T> resultClass,PropertyNamingStrategy pns) throws IOException {

        AssertUtils.notNull(resultClass,"参数 resultClass 不能为空!");
        if(null != pns){
			ObjectMapper objectMapperCopy = objectMapper.copy();
			objectMapperCopy.setPropertyNamingStrategy(pns);
			return objectMapperCopy.readValue(json,resultClass);
		}
		
		return objectMapper.readValue(json,resultClass);
    }

    /**
     * json String 转对象
     * @param json              json String
     * @param typeReference     类型引用
     * @param pns               命名策略
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference, PropertyNamingStrategy pns) throws IOException {

        AssertUtils.notNull(typeReference,"参数 typeReference 不能为空!");
        if(null != pns){
			ObjectMapper objectMapperCopy = objectMapper.copy();
			objectMapperCopy.setPropertyNamingStrategy(pns);
			return objectMapperCopy.readValue(json,typeReference);
		}
		
		return objectMapper.readValue(json,typeReference);
    }

    /***
     * json String 转对象
     * @param json          json String
     * @param resultClass   要转的类型
     * @param key           要转的 key
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> T toObject(String json,Class<T> resultClass,String key) throws IOException {

        AssertUtils.notNull(resultClass,"参数 resultClass 不能为空!");

        JsonNode jsonNode = JSONUtils.toJSON(json);
        return JSONUtils.toObject( keyNodeJson(jsonNode,key) ,resultClass);
    }


    /**
     * json 转为 list
     * @param json          json String
     * @param resultClass   要转的类型
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> List<T> toList(String json,Class<T> resultClass) throws IOException {

        AssertUtils.notNull(resultClass,"参数 resultClass 不能为空!");
        JavaType javaType = objectMapper.getTypeFactory().constructParametrizedType(List.class, List.class,resultClass);
        return objectMapper.readValue(json, javaType);

    }

    /**
     * json 转为 list
     * @param json          要转的 json
     * @param resultClass   要转的类型
     * @param key           要转的 key
     * @param <T>
     * @return
     * @throws IOException
     */
    public static <T> List<T> toList(String json,Class<T> resultClass,String key) throws IOException {

        AssertUtils.notNull(resultClass,"参数 resultClass 不能为空!");
        AssertUtils.notNull(resultClass,"参数 key 不能为空!");
        JsonNode jsonNode = JSONUtils.toJSON(json);
        return JSONUtils.toList(keyNodeJson(jsonNode,key), resultClass);
    }


    /**
     * json String 转换为 jsonNode
     * @param json
     * @return
     * @throws IOException
     */
    public static JsonNode toJSON(String json) throws IOException {

        return objectMapper.readTree(json);
    }

    /**
     * json String 转换为 map
     * @param jsonStr
     * @param <T>
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    public static <T> Map<String, T> json2map(String jsonStr) throws  IOException{
        return objectMapper.readValue(jsonStr, LinkedHashMap.class);
    }

    private static JsonNode keyNode(JsonNode jsonNode, String key){
        JsonNode keyNode = jsonNode.path(key);
        if( null != keyNode ){
            keyNode = jsonNode.get(key);
        }
        return keyNode;
    }

    private static String keyNodeJson(JsonNode jsonNode, String key){
        JsonNode keyNode = keyNode(jsonNode,key);
        String json = keyNode.textValue();
        if( null == json || Objects.equal("null",json))
            json = keyNode.toString();
        return json;
    }
}
