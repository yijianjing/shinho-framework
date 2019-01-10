package com.shinho.framework.common.http;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.common.collect.Lists;
import com.shinho.framework.base.model.NameValue;
import com.shinho.framework.base.utils.StringUtils;
import com.shinho.framework.common.utils.JSONUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Created by linxiaohui on 15/11/11.
 */
public class Response {

    private int statusCode;

    private String responseBody;

    private Map<String,List<NameValue<String>>> headers;

    Response(){

    }



    Response(int statusCode,String responseBody,Map<String,List<NameValue<String>>> headers){
        this.statusCode = statusCode;
        this.responseBody = StringUtils.unicodeToString(responseBody);
        this.headers = headers;
    }


    public int getStatusCode() {
        return statusCode;
    }


    public String getResponseBody() {
        return responseBody;
    }


    public <T extends Serializable> T convert(Class<T> resultClass) throws IOException {

        return JSONUtils.toObject(responseBody,resultClass);
    }
    
    public <T extends Serializable> T convert(Class<T> resultClass,PropertyNamingStrategy pse) throws IOException {

        return JSONUtils.toObject(responseBody,resultClass,pse);
    }    
    
    public <T extends Serializable> T convert(TypeReference<T> typeReference, PropertyNamingStrategy pse) throws IOException {

        return JSONUtils.toObject(responseBody,typeReference,pse);
    }      
    
    public <T extends Serializable> T convert(Class<T> resultClass,String key) throws IOException {

        return JSONUtils.toObject( responseBody ,resultClass , key);
    }

    public <T extends Serializable> List<T> convertToList(Class<T> resultClass) throws IOException {

        return JSONUtils.toList(responseBody, resultClass);
    }

    public <T extends Serializable> List<T> convertToList(Class<T> resultClass,String key) throws IOException {

        return JSONUtils.toList(responseBody, resultClass,key);
    }

    public NameValue<String> getFirstHeader(String name){
        List<NameValue<String>> headers = getHeaders(name);
        if( null != headers && !headers.isEmpty() ){
            return headers.get(0);
        }
        return null;
    }

    public NameValue<String> getLastHeader(String name){

        List<NameValue<String>> headers = getHeaders(name);
        if( null != headers && !headers.isEmpty() ){
            return headers.get(headers.size()-1);
        }
        return null;
    }


    public List<NameValue<String>> getHeaders(String name){
        return headers.get(name);
    }

    public List<NameValue<String>> getAllHeaders(){

        List<NameValue<String>> result = Lists.newArrayList();
        headers.values().forEach(e -> result.addAll(e));
        return result;
    }
}
