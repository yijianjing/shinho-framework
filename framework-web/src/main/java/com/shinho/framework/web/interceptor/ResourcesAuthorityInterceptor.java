package com.shinho.framework.web.interceptor;

import com.shinho.framework.web.AuthorityProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 基于资源的权限处理
 * Created by linxiaohui on 15/11/12.
 */
public class ResourcesAuthorityInterceptor extends HandlerInterceptorAdapter{

    private static final Logger LOG = LoggerFactory.getLogger(ResourcesAuthorityInterceptor.class);

    protected AuthorityProcess authorityProcess;
    protected ApplicationContext applicationContext;
    private boolean init = false;


    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response, Object handler) throws Exception {

        if( !init ){
            init( WebApplicationContextUtils.findWebApplicationContext(request.getServletContext()) );
        }

        if( handler instanceof HandlerMethod && null != authorityProcess){
            return authorityProcess.validationByResources( request.getRequestURI() );
        }
        return true;
    }

    private synchronized void init(ApplicationContext applicationContext){

        init = true;
        this.applicationContext = applicationContext;
        try {
            authorityProcess = applicationContext.getBean(AuthorityProcess.class);
        }catch (Exception e){
            LOG.warn("没有找到 authorityProcess {}",e.getMessage());
        }
    }

}
