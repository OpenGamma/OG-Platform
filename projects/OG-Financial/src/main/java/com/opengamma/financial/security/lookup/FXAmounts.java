/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.security.lookup;

import com.opengamma.financial.currency.CurrencyPair;
import com.opengamma.financial.currency.CurrencyPairs;
import com.opengamma.financial.currency.CurrencyUtils;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.money.Currency;

/**
 * The two currency amounts in an FX trade.
 */
public final class FXAmounts {

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
  private FXAmounts(Currency baseCurrency, Currency counterCurrency, double baseAmount, double counterAmount) {
    ArgumentChecker.notNull(baseCurrency, "baseCurrency");
    ArgumentChecker.notNull(counterCurrency, "counterCurrency");
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

  /* package */ static FXAmounts forForward(Currency payCurrency,
                                            Currency receiveCurrency,
                                            double payAmount,
                                            double receiveAmount,
                                            CurrencyPairs currencyPairs) {
    return forAmounts(payCurrency, receiveCurrency, payAmount, receiveAmount, currencyPairs);
  }

  /* package */ static FXAmounts forOption(Currency putCurrency,
                                           Currency callCurrency,
                                           double putAmount,
                                           double callAmount,
                                           boolean isLong,
                                           CurrencyPairs currencyPairs) {
    if (isLong) {
      return forAmounts(putCurrency, callCurrency, putAmount, callAmount, currencyPairs);
    } else {
      return forAmounts(callCurrency, putCurrency, callAmount, putAmount, currencyPairs);
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    FXAmounts fxAmounts = (FXAmounts) o;

    if (Double.compare(fxAmounts._baseAmount, _baseAmount) != 0) {
      return false;
    }
    if (Double.compare(fxAmounts._counterAmount, _counterAmount) != 0) {
      return false;
    }
    if (!_baseCurrency.equals(fxAmounts._baseCurrency)) {
      return false;
    }
    if (!_counterCurrency.equals(fxAmounts._counterCurrency)) {
      return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int result;
    long temp;
    result = _baseCurrency.hashCode();
    result = 31 * result + _counterCurrency.hashCode();
    temp = _counterAmount != +0.0d ? Double.doubleToLongBits(_counterAmount) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    temp = _baseAmount != +0.0d ? Double.doubleToLongBits(_baseAmount) : 0L;
    result = 31 * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public String toString() {
    return "FXAmounts [" +
        "_baseCurrency=" + _baseCurrency +
        ", _counterCurrency=" + _counterCurrency +
        ", _counterAmount=" + _counterAmount +
        ", _baseAmount=" + _baseAmount +
        "]";
  }
}
