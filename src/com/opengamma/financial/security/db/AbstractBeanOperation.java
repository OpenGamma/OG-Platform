/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

import com.opengamma.engine.security.Security;

/**
 * Partial implementation of BeanOperation for simple cases.
 * @param <S> Security
 * @param <SBean> SecurityBean
 */
public abstract class AbstractBeanOperation<S extends Security, SBean extends SecurityBean> extends Converters implements BeanOperation<S, SBean> {
  
  private final Class<? extends SBean> _beanClass;
  private final Class<? extends S> _securityClass;
  private final String _securityType;
  
  protected AbstractBeanOperation(final String securityType, final Class<? extends S> securityClass, final Class<? extends SBean> beanClass) {
    _securityType = securityType;
    _securityClass = securityClass;
    _beanClass = beanClass;
  }
  
  @Override
  public Class<? extends SBean> getBeanClass() {
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
  public SBean resolve(HibernateSecurityMasterDao secMasterSession, Date now, SBean bean) {
    return bean;
  }
  
  @Override
  public void postPersistBean(HibernateSecurityMasterDao secMasterSession, Date effectiveDate, SBean bean) {
    // No op
  }
  
}