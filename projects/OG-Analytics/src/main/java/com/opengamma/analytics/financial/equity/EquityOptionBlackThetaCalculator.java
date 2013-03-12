/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOptionBlackMethod;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOptionBlackMethod;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.option.EquityOptionBlackMethod;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the theta (first order sensitivity of price with respect to the time to expiry) using the Black method.
 */
public final class EquityOptionBlackThetaCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** Static instance */
  private static final EquityOptionBlackThetaCalculator s_instance = new EquityOptionBlackThetaCalculator();

  /**
   * Gets the (singleton) instance of this calculator
   * @return The instance of this calculator
   */
  public static EquityOptionBlackThetaCalculator getInstance() {
    return s_instance;
  }

  private EquityOptionBlackThetaCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final Double thetaBlack = EquityIndexOptionBlackMethod.getInstance().spotTheta(option, data);
    return thetaBlack;
  }

  @Override
  public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final Double thetaBlack = EquityOptionBlackMethod.getInstance().spotTheta(option, data);
    return thetaBlack;
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final Double thetaBlack = EquityIndexFutureOptionBlackMethod.getInstance().theta(option, data);
    return thetaBlack;
  }
}
