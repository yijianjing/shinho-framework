package com.shinho.framework.jpa.event;


import com.shinho.framework.base.model.Describable;

/**
 * 事件类型
 */
public enum OperateType implements Describable {
    create {
        public String getDescription() {
            return "创建";
        }
    },
    remove {
        public String getDescription() {
            return "删除";
        }
    },
    update {
        public String getDescription() {
            return "修改";
        }
    },
    query {
        public String getDescription() {
            return "查询";
        }
    }
}