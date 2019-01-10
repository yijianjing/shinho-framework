package com.shinho.framework.jpa;

import com.shinho.framework.base.template.CompensationTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

/**
 * Created by linxiaohui on 15/12/8.
 */
public abstract class ObjectOptimisticLockingCompensation<T> implements CompensationTemplate<T> {

    @SuppressWarnings("unchecked")
	private static final Class<? extends Exception> [] retriesFor = new Class[]{ObjectOptimisticLockingFailureException.class};
    private int retries=3;
    public ObjectOptimisticLockingCompensation(){

    }

    public ObjectOptimisticLockingCompensation(int retries){
        this.retries=3;
    }

    public int retries() {
        return retries;
    }

    public Class<? extends Exception>[] retriesFor() {
        return retriesFor;
    }

}
