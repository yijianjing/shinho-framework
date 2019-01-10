package com.shinho.framework.jpa.extend;

import com.google.common.collect.Lists;

import com.shinho.framework.base.specification.*;
import com.shinho.framework.base.specification.Expression;
import com.shinho.framework.base.specification.support.jpa.JoinProperty;
import com.shinho.framework.base.utils.DateUtils;
import org.apache.commons.beanutils.ConvertUtils;
import org.hibernate.jpa.criteria.path.AbstractPathImpl;

import javax.persistence.criteria.*;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ISpecification 工具类
 */
public class SpecificationUtils {

    public static class Specification implements org.springframework.data.jpa.domain.Specification {

        private ISpecification specification ;

        private Specification(ISpecification specification){
            this.specification = specification;
        }

        @Override
        public Predicate toPredicate(Root root, CriteriaQuery query, CriteriaBuilder criteriaBuilder) {
            return parse(specification,root,criteriaBuilder);
        }

        public boolean isDistinct() {
            return specification.isDistinct();
        }
    }

    /**
     * 解析规则
     * @param specification
     * @return
     */
    public static <T> org.springframework.data.jpa.domain.Specification<T> specification(ISpecification<T> specification){
        return  new Specification(specification);
    }

    /**
     * 判断是否是数字
     * @param javaType
     * @return
     */
    private static boolean isNumber(Class javaType){
        try {
            return javaType.asSubclass(Number.class) == javaType;
        }catch (Exception e){
            return false;
        }
    }

    /**
     * 对象转换为数组
     * @param value
     * @return
     */
    private static Object [] toArray(Object value, String split){

        if( null == value)
            throw new IllegalArgumentException("null not convert array");

        if( value instanceof String ){
            return ((String) value).split(split);
        }else if( value instanceof Collection){
            return ((Collection) value).toArray();
        }else if ( value.getClass().isArray()){
            return (Object []) value;
        }else {
            throw new IllegalArgumentException(value+" not convert array");
        }

    }

    /**
     *  类型转换
     * @param value     值
     * @param javaType  要转换的类型
     * @param isThrow   转换失败是否抛出异常( true IllegalArgumentException  | false 返回 null  )
     * @return
     */
    public static <T>T convert(Object value,Class<T> javaType,boolean isThrow){



        try {
            //( 临时方案,后续需在 convert 中注册 )

            //时间转换
            if(  javaType.isAssignableFrom(java.util.Date.class) ){
                try {
                    value = DateUtils.parse(String.valueOf(value));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //枚举支持
            if( javaType.isEnum() ){
                if( value instanceof String ){
                    return (T)Enum.valueOf((Class)javaType,(String)value);
                }else if ( value instanceof Number){
                    return javaType.getEnumConstants()[((Number) value).intValue()];
                }
            }
            return (T) ConvertUtils.convert(value,javaType);
        }catch (Exception e){
            if( isThrow ){
                throw new IllegalArgumentException(value + "convert to " + javaType+" error", e);
            }
            return null;
        }
    }


    public static Path getPath(Path<?> parent,Property property,CriteriaBuilder builder){

        String propertyName = property.name();
        if( propertyName.contains(".") ){ //多层级

            Path path = parent;
            for (String name : propertyName.split("\\.")) {
                if( property instanceof JoinProperty ){
                    path = findPathAndTryJoin(path,JoinProperty.join(name,((JoinProperty) property).joinType()).on(((JoinProperty) property).on()),builder);
                }else {
                    path = findPathAndTryJoin(path,Property.name(name),builder);
                }
            }
            return path;
        }else {
            return findPathAndTryJoin(parent,property,builder);
        }

    }


    /** 获取 path,并且 尝试 join **/
    private static Path findPathAndTryJoin(Path parent,Property property,CriteriaBuilder builder){


        JoinProperty joinProperty = property instanceof JoinProperty
                ? (JoinProperty) property
                : JoinProperty.join(property.name(), JoinProperty.JoinType.INNER);

        AbstractPathImpl _path = (AbstractPathImpl) parent.get(property.name());

        if( !_path.getAttribute().isCollection() ){ //不是集合属性,不需要 join
            return _path;
        }


        if( _path.getParentPath() instanceof From ){

            //父亲节点
            From _parent = (From) _path.getParentPath();
            Set<Join> joins = _parent.getJoins();

            Join join = null ;
            for ( Join _join : joins ){
                if( _join.getAttribute() == _path.getAttribute() ){ //存在
                    join = _join;
                    break;
                }
            }

            //不存在join,执行join
            if( null == join ){
                join = _parent.join(joinProperty.name(),JoinType.valueOf( joinProperty.joinType().name() ));
            }

            if( null == joinProperty.on() ){
                return join;
            }

            return join.on(parse(joinProperty.on(),parent,builder)) ;
        }

        return _path;
    }



    /**
     * 把 expression 解析为相应的 Predicate
     * @param expression    要解析的规则
     * @param root          root
     * @param builder       builder
     * @return
     */
    public static Predicate parseExpression(Expression expression, Path<?> root, CriteriaBuilder builder) {


        Object value = expression.getValue();
        Operator operator = expression.getOperator();

        //获取 path
        Path path = getPath(root,expression.getProperty(),builder);

        //需要的数据类型
        Class javaType = path.getJavaType();


        if( value instanceof Property){

            value = getPath(root,(Property)value,builder);
        } else if ( operator == Operator.IN || operator == Operator.NIN ) { // in 和 not ni 情况处理

            value = Stream.of(toArray(value, ",")).map(e -> convert(e, javaType, false))
                    .filter(e -> e != null)
                    .collect(Collectors.toList());
        }else if( !javaType.isInstance( value ) ){ //类型不正确

            value = convert(value,javaType,true);
        }

        switch (operator) {

            case NU:    return path.isNull();
            case NNU:   return path.isNotNull();
            case EMP:   return builder.isEmpty(findBasicPath(path));
            case NEMP:  return builder.isNotEmpty(findBasicPath(path));

            case EQ:    return value instanceof Path
                    ? builder.equal(path,(Path)value)
                    : builder.equal(path,value);

            case NEQ:   return value instanceof Path
                    ? builder.notEqual(path,(Path)value)
                    : builder.notEqual(path,value);

            case IN:    return value instanceof Path
                    ? builder.in(path).value((Path)value)
                    : builder.in(path).value(value);

            case NIN:   return value instanceof Path
                    ? builder.in(path).value((Path)value).not()
                    : builder.in(path).value(value).not();

            case GE:    return value instanceof Path
                    ? builder.greaterThanOrEqualTo(path, (Path) value)
                    : builder.greaterThanOrEqualTo(path, (Comparable) value);

            case LT:    return value instanceof Path
                    ? builder.lessThan(path,(Path)value)
                    : builder.lessThan(path,(Comparable)value);

            case LE:    return value instanceof Path
                    ? builder.lessThanOrEqualTo(path,(Path)value)
                    : builder.lessThanOrEqualTo(path, (Comparable) value);

            case GT:    return value instanceof Path
                    ? builder.greaterThan(path,(Path)value)
                    : builder.greaterThan(path, (Comparable) value);

            case CI:    return value instanceof Path
                    ? builder.like(path,like(builder,true,(Path)value,true))
                    : builder.like(path,"%"+value+"%");

            case NCI:   return value instanceof Path
                    ? builder.notLike(path,like(builder,true,(Path)value,true))
                    : builder.notLike(path,"%"+value+"%");

            case BW:    return value instanceof Path
                    ? builder.like(path,like(builder,false,(Path)value,true))
                    : builder.like(path,  value + "%" );

            case NBW:   return value instanceof Path
                    ? builder.notLike(path,like(builder,false,(Path)value,true))
                    : builder.notLike(path, value + "%");

            case EW:    return value instanceof Path
                    ? builder.like(path,like(builder,true,(Path)value,false))
                    : builder.like(path, "%" + value );

            case NEW:   return value instanceof Path
                    ? builder.notLike(path,like(builder,true,(Path)value,false))
                    : builder.notLike(path, "%" + value);


            default:    return builder.disjunction();
        }
    }

    private static <X> Path<X> findBasicPath(Path<X> path){
        if( path instanceof Join ){
            String name = ((Join) path).getAttribute().getName();
            return path.getParentPath().get(name);
        }
        return path;
    }

    private static javax.persistence.criteria.Expression like(CriteriaBuilder builder ,
                                                              boolean left,
                                                              javax.persistence.criteria.Expression value,
                                                              boolean right){

        if( left ){
            value = builder.concat("%",value);
        }
        if( right ){
            value = builder.concat(value,"%");
        }
        return value;
    }



    /**
     * 解析规则
     * @param spe       要解析的规则
     * @param root
     * @param builder
     * @return
     */
    public static Predicate parse(ISpecification spe , Path<?> root, CriteriaBuilder builder){

        Predicate predicate = builder.conjunction();
        if( spe == null ){
            return predicate;
        } else if( spe instanceof Expression){
            predicate = parseExpression((Expression)spe,root,builder);
        }else if( spe instanceof SpecificationComposite){

            SpecificationComposite group = (SpecificationComposite)spe;
            List<Predicate> predicates = Lists.newArrayListWithExpectedSize(group.getSpecifications().size());

            predicates.addAll(group.getSpecifications().stream()
                    .map(_specification -> parse(_specification, root, builder))
                    .collect(Collectors.toList()));
            if( group.logic() == Logic.AND  ){
                predicate = builder.and(predicates.toArray(new Predicate[]{} ));
            }else {
                predicate = builder.or(predicates.toArray(new Predicate[]{} ));
            }
        }
        return spe.isNegated() ? predicate.not() : predicate;
    }

}
