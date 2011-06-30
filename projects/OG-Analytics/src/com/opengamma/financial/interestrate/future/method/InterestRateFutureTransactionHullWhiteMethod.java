/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureTransaction;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Method to compute the present value and its sensitivities for an interest rate future with Hull-White model convexity adjustment.
 */
public class InterestRateFutureTransactionHullWhiteMethod extends InterestRateFutureTransactionMethod {

  //  /**
  //   * The model used for the convexity adjustment computation.
  //   */
  //  private final HullWhiteOneFactorPiecewiseConstantDataBundle _data;
  /**
   * The method used to compute the future price.
   */
  private final InterestRateFutureSecurityHullWhiteMethod _securityMethod;

  /**
   * Constructor from Hull-White model details.
   * @param meanReversion The mean reversion speed (a) parameter.
   * @param volatility The volatility parameters. 
   * @param volatilityTime The times separating the constant volatility periods.
   */
  public InterestRateFutureTransactionHullWhiteMethod(final double meanReversion, final double[] volatility, final double[] volatilityTime) {
    Validate.notNull(volatility, "volatility time");
    Validate.notNull(volatilityTime, "volatility time");
    HullWhiteOneFactorPiecewiseConstantDataBundle data = new HullWhiteOneFactorPiecewiseConstantDataBundle(meanReversion, volatility, volatilityTime);
    _securityMethod = new InterestRateFutureSecurityHullWhiteMethod(data);
  }

  /**
   * Constructor from the model.
   * @param data The Hull-White one factor model parameters.
   */
  public InterestRateFutureTransactionHullWhiteMethod(final HullWhiteOneFactorPiecewiseConstantDataBundle data) {
    Validate.notNull(data, "Data");
    _securityMethod = new InterestRateFutureSecurityHullWhiteMethod(data);
  }

  /**
   * Present value computation using the hull-White model for the convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The present value.
   */
  public CurrencyAmount presentValue(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    double futurePrice = _securityMethod.price(future.getUnderlyingFuture(), curves);
    double pv = presentValueFromPrice(future, futurePrice);
    return CurrencyAmount.of(future.getUnderlyingFuture().getCurrency(), pv);
  }

  @Override
  public CurrencyAmount presentValue(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof InterestRateFutureTransaction, "Interest rate future transaction");
    return presentValue((InterestRateFutureTransaction) instrument, curves);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _securityMethod.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    InterestRateFutureTransactionHullWhiteMethod other = (InterestRateFutureTransactionHullWhiteMethod) obj;
    if (!ObjectUtils.equals(_securityMethod, other._securityMethod)) {
      return false;
    }
    return true;
  }

}
