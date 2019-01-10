package com.shinho.framework.feign;

import com.shinho.framework.base.feign.Constants;
import com.shinho.framework.base.model.KeyValue;
import com.shinho.framework.base.utils.BeanMapUtils;
import com.shinho.framework.common.utils.JSONUtils;
import feign.RequestTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.*;
import java.util.stream.Collectors;


/**
 * Created by linxiaohui on 2017/7/19.
 */
public class ComplexFeignInterceptor implements feign.RequestInterceptor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ComplexFeignInterceptor.class);
    private static final BeanMapUtils.BeanToMapStyle [] BEAN_TO_MAP_STYLES = {
            BeanMapUtils.BeanToMapStyle.collectionToIndex,
            BeanMapUtils.BeanToMapStyle.mapToIndex,
            BeanMapUtils.BeanToMapStyle.arrayToList};


    @Override
    public void apply(RequestTemplate template) {

        KeyValue<Method,List<FeignArgsHolder.FeignMethodParameter>> keyValue = FeignArgsHolder.get();

        if( null == keyValue ){
            LOGGER.warn("没有开启 FeignArgsHolder ,请配置 ComplexFeignInvocationHandler ");
            return;
        }

        byte [] body = template.body();
        if( null != body && body.length > 0 ){

            LOGGER.debug("方法 {} requestBody 中已有数据,跳过处理",keyValue.getKey());
            return;
        }



        //放入请求头( 欣和框架扩展的 feign 请求 )
        template.header(Constants.IS_FEIGN_EXTENDED,"true");


        List<FeignArgsHolder.FeignMethodParameter> ext = keyValue.getValue().stream().filter( e -> e.isExtended() )
                .collect(Collectors.toList());


        if(Objects.equals("GET",template.method())){

            for(FeignArgsHolder.FeignMethodParameter parameter : ext ){

                if( parameter.isExtended() ){

                    String argName = parameter.getName();
                    Object arg = parameter.getValue();
                    Map<Object,Object> map = BeanUtils.isSimpleProperty(arg.getClass())
                            ? Collections.singletonMap(argName,arg)
                            : BeanMapUtils.beanToMap(arg,true,argName,BEAN_TO_MAP_STYLES);

                    template = addQueryMapQueryParameters(map,template );
                }

            }
        }else {

            try {

                Map args = ext.stream().collect(Collectors.toMap( k -> k.getName() , v -> v.getValue() ));
                template.body(JSONUtils.toJSON(args));
            }catch (Exception e){
                throw new RuntimeException(e);
            }
        }


    }


    private RequestTemplate addQueryMapQueryParameters(Map<Object, Object> queryMap, RequestTemplate mutable) {


      for (Map.Entry<Object, Object> currEntry : queryMap.entrySet()) {

          if( currEntry.getKey().getClass() != String.class ){
              throw new IllegalArgumentException("参数名必须为字符串");
          }


        Collection<String> values = new ArrayList<String>();

        boolean encoded = true;
        Object currValue = currEntry.getValue();
        if (currValue instanceof Iterable<?>) {
          Iterator<?> iter = ((Iterable<?>) currValue).iterator();
          while (iter.hasNext()) {
            Object nextObject = iter.next();
            values.add(nextObject == null ? null : encoded ? nextObject.toString() : urlEncode(nextObject.toString()));
          }
        } else {
          values.add(currValue == null ? null : encoded ? currValue.toString() : urlEncode(currValue.toString()));
        }

        mutable.query(true, encoded ? (String) currEntry.getKey() : urlEncode(currEntry.getKey()), values);
      }
      return mutable;
    }

    public static String urlEncode(Object arg) {
        try {
            return URLEncoder.encode(String.valueOf(arg), feign.Util.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }


}
