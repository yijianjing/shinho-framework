package com.shinho.framework.jpa.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;

/**
 * 事件侦听器
 */
public class ActionAuditListener  {

    private static final Logger logger = LoggerFactory.getLogger(ActionAuditListener.class);

    @PostLoad
    private void postLoad(Object entity){

    }

    @PostPersist
    private void postPersist(Object entity) {
        //判断是否是操作审计实体
        notice(new ActionEvent(OperateType.create,entity));

    }

    @PostRemove
    private void PostRemove(Object entity){

        notice(new ActionEvent(OperateType.remove,entity));
    }


    @PostUpdate
    private void PostUpdate(Object entity){

        notice(new ActionEvent(OperateType.update,entity));
    }


    protected void notice(ActionEvent actionEvent) {

        logger.info("{} 执行了 {} 操作",actionEvent.getSource(),actionEvent.getOperateType().getDescription());
        ActionEventManager.notice( actionEvent );
    }
}