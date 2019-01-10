package com.shinho.framework.base.utils;


import com.shinho.framework.base.template.CompensationTemplate;
import com.shinho.framework.base.template.Template;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;


/**
 * Created by linxiaohui on 16/1/20.
 */
public class TemplateUtils {


    private static final Logger logger = LoggerFactory.getLogger(TemplateUtils.class);


    public static <T> T execute(int retries,Template <T> template) throws Exception {
        return TemplateUtils.execute(template,retries,Exception.class);
    }

    @SafeVarargs
    public static <T> T execute(Template <T> template, int retries, Class<? extends Exception> ... retriesFor) throws Exception {

        if(  null == retriesFor || retries<1 )
            return template.execute();

        for(  int i=0; i<retries; i++ ){
            try {
                return template.execute();
            }catch (Exception e){

                if( needCompensation( e,retriesFor ) ){
                    if( logger.isInfoEnabled()  ){
                        logger.info(MessageFormat.format("出现异常{0} 尝试第{1}次补偿",e,i+1));
                    }
                    continue;
                }
                throw e;
            }
        }
        return template.execute();
    }

    public static <T> T execute(Template<T> template) throws Exception {

        if( template instanceof CompensationTemplate ){
            CompensationTemplate<T> compensation = (CompensationTemplate) template;
            return execute(template,compensation.retries(),compensation.retriesFor());
        }else {
            return template.execute();
        }
    }

    public static <T> T find(Template<T> template){

        try {
            return execute(template);
        }catch (Exception e){
            if( logger.isDebugEnabled() ){
                logger.debug("",e);
            }
            return null;
        }
    }

    private static boolean needCompensation(Exception e,Class<? extends Exception> [] retriesFor){

        if( null ==e || null == retriesFor )
            return false;

        for(Class<? extends Exception> c : retriesFor ){
            if( null != c && c.isInstance(e) ){
                return true;
            }
        }
        return false;
    }
}
