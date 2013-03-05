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
 * Calculates the value (or dollar) delta of an option given market data and the delta. The value delta is defined as the
 * option delta multiplied by the spot and shares per option.
 */
public final class ValueDeltaCalculator implements ValueGreekCalculator {
  /** Static instance */
  private static final ValueDeltaCalculator s_instance = new ValueDeltaCalculator();
  /** Calculates the multiplier for converting delta to value delta */
  private static final MultiplierCalculator s_multiplierCalculator = new MultiplierCalculator();

  /**
   * Gets an instance of this calculator
   * @return The (singleton) instance
   */
  public static ValueDeltaCalculator getInstance() {
    return s_instance;
  }

  private ValueDeltaCalculator() {
  }

  @Override
  public double valueGreek(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final double delta) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(market, "market");
    return delta * derivative.accept(s_multiplierCalculator, market);
  }

  /**
   * Calculates the multiplier for value delta - spot * shares per option
   */
  private static final class MultiplierCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {

    /* package */MultiplierCalculator() {
    }

    @Override
    public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle market) {
      return option.getUnitAmount() * market.getForwardCurve().getSpot();
    }

    @Override
    public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle market) {
      return option.getUnitAmount() * market.getForwardCurve().getSpot();
    }

    @Override
    public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle market) {
      return option.getPointValue() * market.getForwardCurve().getSpot();
    }

    @Override
    public Double visitAgricultureFutureOption(final AgricultureFutureOption option, final StaticReplicationDataBundle market) {
      return option.getUnderlying().getUnitAmount() * market.getForwardCurve().getSpot();
    }

    @Override
    public Double visitEnergyFutureOption(final EnergyFutureOption option, final StaticReplicationDataBundle market) {
      return option.getUnderlying().getUnitAmount() * market.getForwardCurve().getSpot();
    }

    @Override
    public Double visitMetalFutureOption(final MetalFutureOption option, final StaticReplicationDataBundle market) {
      return option.getUnderlying().getUnitAmount() * market.getForwardCurve().getSpot();
    }
  }

}
