/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate.future.method;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.financial.interestrate.YieldCurveBundle;
import com.opengamma.financial.interestrate.future.definition.InterestRateFutureSecurity;
import com.opengamma.financial.model.interestrate.HullWhiteOneFactorPiecewiseConstantInterestRateModel;
import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.definition.HullWhiteOneFactorPiecewiseConstantDataBundle;

/**
 * Method to compute the price for an interest rate future with convexity adjustment from a Hull-White one factor model.
 */
public class InterestRateFutureSecurityHullWhiteMethod {

  /**
   * The model used for the convexity adjustment computation.
   */
  private final HullWhiteOneFactorPiecewiseConstantDataBundle _data;

  /**
   * The Hull-White model.
   */
  private final HullWhiteOneFactorPiecewiseConstantInterestRateModel _model = new HullWhiteOneFactorPiecewiseConstantInterestRateModel();

  /**
   * Constructor from the model details.
   * @param meanReversion The mean reversion speed (a) parameter.
   * @param volatility The volatility parameters. 
   * @param volatilityTime The times separating the constant volatility periods.
   */
  public InterestRateFutureSecurityHullWhiteMethod(final double meanReversion, final double[] volatility, final double[] volatilityTime) {
    Validate.notNull(volatility, "volatility time");
    Validate.notNull(volatilityTime, "volatility time");
    _data = new HullWhiteOneFactorPiecewiseConstantDataBundle(meanReversion, volatility, volatilityTime);
  }

  /**
   * Constructor from the model.
   * @param data The Hull-White one factor model parameters.
   */
  public InterestRateFutureSecurityHullWhiteMethod(final HullWhiteOneFactorPiecewiseConstantDataBundle data) {
    Validate.notNull(data, "data");
    _data = data;
  }

  /**
   * Computes the price of a future from the curves using an estimation of the future rate without convexity adjustment.
   * @param future The future.
   * @param curves The yield curves. Should contain the forward curve associated. 
   * @return The price.
   */
  public double price(final InterestRateFutureSecurity future, final YieldCurveBundle curves) {
    Validate.notNull(future, "Future");
    Validate.notNull(curves, "Curves");
    final YieldAndDiscountCurve forwardCurve = curves.getCurve(future.getForwardCurveName());
    double forward = (forwardCurve.getDiscountFactor(future.getFixingPeriodStartTime()) / forwardCurve.getDiscountFactor(future.getFixingPeriodEndTime()) - 1) / future.getFixingPeriodAccrualFactor();
    double futureConvexityFactor = _model.futureConvexityFactor(future, _data);
    double price = 1.0 - futureConvexityFactor * forward + (1 - futureConvexityFactor) / future.getFixingPeriodAccrualFactor();
    return price;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_data == null) ? 0 : _data.hashCode());
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
    InterestRateFutureSecurityHullWhiteMethod other = (InterestRateFutureSecurityHullWhiteMethod) obj;
    if (!ObjectUtils.equals(_data, other._data)) {
      return false;
    }
    return true;
  }

}
