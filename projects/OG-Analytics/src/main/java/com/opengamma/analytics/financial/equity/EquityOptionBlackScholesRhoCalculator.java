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
public final class EquityOptionBlackScholesRhoCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** Static instance */
  private static final EquityOptionBlackScholesRhoCalculator s_instance = new EquityOptionBlackScholesRhoCalculator();

  /**
   * Gets the (singleton) instance of this calculator
   * @return The instance of this calculator
   */
  public static EquityOptionBlackScholesRhoCalculator getInstance() {
    return s_instance;
  }

  private EquityOptionBlackScholesRhoCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");

    final Double rhoBlackScholes = EquityIndexOptionBlackMethod.getInstance().rhoBlackScholes(option, data);
    return rhoBlackScholes;
  }

  @Override
  public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final Double rhoBlackScholes = EquityOptionBlackMethod.getInstance().rhoBlackScholes(option, data);
    return rhoBlackScholes;
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final Double rhoBlackScholes = EquityIndexFutureOptionBlackMethod.getInstance().rhoBlackScholes(option, data);
    return rhoBlackScholes;
  }
}
