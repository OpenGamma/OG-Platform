/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.future;

import com.opengamma.analytics.financial.commodity.derivative.AgricultureFuture;
import com.opengamma.analytics.financial.commodity.derivative.EnergyFuture;
import com.opengamma.analytics.financial.commodity.derivative.MetalFuture;
import com.opengamma.analytics.financial.equity.future.derivative.CashSettledFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexDividendFuture;
import com.opengamma.analytics.financial.equity.future.derivative.EquityIndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.IndexFuture;
import com.opengamma.analytics.financial.equity.future.derivative.VolatilityIndexFuture;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.future.derivative.BondFuture;
import com.opengamma.analytics.financial.interestrate.future.derivative.InterestRateFutureTransaction;
import com.opengamma.analytics.financial.simpleinstruments.pricing.SimpleFutureDataBundle;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public abstract class MarkToMarketFuturesCalculator extends InstrumentDerivativeVisitorAdapter<SimpleFutureDataBundle, Double> {

  /* package */MarkToMarketFuturesCalculator() {
  }

  @Override
  public Double visitAgricultureFuture(final AgricultureFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getReferencePrice(), future.getUnitAmount(), future.getExpiry()));
  }

  @Override
  public Double visitEnergyFuture(final EnergyFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getReferencePrice(), future.getUnitAmount(), future.getExpiry()));
  }

  @Override
  public Double visitEquityFuture(final EquityFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getStrike(), future.getUnitAmount(), future.getTimeToSettlement()));
  }

  @Override
  public Double visitEquityIndexDividendFuture(final EquityIndexDividendFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getStrike(), future.getUnitAmount(), future.getTimeToSettlement()));
  }

  @Override
  public Double visitMetalFuture(final MetalFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getReferencePrice(), future.getUnitAmount(), future.getExpiry()));
  }

  @Override
  public Double visitInterestRateFutureTransaction(final InterestRateFutureTransaction future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getReferencePrice(), future.getUnderlyingSecurity().getNotional() * future.getUnderlyingSecurity().getPaymentAccrualFactor() * future.getQuantity(),
        future.getUnderlyingSecurity().getTradingLastTime()));
  }

  @Override
  public Double visitBondFuture(final BondFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getReferencePrice(), future.getNotional(), future.getTradingLastTime()));
  }

  @Override
  public Double visitCashSettledFuture(final CashSettledFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getStrike(), future.getUnitAmount(), future.getTimeToSettlement()));
  }

  @Override
  public Double visitIndexFuture(final IndexFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getStrike(), future.getUnitAmount(), future.getTimeToSettlement()));
  }

  @Override
  public Double visitEquityIndexFuture(final EquityIndexFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getStrike(), future.getUnitAmount(), future.getTimeToSettlement()));
  }

  @Override
  public Double visitVolatilityIndexFuture(final VolatilityIndexFuture future, final SimpleFutureDataBundle dataBundle) {
    ArgumentChecker.notNull(future, "future");
    ArgumentChecker.notNull(dataBundle, "data bundle");
    return Double.valueOf(getResult(dataBundle, future.getStrike(), future.getUnitAmount(), future.getTimeToSettlement()));
  }

  abstract double getResult(SimpleFutureDataBundle dataBundle, double strike, double unitAmount, double t);

  /**
   * Calculates the present value
   */
  public static final class PresentValueCalculator extends MarkToMarketFuturesCalculator {
    private static final PresentValueCalculator INSTANCE = new PresentValueCalculator();

    public static PresentValueCalculator getInstance() {
      return INSTANCE;
    }

    private PresentValueCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return (dataBundle.getMarketPrice().doubleValue() - strike) * unitAmount;
    }

  }

  /**
   * Calculates the spot delta. The 'Delta', dV/dS for a linear future is simply the unitAmount.
   */
  public static final class SpotDeltaCalculator extends MarkToMarketFuturesCalculator {
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
   * Calculates the value delta of a Future.<p>
   * ValueDelta is defined as S(t) * dV/dS, hence it should be equal to unitAmount * market_price. <p>
   * ValueDelta represents the cash value of the position or, the value of money one would make if the underlying increased in price by 100%.<p>
   * Observe: PNL = dV/dS * (change in S) = S(t) * dV/dS * (S(T) - S(t)) / S(t),
   * thus S(t)* dV/dS (ValueDelta) would be the PNL if 1.0 = (S(T) - S(t)) / S(t) => S(T) = 2*S(t),
   * i.e. if the underlying doubled (increased by 100%). It thus gives a measure of the sensitivity as a relative measure.
   */
  public static final class ValueDeltaCalculator extends MarkToMarketFuturesCalculator {
    private static final ValueDeltaCalculator INSTANCE = new ValueDeltaCalculator();

    public static ValueDeltaCalculator getInstance() {
      return INSTANCE;
    }

    private ValueDeltaCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return unitAmount * dataBundle.getMarketPrice().doubleValue();
    }
  }

  /**
   * Calculates the rates delta
   */
  public static final class RatesDeltaCalculator extends MarkToMarketFuturesCalculator {
    private static final RatesDeltaCalculator INSTANCE = new RatesDeltaCalculator();

    public static RatesDeltaCalculator getInstance() {
      return INSTANCE;
    }

    private RatesDeltaCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return t * dataBundle.getMarketPrice().doubleValue() * unitAmount;
    }
  }

  /**
   * Calculates the pv01
   */
  public static final class PV01Calculator extends MarkToMarketFuturesCalculator {
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
  public static final class SpotPriceCalculator extends MarkToMarketFuturesCalculator {
    private static final SpotPriceCalculator INSTANCE = new SpotPriceCalculator();

    public static SpotPriceCalculator getInstance() {
      return INSTANCE;
    }

    private SpotPriceCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      if (dataBundle.getSpotValue() == null) {
        throw new UnsupportedOperationException("Spot value for future underlying was null");
      }
      return dataBundle.getSpotValue().doubleValue();
    }

  }

  /**
   * Gets the forward price
   */
  public static final class ForwardPriceCalculator extends MarkToMarketFuturesCalculator {
    private static final ForwardPriceCalculator INSTANCE = new ForwardPriceCalculator();

    public static ForwardPriceCalculator getInstance() {
      return INSTANCE;
    }

    private ForwardPriceCalculator() {
    }

    @Override
    double getResult(final SimpleFutureDataBundle dataBundle, final double strike, final double unitAmount, final double t) {
      return dataBundle.getMarketPrice().doubleValue();
    }

  }
}
