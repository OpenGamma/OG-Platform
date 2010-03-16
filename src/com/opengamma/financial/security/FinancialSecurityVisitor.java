/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

/**
 * General visitor for securities.  This is specifically divorced from the Security definition in the engine.
 * Note how we've just got the concrete classes here.
 * @author jim
 */
public interface FinancialSecurityVisitor<T> extends FutureSecurityVisitor<T>,BondSecurityVisitor<T>,EquityOptionSecurityVisitor<T> {
  public T visitEquitySecurity(EquitySecurity security);
}
