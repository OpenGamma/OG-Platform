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
  public T visitBondSecurityBean(BondSecurityBean security);
  public T visitEquitySecurityBean(EquitySecurityBean security);
  public T visitEquityOptionSecurityBean(EquityOptionSecurityBean security);
  public T visitFutureSecurityBean(FutureSecurityBean security);
}
