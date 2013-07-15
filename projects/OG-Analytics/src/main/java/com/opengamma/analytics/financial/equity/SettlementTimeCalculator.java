/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.variance.EquityVarianceSwap;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.varianceswap.VarianceSwap;
import com.opengamma.util.ArgumentChecker;

/**
 * Gets the time (in years) to settlement of an instrument
 */
public final class SettlementTimeCalculator extends InstrumentDerivativeVisitorAdapter<Object, Double> {
  /** The static instance */
  private static final SettlementTimeCalculator INSTANCE = new SettlementTimeCalculator();

  /**
   * Gets the static instance
   * @return A static instance
   */
  public static SettlementTimeCalculator getInstance() {
    return INSTANCE;
  }

  private SettlementTimeCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option) {
    ArgumentChecker.notNull(option, "option");
    return option.getTimeToSettlement();
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final Object data) {
    ArgumentChecker.notNull(option, "option");
    return option.getTimeToSettlement();
  }

  @Override
  public Double visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap) {
    ArgumentChecker.notNull(varianceSwap, "variance swap");
    return varianceSwap.getTimeToSettlement();
  }

  @Override
  public Double visitEquityVarianceSwap(final EquityVarianceSwap varianceSwap, final Object data) {
    ArgumentChecker.notNull(varianceSwap, "variance swap");
    return varianceSwap.getTimeToSettlement();
  }

  @Override
  public Double visitVarianceSwap(final VarianceSwap varianceSwap) {
    ArgumentChecker.notNull(varianceSwap, "variance swap");
    return varianceSwap.getTimeToSettlement();
  }

  @Override
  public Double visitVarianceSwap(final VarianceSwap varianceSwap, final Object data) {
    ArgumentChecker.notNull(varianceSwap, "variance swap");
    return varianceSwap.getTimeToSettlement();
  }
}
