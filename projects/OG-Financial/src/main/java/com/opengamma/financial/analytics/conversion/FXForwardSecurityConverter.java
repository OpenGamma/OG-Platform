/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link FXForwardSecurity} to {@link ForexDefinition}
 */
public class FXForwardSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  @Override
  public InstrumentDefinition<?> visitFXForwardSecurity(final FXForwardSecurity fxForwardSecurity) {
    ArgumentChecker.notNull(fxForwardSecurity, "fx forward security");
    final Currency payCurrency = fxForwardSecurity.getPayCurrency();
    final Currency receiveCurrency = fxForwardSecurity.getReceiveCurrency();
    final double payAmount = fxForwardSecurity.getPayAmount();
    final double receiveAmount = fxForwardSecurity.getReceiveAmount();
    final ZonedDateTime forwardDate = fxForwardSecurity.getForwardDate();
    return ForexDefinition.fromAmounts(payCurrency, receiveCurrency, forwardDate, -payAmount, receiveAmount);
  }
}
