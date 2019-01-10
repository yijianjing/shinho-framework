package com.shinho.framework.web;


import com.shinho.framework.web.exception.NoAuthorityException;

/**
 * Created by linxiaohui on 15/11/12.
 */
public interface AuthorityProcess {

    boolean validationByPermissions(String... auth) throws NoAuthorityException;

    boolean validationByResources(String resources) throws NoAuthorityException;
}
