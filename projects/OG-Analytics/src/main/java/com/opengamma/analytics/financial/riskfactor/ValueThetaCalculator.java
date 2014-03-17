/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.riskfactor;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFutureOption;
import com.opengamma.analytics.financial.commodity.derivative.MetalFutureOption;
import com.opengamma.analytics.financial.equity.StaticReplicationDataBundle;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the value (or dollar) theta of an option given market data and the theta. The value theta is defined as the
 * option theta multiplied by the shares per option.
 */
public final class ValueThetaCalculator implements ValueGreekCalculator {
  /** Static instance */
  private static final ValueThetaCalculator s_instance = new ValueThetaCalculator();
  /** Calculates the multiplier for converting theta to value theta */
  private static final MultiplierCalculator s_multiplierCalculator = new MultiplierCalculator();

  /**
   * Gets an instance of this calculator
   * @return The (singleton) instance
   */
  public static ValueThetaCalculator getInstance() {
    return s_instance;
  }

  /**
   * Private constructor
   */
  private ValueThetaCalculator() {
  }

  @Override
  public double valueGreek(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final double theta) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(market, "market");
    return theta * derivative.accept(s_multiplierCalculator, market);
  }

  /**
   * Calculates the multiplier for value theta - theta * shares per option
   */
  private static final class MultiplierCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {

    /**
     * Default constructor.
     */
    /* package */MultiplierCalculator() {
    }

    @Override
    public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle market) {
      return option.getUnitAmount();
    }

    @Override
    public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle market) {
      return option.getUnitAmount();
    }

    @Override
    public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle market) {
      return option.getPointValue();
    }

    @Override
    public Double visitAgricultureFutureOption(final AgricultureFutureOption option, final StaticReplicationDataBundle market) {
      return option.getUnderlying().getUnitAmount();
    }

    @Override
    public Double visitEnergyFutureOption(final EnergyFutureOption option, final StaticReplicationDataBundle market) {
      return option.getUnderlying().getUnitAmount();
    }

    @Override
    public Double visitMetalFutureOption(final MetalFutureOption option, final StaticReplicationDataBundle market) {
      return option.getUnderlying().getUnitAmount();
    }
  }

}
