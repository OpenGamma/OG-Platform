/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InterestRateCurveSensitivity;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.analytics.financial.interestrate.future.derivative.FederalFundsFutureTransaction;
import com.opengamma.analytics.financial.interestrate.method.PricingMethod;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Methods for the pricing of Federal Funds futures generic to all models.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public abstract class FederalFundsFutureTransactionMethod implements PricingMethod {

  /**
   * The security method.
   */
  private FederalFundsFutureSecurityMethod _methodSecurity;

  /**
   * Gets the security method.
   * @return The method.
   */
  public FederalFundsFutureSecurityMethod getMethodSecurity() {
    return _methodSecurity;
  }

  /**
   * Sets the security method.
   * @param methodSecurity The method.
   */
  public void setMethodSecurity(final FederalFundsFutureSecurityMethod methodSecurity) {
    this._methodSecurity = methodSecurity;
  }

  /**
   * Compute the present value of a future transaction from a quoted price.
   * @param future The future.
   * @param price The quoted price.
   * @return The present value.
   */
  public CurrencyAmount presentValueFromPrice(final FederalFundsFutureTransaction future, final double price) {
    final double pv = (price - future.getReferencePrice()) * future.getUnderlyingSecurity().getPaymentAccrualFactor() * future.getUnderlyingSecurity().getNotional() * future.getQuantity();
    return CurrencyAmount.of(future.getUnderlyingSecurity().getCurrency(), pv);
  }

  /**
   * Compute the present value of a future transaction from the curves.
   * @param future The future.
   * @param curves The yield curves.
   * @return The present value.
   */
  public CurrencyAmount presentValue(final FederalFundsFutureTransaction future, final YieldCurveBundle curves) {
    final double price = getMethodSecurity().price(future.getUnderlyingSecurity(), curves);
    return presentValueFromPrice(future, price);
  }

  @Override
  public CurrencyAmount presentValue(final InstrumentDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof FederalFundsFutureTransaction, "Federal Funds future");
    return presentValue((FederalFundsFutureTransaction) instrument, curves);
  }

  /**
   * Compute the present value sensitivity to rates of a interest rate future by discounting.
   * @param future The future.
   * @param curves The yield curves.
   * @return The present value rate sensitivity.
   */
  public InterestRateCurveSensitivity presentValueCurveSensitivity(final FederalFundsFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    final InterestRateCurveSensitivity priceSensi = _methodSecurity.priceCurveSensitivity(future.getUnderlyingSecurity(), curves);
    final InterestRateCurveSensitivity result = priceSensi.multipliedBy(future.getUnderlyingSecurity().getPaymentAccrualFactor() * future.getUnderlyingSecurity().getNotional() * future.getQuantity());
    return result;
  }

}
