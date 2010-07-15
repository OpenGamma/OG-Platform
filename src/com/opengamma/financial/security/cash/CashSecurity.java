/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.cash;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityVisitor;

/**
 * 
 */
public class CashSecurity extends FinancialSecurity {
  private static final String SECURITY_TYPE = "Cash";

  public CashSecurity(final ZonedDateTime endDate) {
    super(SECURITY_TYPE);
  }

  public <T> T accept(CashSecurityVisitor<T> visitor) {
    return visitor.visitCashSecurity(this);
  }

  @Override
  public final <T> T accept(final FinancialSecurityVisitor<T> visitor) {
    return visitor.visitCashSecurity(this);
  }

}
