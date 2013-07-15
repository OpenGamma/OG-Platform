/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
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

/**
 * Calculates the present value of equity options using the Black method.
 */
public final class EquityOptionBlackPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** A static instance */
  private static final EquityOptionBlackPresentValueCalculator INSTANCE = new EquityOptionBlackPresentValueCalculator();

  /**
   * Gets the static instance
   * @return The static instance
   */
  public static EquityOptionBlackPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  private EquityOptionBlackPresentValueCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    return EquityIndexOptionBlackMethod.getInstance().presentValue(option, data);
  }

  @Override
  public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    return EquityOptionBlackMethod.getInstance().presentValue(option, data);
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    return EquityIndexFutureOptionBlackMethod.getInstance().presentValue(option, data);
  }

}
