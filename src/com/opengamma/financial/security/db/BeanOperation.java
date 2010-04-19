/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;

/**
 * Operations to convert a real entity to/from a bean and hence to/from the Hibernate database.
 * 
 * @author Andrew Griffin
 */
/* package */ interface BeanOperation<S extends Security, SBean extends SecurityBean> {
  
  /**
   * Returns the bean implementation class.
   */
  public Class<? extends SBean> getBeanClass ();
  
  /**
   * Returns the security implementation class.
   */
  public Class<? extends S> getSecurityClass ();
  
  /**
   * Returns the security type name.
   */
  public String getSecurityType ();
  
  /**
   * Create a bean representation of the security. Does not need to set the base properties
   * of SecurityBean.
   */
  public SBean createBean (HibernateSecurityMasterSession secMasterSession, S security);
  
  /**
   * Convert a bean representation to a security.
   */
  public S createSecurity (Identifier identifier, SBean bean);
  
  /**
   * Test a bean and security representation for equality.
   */
  public boolean beanEquals (SBean bean, S security);
  
}