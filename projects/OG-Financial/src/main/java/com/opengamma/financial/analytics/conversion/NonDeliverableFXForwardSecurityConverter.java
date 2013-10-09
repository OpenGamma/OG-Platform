/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.forex.definition.ForexNonDeliverableForwardDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link NonDeliverableFXForwardSecurity} to {@link ForexNonDeliverableForwardDefinition}
 */
public class NonDeliverableFXForwardSecurityConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {

  @Override
  public InstrumentDefinition<?> visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity fxForwardSecurity) {
    ArgumentChecker.notNull(fxForwardSecurity, "fx forward security");
    final Currency payCurrency = fxForwardSecurity.getPayCurrency();
    final Currency receiveCurrency = fxForwardSecurity.getReceiveCurrency();
    final double payAmount = fxForwardSecurity.getPayAmount();
    final double receiveAmount = fxForwardSecurity.getReceiveAmount();
    final double exchangeRate = receiveAmount / payAmount;
    final ZonedDateTime fixingDate = fxForwardSecurity.getForwardDate();
    final ZonedDateTime paymentDate = fixingDate; //TODO get this right
    return new ForexNonDeliverableForwardDefinition(payCurrency, receiveCurrency, receiveAmount, exchangeRate, fixingDate, paymentDate);
  }
}
