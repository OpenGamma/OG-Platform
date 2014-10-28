/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.volatility.BlackScholesFormulaRepository;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the Black-Scholes daily theta
 */
public final class EquityOptionBlackScholesThetaCalculator extends
    InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** Static instance */
  private static final EquityOptionBlackScholesThetaCalculator s_instance = new EquityOptionBlackScholesThetaCalculator();

  /**
   * Gets the (singleton) instance of this calculator
   * @return The instance of this calculator
   */
  public static EquityOptionBlackScholesThetaCalculator getInstance() {
    return s_instance;
  }

  private EquityOptionBlackScholesThetaCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(EquityIndexOption option, StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    double t = option.getTimeToExpiry();
    double k = option.getStrike();
    boolean isCall = option.isCall();
    return computeTheta(k, t, isCall, data);
  }

  @Override
  public Double visitEquityOption(EquityOption option, StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    double t = option.getTimeToExpiry();
    double k = option.getStrike();
    boolean isCall = option.isCall();
    return computeTheta(k, t, isCall, data);
  }

  /*
   * For index options, standard theta is driftless theta, i.e., b=r=0.  
   * Thus use {@link EquityOptionBlackThetaCalculator} for this instrument.
   */
  @Override
  public Double visitEquityIndexFutureOption(EquityIndexFutureOption option,
      final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    double t = option.getExpiry();
    double k = option.getStrike();
    boolean isCall = option.isCall();
    return computeTheta(k, t, isCall, data);
  }

  private double computeTheta(double k, double t, boolean isCall, StaticReplicationDataBundle data) {
    double s = data.getForwardCurve().getSpot();
    double fwd = data.getForwardCurve().getForward(t);
    double r = data.getDiscountCurve().getInterestRate(t);
    double b = t > 0 ? Math.log(fwd / s) / t : r;
    double volatility = data.getVolatilitySurface().getVolatility(t, k);
    double theta = BlackScholesFormulaRepository.theta(s, k, t, volatility, r, b, isCall) / 365.0; // daily theta
    return theta;
  }

}
