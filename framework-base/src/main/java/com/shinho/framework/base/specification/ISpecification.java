package com.shinho.framework.base.specification;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public interface ISpecification<T> extends IDistinct{

    /**
     * 是否为否定的规则
     * @return
     */
    boolean isNegated();

    /**
     * 否定
     * @return
     */
    default ISpecification not(){
        return not(false);
    };

    /**
     * 否定
     * @param isNew 当为 true 时,返回一个新的(否定属性的)实体,原实体不变
     * @return
     */
    ISpecification not(boolean isNew);

    /**
     * 获取运算结果
     * @param context
     * @return
     */
    boolean evaluate(Object context);


    /**
     * 执行查询方法
     * @param collection
     * @return
     */
    default List<T> find(Collection<T> collection){

        if( null == collection || collection.isEmpty() ){
            return Collections.EMPTY_LIST;
        }
        Stream<T> stream = collection.stream().filter(e -> evaluate(e) );
        if( isDistinct() ){
            stream = stream.distinct();
        }
        return stream.collect(Collectors.toList());
    }

}


