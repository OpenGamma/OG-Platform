/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import java.util.Date;

import com.opengamma.engine.security.Security;

/**
 * Operations to convert a real entity to/from a bean and hence to/from the Hibernate database.
 */
/* package */ interface BeanOperation<S extends Security, SBean extends SecurityBean> {
  
  /**
   * Returns the bean implementation class.
   */
  Class<? extends SBean> getBeanClass();
  
  /**
   * Returns the security implementation class.
   */
  Class<? extends S> getSecurityClass();
  
  /**
   * Returns the security type name.
   */
  String getSecurityType();
  
  /**
   * Create a bean representation of the security. Does not need to set the base properties
   * of SecurityBean.
   */
  SBean createBean(HibernateSecurityMasterDao secMasterSession, S security);
  
  /**
   * Convert a bean representation to a security.
   */
  S createSecurity(SBean bean);
  
  /**
   * Test a bean and security representation for equality.
   */
  boolean beanEquals(SBean bean, S security);
  
  /**
   * Loads additional (deep) data for a security bean. For example to implement date constrained relationships
   * that Hibernate alone can't deal with. May update the supplied bean, and return it, or return a new bean.
   */
  SBean resolve(HibernateSecurityMasterDao secMasterSession, Date now, SBean bean);
  
  /**
   * Additional persistence required after the main bean has been passed to Hibernate. Used with resolve to
   * store data Hibernate alone can't deal with.
   */
  void postPersistBean(HibernateSecurityMasterDao secMasterSession, Date effectiveDate, SBean bean); 
  
}