/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.money;

import com.opengamma.util.ArgumentChecker;

/**
 * Pricer that keeps a running sum. It is optimised for the most common case of a series of multi currency amounts in
 * the same currency.
 */
public class MultipleCurrencyAmountPricer {
  // we pull out a single currency value and keep a running sum for it. This saves creating multiple transient
  // MCA object.
  /** running total (less the initial coupon amount) for optimised currency */
  private double _singleCurrencySubsequentAmounts;
  /** the currency we have optimised */
  private Currency _optimisedCurrency;
  /** holds the running sum - excluding subsequent payments in the optimised currency */
  private MultipleCurrencyAmount _currencyAmount;

  // the total amount is _singleCurrencySubsequentAmounts + _currencyAmount

  /**
   * Create a pricing object
   * @param amount the initial amount in the series of payments
   */
  public MultipleCurrencyAmountPricer(MultipleCurrencyAmount amount) {
    ArgumentChecker.notNull(amount, "amount");
    if (amount.size() > 0) {
      // optimise the pricing of this currency by skipping intermediate MCA objects
      CurrencyAmount currencyAmount = amount.iterator().next();
      _singleCurrencySubsequentAmounts = 0.0;
      _optimisedCurrency = currencyAmount.getCurrency();
    }
    _currencyAmount = amount;
  }

  /**
   * Add the amount to the existing sum
   * @param amountToAdd the amount to add
   */
  public void plus(MultipleCurrencyAmount amountToAdd) {
    ArgumentChecker.notNull(amountToAdd, "amountToAdd");
    if (_optimisedCurrency == null) {
      _currencyAmount = _currencyAmount.plus(amountToAdd);
    } else {
      CurrencyAmount optimisedAmount = amountToAdd.getCurrencyAmount(_optimisedCurrency);
      if (optimisedAmount != null && amountToAdd.size() == 1) {
        // we only have the optimised currency so just update the running total
        _singleCurrencySubsequentAmounts += optimisedAmount.getAmount();
        return;
      }
      _currencyAmount = _currencyAmount.plus(amountToAdd);
    }
  }

  /**
   * Get the sum of all the payments
   * @return the sum
   */
  public MultipleCurrencyAmount getSum() {
    if (_optimisedCurrency == null) {
      return _currencyAmount;
    }
    return _currencyAmount.plus(_optimisedCurrency, _singleCurrencySubsequentAmounts);
  }

}
