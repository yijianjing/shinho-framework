package com.shinho.framework.base.specification;

/**
 * Created by linxiaohui on 2017/4/19.
 */
public class Nothing implements ISpecification {

    public static final Nothing NOTHING = new Nothing();

    private Nothing(){

    }

    @Override
    public boolean isNegated() {
        return false;
    }

    @Override
    public ISpecification not(boolean isNew) {
        return this;
    }

    @Override
    public boolean evaluate(Object context) {
        return true;
    }

    /**
     * 是否去重
     **/
    @Override
    public boolean isDistinct() {
        return false;
    }
}
