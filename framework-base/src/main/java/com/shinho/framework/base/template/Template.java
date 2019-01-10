package com.shinho.framework.base.template;

/**
 * Created by linxiaohui on 16/1/20.
 */
public interface Template <T> {

    T execute() throws Exception;
}
