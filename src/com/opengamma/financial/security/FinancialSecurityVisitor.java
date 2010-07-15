/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security;

import com.opengamma.financial.security.bond.BondSecurityVisitor;
import com.opengamma.financial.security.equity.EquitySecurity;
import com.opengamma.financial.security.future.FutureSecurityVisitor;
import com.opengamma.financial.security.option.OptionSecurityVisitor;

/**
 * General visitor for securities.  This is specifically divorced from the Security definition in the engine.
 * Note how we've just got the concrete classes here.
 * 
 * @param <T> Return type for visitor.
 */
public interface FinancialSecurityVisitor<T> extends FutureSecurityVisitor<T>, BondSecurityVisitor<T>, OptionSecurityVisitor<T> {
  T visitEquitySecurity(EquitySecurity security);
}
