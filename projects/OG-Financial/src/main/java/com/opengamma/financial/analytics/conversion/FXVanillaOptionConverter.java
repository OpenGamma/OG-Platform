/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.conversion;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.definition.ForexDefinition;
import com.opengamma.analytics.financial.forex.definition.ForexOptionVanillaDefinition;
import com.opengamma.analytics.financial.instrument.InstrumentDefinition;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * Converts {@link FXOptionSecurity} to {@link ForexOptionVanillaDefinition}
 */
public class FXVanillaOptionConverter extends FinancialSecurityVisitorAdapter<InstrumentDefinition<?>> {
  /** The currency pairs */
  private final CurrencyPairs _currencyPairs;

  /**
   * @param currencyPairs The currency pairs, not null
   */
  public FXVanillaOptionConverter(final CurrencyPairs currencyPairs) {
    ArgumentChecker.notNull(currencyPairs, "currency pairs");
    _currencyPairs = currencyPairs;
  }

  @Override
  public InstrumentDefinition<?> visitFXOptionSecurity(final FXOptionSecurity fxOptionSecurity) {
    ArgumentChecker.notNull(fxOptionSecurity, "fx option security");
    final Currency putCurrency = fxOptionSecurity.getPutCurrency();
    final Currency callCurrency = fxOptionSecurity.getCallCurrency();
    final double putAmount = fxOptionSecurity.getPutAmount();
    final double callAmount = fxOptionSecurity.getCallAmount();
    final ZonedDateTime expiry = fxOptionSecurity.getExpiry().getExpiry();
    final ZonedDateTime settlementDate = fxOptionSecurity.getSettlementDate();
    final boolean isLong = fxOptionSecurity.isLong();
    ForexDefinition underlying;
    final CurrencyPair baseQuotePair = _currencyPairs.getCurrencyPair(putCurrency, callCurrency);
    if (baseQuotePair == null) {
      throw new OpenGammaRuntimeException("Could not get base/quote order for currency pair (" + putCurrency + ", " + callCurrency + ")");
    }
    if (baseQuotePair.getBase().equals(putCurrency)) {
      underlying = ForexDefinition.fromAmounts(putCurrency, callCurrency, settlementDate, putAmount, -callAmount);
      return new ForexOptionVanillaDefinition(underlying, expiry, false, isLong);
    }
    underlying = ForexDefinition.fromAmounts(callCurrency, putCurrency, settlementDate, callAmount, -putAmount);
    return new ForexOptionVanillaDefinition(underlying, expiry, true, isLong);
  }

}
