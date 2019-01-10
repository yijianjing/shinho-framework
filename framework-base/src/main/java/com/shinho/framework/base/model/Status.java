package com.shinho.framework.base.model;

import java.io.Serializable;

public interface Status extends Describable ,Serializable{

    Status end();
    Status cancel();

    default boolean isEnd()     { return this == end(); };
    default boolean isCancel()  { return this == cancel(); };
}
