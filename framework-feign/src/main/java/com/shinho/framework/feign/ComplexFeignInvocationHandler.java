package com.shinho.framework.feign;

import feign.InvocationHandlerFactory;
import feign.Target;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

import static feign.Util.checkNotNull;

/**
 * Created by linxiaohui on 2017/7/19.
 */
public class ComplexFeignInvocationHandler implements InvocationHandler {

    private final Target target;
    private final Map<Method, InvocationHandlerFactory.MethodHandler> dispatch;

    public ComplexFeignInvocationHandler(Target target, Map<Method, InvocationHandlerFactory.MethodHandler> dispatch) {

        this.target = checkNotNull(target, "target");
        this.dispatch = checkNotNull(dispatch, "dispatch for %s", target);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {


        try {
            if ("equals".equals(method.getName())) {
                try {
                    Object otherHandler =
                            args.length > 0 && args[0] != null ? Proxy.getInvocationHandler(args[0]) : null;
                    return equals(otherHandler);
                } catch (IllegalArgumentException e) {
                    return false;
                }
            } else if ("hashCode".equals(method.getName())) {
                return hashCode();
            } else if ("toString".equals(method.getName())) {
                return toString();
            }


            FeignArgsHolder.put(method,args);
            return dispatch.get(method).invoke(args);
        }finally {
            FeignArgsHolder.remove();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ComplexFeignInvocationHandler) {
            ComplexFeignInvocationHandler other = (ComplexFeignInvocationHandler) obj;
            return target.equals(other.target);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return target.hashCode();
    }

    @Override
    public String toString() {
        return target.toString();
    }

}
