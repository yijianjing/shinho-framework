package com.shinho.framework.web.actuator;

import com.google.common.base.Strings;
import com.shinho.framework.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.ManagementServerProperties;
import org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMapping;
import org.springframework.boot.actuate.endpoint.mvc.MvcEndpointSecurityInterceptor;
import org.springframework.http.HttpStatus;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Objects;

public class EndpointHandlerMappingCustomizer implements org.springframework.boot.actuate.endpoint.mvc.EndpointHandlerMappingCustomizer {

    private static final String ipWhiteListKey = "${actuator.ip_white_list}";

    @Autowired
    private ManagementServerProperties managementServerProperties;
    @Value(ipWhiteListKey)
    private String ipWhiteList;


    @Override
    public void customize(EndpointHandlerMapping mapping) {

        PathMatcher pathMatcher = mapping.getPathMatcher();
        ManagementServerProperties.Security security = managementServerProperties.getSecurity();
        mapping.setSecurityInterceptor(new MvcEndpointSecurityInterceptor(security.isEnabled(),security.getRoles()){

            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                                     Object handler) throws Exception {

                boolean isOK = super.preHandle(request,response,handler);
                if( !isOK ){
                    return false;
                }

                if(Strings.isNullOrEmpty(ipWhiteList) || Objects.equals(ipWhiteList,ipWhiteListKey) ){
                    return true;
                }

                String ip = request.getRemoteAddr();
                String oIp = WebUtils.findRemoteAddr();
                for( String white : ipWhiteList.split(",") ){
                    if( pathMatcher.match(white,ip) || pathMatcher.match(white,oIp) ){
                        return true;
                    }
                }
                response.sendError(HttpStatus.UNAUTHORIZED.value(), "Full authentication is required to access this resource.");
                return false;
            }
        });
    }

    public String getIpWhiteList() {
        return ipWhiteList;
    }

    public void setIpWhiteList(String ipWhiteList) {
        this.ipWhiteList = ipWhiteList;
    }
}