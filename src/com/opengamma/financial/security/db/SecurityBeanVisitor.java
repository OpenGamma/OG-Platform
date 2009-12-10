/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.db;

/**
 * 
 *
 * @author jim
 */
public interface SecurityBeanVisitor<T> {
  public T visitEquitySecurityBean(EquitySecurityBean security);
}
