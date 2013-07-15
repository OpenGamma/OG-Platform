/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.future.pricing;

import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.util.ArgumentChecker;


/**
 * 
 */
public abstract class DividendYieldFuturesCalculator extends InstrumentDerivativeVisitorAdapter<SimpleFutureDataBundle, Double> {

  /* package */DividendYieldFuturesCalculator() {
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return getResult(dataBundle, future.getStrike(), future.getUnitAmount(), future.getTimeToSettlement());
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return getResult(dataBundle, future.getStrike(), future.getUnitAmount(), future.getTimeToSettlement());
  }

  abstract double getResult(SimpleFutureDataBundle dataBundle, double strike, double unitAmount, double t);

  /**
   * Calculates the present value
   */
  public static final class PresentValueCalculator extends DividendYieldFuturesCalculator {
    private static final PresentValueCalculator INSTANCE = new PresentValueCalculator();

    public static PresentValueCalculator getInstance() {
      return INSTANCE;
    }

    private PresentValueCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return (dataBundle.getMarketPrice() - strike) * unitAmount;
    }

  }

  /**
   * Calculates the spot delta
   */
  public static final class SpotDeltaCalculator extends DividendYieldFuturesCalculator {
    private static final SpotDeltaCalculator INSTANCE = new SpotDeltaCalculator();

    public static SpotDeltaCalculator getInstance() {
      return INSTANCE;
    }

    private SpotDeltaCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return unitAmount;
    }
  }

  /**
   * Calculates the rates delta
   */
  public static final class RatesDeltaCalculator extends DividendYieldFuturesCalculator {
    private static final RatesDeltaCalculator INSTANCE = new RatesDeltaCalculator();

    public static RatesDeltaCalculator getInstance() {
      return INSTANCE;
    }

    private RatesDeltaCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return t * dataBundle.getMarketPrice() * unitAmount;
    }
  }

  /**
   * Calculates the pv01
   */
  public static final class PV01Calculator extends DividendYieldFuturesCalculator {
    private static final PV01Calculator INSTANCE = new PV01Calculator();

    public static PV01Calculator getInstance() {
      return INSTANCE;
    }

    private PV01Calculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return RatesDeltaCalculator.getInstance().getResult(dataBundle, strike, unitAmount, t) / 10000;
    }
  }

  /**
   * Gets the spot price
   */
  public static final class SpotPriceCalculator extends DividendYieldFuturesCalculator {
    private static final SpotPriceCalculator INSTANCE = new SpotPriceCalculator();

    public static SpotPriceCalculator getInstance() {
      return INSTANCE;
    }

    private SpotPriceCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return dataBundle.getSpotValue();
    }

  }

  /**
   * Gets the forward price
   */
  public static final class ForwardPriceCalculator extends DividendYieldFuturesCalculator {
    private static final ForwardPriceCalculator INSTANCE = new ForwardPriceCalculator();

    public static ForwardPriceCalculator getInstance() {
      return INSTANCE;
    }

    private ForwardPriceCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return dataBundle.getMarketPrice();
    }

  }
}
