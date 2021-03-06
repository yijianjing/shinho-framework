package com.shinho.framework.base.model;

/**
 * Created by linxiaohui on 2016/11/7.
 */
public enum  CommonStatus implements Status{

    normal("正常"),locking("锁定"),used("使用"),cancel("作废");

    private String desc;

    CommonStatus(String desc){
        this.desc = desc;;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    public Status end() {
        return used;
    }

    @Override
    public Status cancel() {
        return cancel;
    }
}
