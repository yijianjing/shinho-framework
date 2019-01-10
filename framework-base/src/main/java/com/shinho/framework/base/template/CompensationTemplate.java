package com.shinho.framework.base.template;

import com.shinho.framework.base.template.Template;

/**
 * Created by linxiaohui on 15/12/8.
 */
public interface CompensationTemplate<T> extends Template<T> {

    int retries();
    Class<? extends Exception>[] retriesFor();

}
