/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.security.FinancialSecurityVisitorAdapter;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.fx.NonDeliverableFXForwardSecurity;
import com.opengamma.financial.security.option.FXBarrierOptionSecurity;
import com.opengamma.financial.security.option.FXDigitalOptionSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXDigitalOptionSecurity;
import com.opengamma.financial.security.option.NonDeliverableFXOptionSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 *
 */
public class BloombergFXSpotRateIdentifierVisitor extends FinancialSecurityVisitorAdapter<ExternalId> {
  private static final String POSTFIX = " Curncy";
  private final CurrencyPairs _currencyPairs;

  public BloombergFXSpotRateIdentifierVisitor(final CurrencyPairs currencyPairs) {
    ArgumentChecker.notNull(currencyPairs, "currency pairs");
    _currencyPairs = currencyPairs;
  }

  @Override
  public ExternalId visitFXForwardSecurity(final FXForwardSecurity security) {
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    return getBloombergId(payCurrency, receiveCurrency);
  }

  @Override
  public ExternalId visitNonDeliverableFXForwardSecurity(final NonDeliverableFXForwardSecurity security) {
    final Currency payCurrency = security.getPayCurrency();
    final Currency receiveCurrency = security.getReceiveCurrency();
    return getBloombergId(payCurrency, receiveCurrency);
  }

  @Override
  public ExternalId visitFXOptionSecurity(final FXOptionSecurity security) {
    final Currency putCurrency = security.getPutCurrency();
    final Currency callCurrency = security.getCallCurrency();
    return getBloombergId(putCurrency, callCurrency);
  }

  @Override
  public ExternalId visitNonDeliverableFXOptionSecurity(final NonDeliverableFXOptionSecurity security) {
    final Currency putCurrency = security.getPutCurrency();
    final Currency callCurrency = security.getCallCurrency();
    return getBloombergId(putCurrency, callCurrency);
  }

  @Override
  public ExternalId visitFXDigitalOptionSecurity(final FXDigitalOptionSecurity security) {
    final Currency putCurrency = security.getPutCurrency();
    final Currency callCurrency = security.getCallCurrency();
    return getBloombergId(putCurrency, callCurrency);
  }

  @Override
  public ExternalId visitNonDeliverableFXDigitalOptionSecurity(final NonDeliverableFXDigitalOptionSecurity security) {
    final Currency putCurrency = security.getPutCurrency();
    final Currency callCurrency = security.getCallCurrency();
    return getBloombergId(putCurrency, callCurrency);
  }

  @Override
  public ExternalId visitFXBarrierOptionSecurity(final FXBarrierOptionSecurity security) {
    final Currency putCurrency = security.getPutCurrency();
    final Currency callCurrency = security.getCallCurrency();
    return getBloombergId(putCurrency, callCurrency);
  }

  private ExternalId getBloombergId(final Currency currency1, final Currency currency2) {
    final CurrencyPair currencyPair = _currencyPairs.getCurrencyPair(currency1, currency2);
    if (currencyPair.getBase().equals(currency1)) {
      return ExternalSchemes.bloombergTickerSecurityId(currency1.getCode() + currency2.getCode() + POSTFIX);
    }
    return ExternalSchemes.bloombergTickerSecurityId(currency2.getCode() + currency1.getCode() + POSTFIX);
  }
}
