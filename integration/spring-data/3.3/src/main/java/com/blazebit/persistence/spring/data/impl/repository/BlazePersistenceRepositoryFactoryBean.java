/*
 * SPDX-License-Identifier: Apache-2.0
 * Copyright Blazebit
 */

package com.blazebit.persistence.spring.data.impl.repository;

import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.view.EntityViewManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mapping.context.MappingContext;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;
import org.springframework.data.repository.core.support.TransactionalRepositoryFactoryBeanSupport;
import org.springframework.util.Assert;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.io.Serializable;

/**
 * @author Moritz Becker
 * @author Eugen Mayer
 * @since 1.6.9
 */
public class BlazePersistenceRepositoryFactoryBean<T extends Repository<S, ID>, S, ID extends Serializable> extends
        TransactionalRepositoryFactoryBeanSupport<T, S, ID> {

    private EntityManager entityManager;

    @Autowired
    private CriteriaBuilderFactory cbf;

    @Autowired
    private EntityViewManager evm;

    /**
     * Creates a new {@link BlazePersistenceRepositoryFactoryBean}.
     */
    protected BlazePersistenceRepositoryFactoryBean() {
        super(null);
    }

    /**
     * Creates a new {@link BlazePersistenceRepositoryFactoryBean} for the given repository interface.
     *
     * @param repositoryInterface must not be {@literal null}.
     */
    protected BlazePersistenceRepositoryFactoryBean(Class<? extends T> repositoryInterface) {
        super(repositoryInterface);
    }

    public boolean isSingleton() {
        return true;
    }

    /**
     * The {@link EntityManager} to be used.
     *
     * @param entityManager the entityManager to set
     */
    @PersistenceContext
    public void setEntityManager(EntityManager entityManager) {
        if (this.entityManager == null) {
            this.entityManager = entityManager;
        }
    }

    /*
     * (non-Javadoc)
     * @see com.blazebit.persistence.spring.data.impl.repository.BlazeRepositoryFactoryBeanSupport#setMappingContext(org.springframework.data.mapping.context.MappingContext)
     */
    @Override
    public void setMappingContext(MappingContext<?, ?> mappingContext) {
        super.setMappingContext(mappingContext);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.springframework.data.repository.support.
     * BlazeTransactionalRepositoryFactoryBeanSupport#doCreateRepositoryFactory()
     */
    @Override
    protected BlazePersistenceRepositoryFactory doCreateRepositoryFactory() {
        return createRepositoryFactory(entityManager);
    }

    /**
     * Returns a {@link RepositoryFactorySupport}.
     *
     * @param entityManager
     * @return
     */
    protected BlazePersistenceRepositoryFactory createRepositoryFactory(EntityManager entityManager) {
        return new BlazePersistenceRepositoryFactory(entityManager, cbf, evm);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() {
        Assert.notNull(entityManager, "EntityManager must not be null!");
        super.afterPropertiesSet();
    }

    public void setEscapeCharacter(char escapeCharacter) {
        // Needed to work with Spring Boot 2.1.4
    }

    public char getEscapeCharacter() {
        // Needed to work with Spring Boot 2.1.4
        return '\\';
    }

}