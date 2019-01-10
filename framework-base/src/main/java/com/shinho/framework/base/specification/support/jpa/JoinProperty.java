package com.shinho.framework.base.specification.support.jpa;

import com.shinho.framework.base.specification.Property;
import com.shinho.framework.base.specification.SpecificationComposite;

public class JoinProperty extends Property{

    public enum JoinType {
        LEFT,
        RIGHT,
        INNER
    }

    private JoinType joinType = JoinType.INNER;
    private SpecificationComposite on ;

    protected JoinProperty(String propertyName,JoinType joinType) {

        super(propertyName);

        if( null == joinType ){
            throw new IllegalArgumentException("joinType must not null");
        }

        this.joinType = joinType;
    }


    public static JoinProperty join(String propertyName,JoinType joinType){
        return new JoinProperty(propertyName,joinType);
    }

    public JoinType joinType() {
        return joinType;
    }

    public JoinProperty on(SpecificationComposite specificationComposite){


        if( null == specificationComposite ){
            return this;
        }

        if( null == on ){
            on = specificationComposite;
        }else {
            on.add(specificationComposite);
        }

        return this;
    }

    public SpecificationComposite on() {
        return on;
    }
}
