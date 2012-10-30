/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.masterdb.security.hibernate;

import java.util.Date;

import com.opengamma.core.security.Security;

/**
 * Operations to convert a real entity to/from a bean and hence to/from the Hibernate database.
 * @param <S> the security
 * @param <H> the Hibernate bean
 */
public interface SecurityBeanOperation<S extends Security, H extends SecurityBean> {

  /**
   * Returns the bean implementation class.
   * @return the Hibernate bean class
   */
  Class<? extends H> getBeanClass();

  /**
   * Returns the security implementation class.
   * @return the security class
   */
  Class<? extends S> getSecurityClass();

  /**
   * Returns the security type name.
   * @return the bean class
   */
  String getSecurityType();

  /**
   * Create a bean representation of the security. Does not need to set the base properties
   * of SecurityBean.
   * @param context  the context
   * @param secMasterSession  the DAO
   * @param security  the security
   * @return the created Hibernate bean
   */
  H createBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, S security);

  /**
   * Convert a bean representation to a security.
   * @param context  the context
   * @param bean  the Hibernate bean
   * @return the created security
   */
  S createSecurity(OperationContext context, H bean);

  /**
   * Loads additional (deep) data for a security bean. For example to implement date constrained relationships
   * that Hibernate alone can't deal with. May update the supplied bean, and return it, or return a new bean.
   * @param context  the context
   * @param secMasterSession  the DAO
   * @param now  the current time
   * @param bean  the Hibernate bean
   * @return the resolved Hibernate bean
   */
  H resolve(OperationContext context, HibernateSecurityMasterDao secMasterSession, Date now, H bean);

  /**
   * Additional persistence required after the main bean has been passed to Hibernate. Used with resolve to
   * @param context  the context
   * @param secMasterSession  the DAO
   * @param effectiveDate  the effective time
   * @param bean  the Hibernate bean
   * store data Hibernate alone can't deal with.
   */
  void postPersistBean(OperationContext context, HibernateSecurityMasterDao secMasterSession, Date effectiveDate, H bean);

}
