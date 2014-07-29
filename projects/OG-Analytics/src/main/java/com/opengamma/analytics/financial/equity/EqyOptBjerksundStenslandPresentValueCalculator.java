/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the present value of equity options using the Black method.
 */
public final class EqyOptBjerksundStenslandPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** A static instance */
  private static final EqyOptBjerksundStenslandPresentValueCalculator INSTANCE = new EqyOptBjerksundStenslandPresentValueCalculator();
  /** The present value calculator */
  private static final BjerksundStenslandModel MODEL = new BjerksundStenslandModel();

  /**
   * Gets the static instance
   * @return The static instance
   */
  public static EqyOptBjerksundStenslandPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  private EqyOptBjerksundStenslandPresentValueCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    ForwardCurve forwardCurve = data.getForwardCurve();
    final double spot = forwardCurve.getSpot();
    final double strike = option.getStrike();
    final double time = option.getTimeToExpiry();
    final double sigma = data.getVolatilitySurface().getVolatility(time, strike);
    final boolean isCall = option.isCall();
    final double interestRate = data.getDiscountCurve().getInterestRate(time);
    final double costOfCarry = time > 0 ? Math.log(forwardCurve.getForward(time) / spot) / time : interestRate;
    return option.getUnitAmount() * MODEL.price(spot, strike, interestRate, costOfCarry, time, sigma, isCall);
  }

  @Override
  public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double spot = data.getForwardCurve().getSpot();
    final double strike = option.getStrike();
    final double time = option.getTimeToExpiry();
    final double sigma = data.getVolatilitySurface().getVolatility(time, strike);
    final boolean isCall = option.isCall();
    final double interestRate = data.getDiscountCurve().getInterestRate(time);
    double costOfCarry = interestRate;
    double modSpot = spot;

    final ForwardCurve fCurve = data.getForwardCurve();
    if (fCurve instanceof ForwardCurveAffineDividends) {
      final AffineDividends div = ((ForwardCurveAffineDividends) fCurve).getDividends();
      final int number = div.getNumberOfDividends();
      int i = 0;
      while (i < number && div.getTau(i) < time) {
        modSpot = modSpot * (1. - div.getBeta(i)) - div.getAlpha(i) * data.getDiscountCurve().getDiscountFactor(div.getTau(i));
        ++i;
      }
    } else {
      costOfCarry = Math.log(fCurve.getForward(time) / spot) / time;
    }
    return option.getUnitAmount() * MODEL.price(modSpot, strike, interestRate, costOfCarry, time, sigma, isCall);
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    ForwardCurve forwardCurve = data.getForwardCurve();
    final double spot = forwardCurve.getSpot();
    final double strike = option.getStrike();
    final double time = option.getExpiry();
    final double sigma = data.getVolatilitySurface().getVolatility(time, strike);
    final boolean isCall = option.isCall();
    final double interestRate = data.getDiscountCurve().getInterestRate(time);
    final double costOfCarry = time > 0 ? Math.log(forwardCurve.getForward(time) / spot) / time : interestRate;
    return option.getPointValue() * MODEL.price(spot, strike, interestRate, costOfCarry, time, sigma, isCall);
  }
}
