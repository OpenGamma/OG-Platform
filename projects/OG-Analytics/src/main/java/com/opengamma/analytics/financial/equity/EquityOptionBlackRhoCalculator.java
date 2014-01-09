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
 * Calculates the rho (first order sensitivity of price with respect to the interest rate) using the Black method.
 */
public final class EquityOptionBlackRhoCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** Static instance */
  private static final EquityOptionBlackRhoCalculator s_instance = new EquityOptionBlackRhoCalculator();

  /**
   * Gets the (singleton) instance of this calculator
   * @return The instance of this calculator
   */
  public static EquityOptionBlackRhoCalculator getInstance() {
    return s_instance;
  }

  private EquityOptionBlackRhoCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final Double rhoBlack = EquityIndexOptionBlackMethod.getInstance().rhoBlack(option, data);
    return rhoBlack;
  }

  @Override
  public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final Double rhoBlack = EquityOptionBlackMethod.getInstance().rhoBlack(option, data);
    return rhoBlack;
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final Double rhoBlack = EquityIndexFutureOptionBlackMethod.getInstance().rhoBlack(option, data);
    return rhoBlack;
  }

}
