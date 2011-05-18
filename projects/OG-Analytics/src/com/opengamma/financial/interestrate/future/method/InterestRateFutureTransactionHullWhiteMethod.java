/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.InterestRateFutureTransaction;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;

/**
 * Method to compute the present value and its sensitivities for an interest rate future with Hull-White model convexity adjustment.
 */
public class InterestRateFutureTransactionHullWhiteMethod extends InterestRateFutureTransactionMethod {

  /**
   * The model used for the convexity adjustment computation.
   */
  private final HullWhiteOneFactorPiecewiseConstantInterestRateModel _model;
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
    _model = new HullWhiteOneFactorPiecewiseConstantInterestRateModel(meanReversion, volatility, volatilityTime);
    _securityMethod = new InterestRateFutureSecurityHullWhiteMethod(_model);
  }

  /**
   * Constructor from the model.
   * @param model The Hull-White one factor model.
   */
  public InterestRateFutureTransactionHullWhiteMethod(final HullWhiteOneFactorPiecewiseConstantInterestRateModel model) {
    Validate.notNull(model, "Model");
    _model = model;
    _securityMethod = new InterestRateFutureSecurityHullWhiteMethod(_model);
  }

  @Override
  public double presentValue(final InterestRateDerivative instrument, final YieldCurveBundle curves) {
    Validate.isTrue(instrument instanceof InterestRateFutureTransaction, "Interest rate future transaction");
    return presentValue((InterestRateFutureTransaction) instrument, curves);
  }

  /**
   * Present value computation using the hull-White model for the convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The present value.
   */
  public double presentValue(final InterestRateFutureTransaction future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    double futurePrice = _securityMethod.price(future.getUnderlyingFuture(), curves);
    double pv = presentValueFromPrice(future, futurePrice);
    return pv;
  }

}
