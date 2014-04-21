/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.analytics.financial.instrument.payment.PaymentFixedDefinition;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.cashflow.CashFlowSecurity;

/**
 * Converts {@link CashFlowSecurity} to the equivalent OG-Analytics object ({@link PaymentFixedDefinition}).
 */
public class CashFlowSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  @Override
  public PaymentFixedDefinition visitCashFlowSecurity(final CashFlowSecurity security) {
    return new PaymentFixedDefinition(security.getCurrency(), security.getSettlement(), security.getAmount());
  }
}
