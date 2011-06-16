/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import org.apache.commons.lang.Validate;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.core.security.Security;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;

/**
 * 
 */
public class TradeVisitorAdapter<T, U> {
  private final FinancialSecurityVisitor<U> _securityVisitor;

  public TradeVisitorAdapter(final FinancialSecurityVisitor<U> securityVisitor) {
    Validate.notNull(securityVisitor, "security visitor");
    _securityVisitor = securityVisitor;
  }

  public T visit(final TradeImpl trade) {
    final Security security = trade.getSecurity();
    if (!(security instanceof FinancialSecurity)) {
      throw new OpenGammaRuntimeException("Security underlying the trade was not a financial security");
    }
    final FinancialSecurity financialSecurity = (FinancialSecurity) security;
    final U securityResult = financialSecurity.accept(_securityVisitor);
    return null;
  }
}
