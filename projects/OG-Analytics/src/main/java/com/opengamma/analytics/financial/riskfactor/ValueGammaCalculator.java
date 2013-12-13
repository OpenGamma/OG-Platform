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
 * Calculates the value (or dollar) gamma of an option given market data and the gamma. The value gamma is defined as the
 * option gamma multiplied by half of the spot squared and shares per option.
 */
public final class ValueGammaCalculator implements ValueGreekCalculator {
  /** Static instance */
  private static final ValueGammaCalculator s_instance = new ValueGammaCalculator();
  /** Calculates the multiplier for converting delta to value delta */
  private static final MultiplierCalculator s_multiplierCalculator = new MultiplierCalculator();

  /**
   * Gets an instance of this calculator
   * @return The (singleton) instance
   */
  public static ValueGammaCalculator getInstance() {
    return s_instance;
  }

  private ValueGammaCalculator() {
  }

  @Override
  public double valueGreek(final InstrumentDerivative derivative, final StaticReplicationDataBundle market, final double gamma) {
    ArgumentChecker.notNull(derivative, "derivative");
    ArgumentChecker.notNull(market, "market");
    return gamma * derivative.accept(s_multiplierCalculator, market);
  }

  /**
   * Calculates the multiplier for value gamma - spot * spot * shares per option / 2
   */
  private static final class MultiplierCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {

    /* package */MultiplierCalculator() {
    }

    @Override
    public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle market) {
      final double spot = market.getForwardCurve().getSpot();
      return option.getUnitAmount() * spot * spot / 20000.;
    }

    @Override
    public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle market) {
      final double spot = market.getForwardCurve().getSpot();
      return option.getPointValue() * spot * spot / 2;
    }

    @Override
    public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle market) {
      final double spot = market.getForwardCurve().getSpot();
      return option.getUnitAmount() * spot * spot / 20000.;
    }

    @Override
    public Double visitAgricultureFutureOption(final AgricultureFutureOption option, final StaticReplicationDataBundle market) {
      final double spot = market.getForwardCurve().getSpot();
      return option.getUnderlying().getUnitAmount() * spot * spot / 2;
    }

    @Override
    public Double visitEnergyFutureOption(final EnergyFutureOption option, final StaticReplicationDataBundle market) {
      final double spot = market.getForwardCurve().getSpot();
      return option.getUnderlying().getUnitAmount() * spot * spot / 2;
    }

    @Override
    public Double visitMetalFutureOption(final MetalFutureOption option, final StaticReplicationDataBundle market) {
      final double spot = market.getForwardCurve().getSpot();
      return option.getUnderlying().getUnitAmount() * spot * spot / 2;
    }
  }

}
