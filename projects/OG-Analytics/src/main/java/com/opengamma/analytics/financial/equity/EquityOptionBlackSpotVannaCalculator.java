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
 * Calculates the spot vanna (sensitivity of price with respect to the spot and volatility) using the Black method.
 */
public final class EquityOptionBlackSpotVannaCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** Static instance */
  private static final EquityOptionBlackSpotVannaCalculator s_instance = new EquityOptionBlackSpotVannaCalculator();

  /**
   * Gets the (singleton) instance of this calculator
   * @return The instance of this calculator
   */
  public static EquityOptionBlackSpotVannaCalculator getInstance() {
    return s_instance;
  }

  private EquityOptionBlackSpotVannaCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    return EquityIndexOptionBlackMethod.getInstance().vannaWrtSpot(option, data);
  }

  @Override
  public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    return EquityOptionBlackMethod.getInstance().vannaWrtSpot(option, data);
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    return EquityIndexFutureOptionBlackMethod.getInstance().vannaWrtSpot(option, data);
  }
}
