package com.shinho.framework.jpa.extend;

import com.shinho.framework.base.specification.ISpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.data.jpa.repository.query.QueryUtils.toOrders;

/**
 * 扩展为支持 JpaSpecificationExecutorExt
 *              可设置事务为,来优化减少只读事务开启 @Transactional(propagation = Propagation.SUPPORTS)
 * @param <T>
 * @param <ID>
 */
@NoRepositoryBean
@Transactional(readOnly = true)
public class SimpleJpaRepositoryExt<T, ID extends Serializable>
        extends org.springframework.data.jpa.repository.support.SimpleJpaRepository<T, ID>
        implements JpaSpecificationExecutorExt<T> {

    private EntityManager em;

    public SimpleJpaRepositoryExt(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
        this.em = entityManager;
    }

    public SimpleJpaRepositoryExt(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
        this.em = em;
    }

    @Override
    public T findOne(ISpecification<T> spec) {
        return super.findOne( SpecificationUtils.specification(spec) );
    }

    @Override
    public List<T> findAll(ISpecification<T> spec) {
        return super.findAll( SpecificationUtils.specification(spec) ) ;
    }

    @Override
    public Page<T> findAll(ISpecification<T> spec, Pageable pageable) {
        return super.findAll( SpecificationUtils.specification(spec),pageable ) ;
    }

    @Override
    public List<T> findAll(ISpecification<T> spec, Sort sort) {
        return super.findAll(SpecificationUtils.specification(spec),sort) ;
    }

    @Override
    public long count(ISpecification<T> spec) {
        return super.count( SpecificationUtils.specification(spec) ) ;
    }

    protected <S extends T> TypedQuery<S> getQuery(Specification<S> spec, Class<S> domainClass, Sort sort) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<S> query = builder.createQuery(domainClass);

        if( spec instanceof com.shinho.framework.jpa.extend.SpecificationUtils.Specification ){
            query.distinct(((SpecificationUtils.Specification) spec).isDistinct());
        }

        Root<S> root = applySpecificationToCriteria(spec, domainClass, query);
        query.select(root);

        if (sort != null) {
            query.orderBy(toOrders(sort, root, builder));
        }

        return applyRepositoryMethodMetadata(em.createQuery(query));
    }

    protected <S extends T> TypedQuery<Long> getCountQuery(Specification<S> spec, Class<S> domainClass) {

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);

        if( spec instanceof com.shinho.framework.jpa.extend.SpecificationUtils.Specification ){
            query.distinct(((SpecificationUtils.Specification) spec).isDistinct());
        }

        Root<S> root = applySpecificationToCriteria(spec, domainClass, query);

        if (query.isDistinct()) {
            query.select(builder.countDistinct(root));
        } else {
            query.select(builder.count(root));
        }

        // Remove all Orders the Specifications might have applied
        query.orderBy(Collections.<Order> emptyList());

        return em.createQuery(query);
    }

    private <S, U extends T> Root<U> applySpecificationToCriteria(Specification<U> spec, Class<U> domainClass,
                                                                  CriteriaQuery<S> query) {

        Assert.notNull(domainClass, "Domain class must not be null!");
        Assert.notNull(query, "CriteriaQuery must not be null!");

        Root<U> root = query.from(domainClass);

        if (spec == null) {
            return root;
        }

        CriteriaBuilder builder = em.getCriteriaBuilder();
        Predicate predicate = spec.toPredicate(root, query, builder);

        if (predicate != null) {
            query.where(predicate);
        }

        return root;
    }

    private <S> TypedQuery<S> applyRepositoryMethodMetadata(TypedQuery<S> query) {

        if (getRepositoryMethodMetadata() == null) {
            return query;
        }

        LockModeType type = getRepositoryMethodMetadata().getLockModeType();
        TypedQuery<S> toReturn = type == null ? query : query.setLockMode(type);

        applyQueryHints(toReturn);

        return toReturn;
    }

    private void applyQueryHints(Query query) {

        for (Map.Entry<String, Object> hint : getQueryHints().entrySet()) {
            query.setHint(hint.getKey(), hint.getValue());
        }
    }
}