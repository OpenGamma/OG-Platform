/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics.blotter;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyUtils;
import com.opengamma.financial.security.fx.FXForwardSecurity;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;

/**
 * The two currency amounts in an FX trade.
 */
public class FXAmounts {

  /** The base currency. */
  private final Currency _baseCurrency;
  /** The counter currency. */
  private final Currency _counterCurrency;
  /** The amount in the base currency. */
  private final double _counterAmount;
  /** The amount in the counter currency. */
  private final double _baseAmount;

  /**
   * @param baseCurrency The base currency
   * @param counterCurrency The counter currency
   * @param baseAmount The amount in the base currency
   * @param counterAmount The amount in the counter currency
   */
  /* package */ FXAmounts(Currency baseCurrency, Currency counterCurrency, double baseAmount, double counterAmount) {
    _baseCurrency = baseCurrency;
    _counterCurrency = counterCurrency;
    _baseAmount = baseAmount;
    _counterAmount = counterAmount;
  }

  /**
   * @return The base currency
   */
  public Currency getBaseCurrency() {
    return _baseCurrency;
  }

  /**
   * @return The counter currency
   */
  public Currency getCounterCurrency() {
    return _counterCurrency;
  }

  /**
   * @return The amount in the base currency.
   */
  public double getBaseAmount() {
    return _baseAmount;
  }

  /**
   * @return The amount in the counter currency.
   */
  public double getCounterAmount() {
    return _counterAmount;
  }

  /* package */ static FXAmounts forForward(FXForwardSecurity security, CurrencyPairs currencyPairs) {
    return forAmounts(security.getPayCurrency(),
                      security.getReceiveCurrency(),
                      security.getPayAmount(),
                      security.getReceiveAmount(),
                      currencyPairs);
  }

  /* package */ static FXAmounts forOption(FXOptionSecurity security, CurrencyPairs currencyPairs) {
    if (security.isLong()) {
      return forAmounts(security.getPutCurrency(),
                        security.getCallCurrency(),
                        security.getPutAmount(),
                        security.getCallAmount(),
                        currencyPairs);
    } else {
      return forAmounts(security.getCallCurrency(),
                        security.getPutCurrency(),
                        security.getCallAmount(),
                        security.getPutAmount(),
                        currencyPairs);
    }
  }

  private static FXAmounts forAmounts(Currency payCurrency,
                                      Currency receiveCurrency,
                                      double payAmount,
                                      double receiveAmount,
                                      CurrencyPairs currencyPairs) {
    Double baseAmount = CurrencyUtils.getBaseAmount(payCurrency, receiveCurrency, payAmount, receiveAmount, currencyPairs);
    Double counterAmount = CurrencyUtils.getCounterAmount(payCurrency, receiveCurrency, payAmount, receiveAmount, currencyPairs);
    CurrencyPair currencyPair = currencyPairs.getCurrencyPair(payCurrency, receiveCurrency);
    if (currencyPair == null || baseAmount == null || counterAmount == null) {
      return null;
    }
    if (currencyPair.getBase().equals(payCurrency)) {
      baseAmount = baseAmount * -1;
    } else {
      counterAmount = counterAmount * -1;
    }
    return new FXAmounts(currencyPair.getBase(), currencyPair.getCounter(), baseAmount, counterAmount);
  }
}
