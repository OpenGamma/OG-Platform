/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.util.Date;

import com.opengamma.core.security.Security;

/**
 * Partial implementation of BeanOperation for simple cases.
 * @param <S> the security
 * @param <H> the Hibernate bean
 */
public abstract class AbstractSecurityBeanOperation<S extends Security, H extends SecurityBean>
    implements SecurityBeanOperation<S, H> {

  /** The Hibernate bean class. */
  private final Class<? extends H> _beanClass;
  /** The security class. */
  private final Class<? extends S> _securityClass;
  /** The security type. */
  private final String _securityType;

  /**
   * Creates an instance.
   * @param securityType  the security type, not null
   * @param securityClass  the security class, not null
   * @param beanClass  the Hibernate bean class, not null
   */
  protected AbstractSecurityBeanOperation(final String securityType, final Class<? extends S> securityClass, final Class<? extends H> beanClass) {
    _securityType = securityType;
    _securityClass = securityClass;
    _beanClass = beanClass;
  }

  //-------------------------------------------------------------------------
  @Override
  public Class<? extends H> getBeanClass() {
    return _beanClass;
  }

  @Override
  public Class<? extends S> getSecurityClass() {
    return _securityClass;
  }

  @Override
  public String getSecurityType() {
    return _securityType;
  }

  @Override
  public H resolve(OperationContext context, HibernateSecurityMasterDao secMasterSession, Date now, H bean) {
    return bean;
  }

  @Override
  public void postPersistBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, Date effectiveDate, H bean) {
    // No op
  }

}
