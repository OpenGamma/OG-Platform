/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.analytics.financial.model.option.pricing.analytic.RollGeskeWhaleyModel;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public final class EqyOptRollGeskeWhaleyPresentValueCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, Double> {
  /** A static instance */
  private static final EqyOptRollGeskeWhaleyPresentValueCalculator INSTANCE = new EqyOptRollGeskeWhaleyPresentValueCalculator();
  /** The present value calculator */
  private static final RollGeskeWhaleyModel MODEL = new RollGeskeWhaleyModel();

  /**
   * Gets the static instance
   * @return The static instance
   */
  public static EqyOptRollGeskeWhaleyPresentValueCalculator getInstance() {
    return INSTANCE;
  }

  private EqyOptRollGeskeWhaleyPresentValueCalculator() {
  }

  @Override
  public Double visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");

    if (!option.isCall()) {
      return EqyOptBjerksundStenslandPresentValueCalculator.getInstance().visitEquityIndexOption(option, data);
    }

    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double unitAmount = option.getUnitAmount();

    return getPresetValue(unitAmount, s, k, t, r, data);
  }

  @Override
  public Double visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");

    if (!option.isCall()) {
      return EqyOptBjerksundStenslandPresentValueCalculator.getInstance().visitEquityOption(option, data);
    }

    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double unitAmount = option.getUnitAmount();

    return getPresetValue(unitAmount, s, k, t, r, data);
  }

  @Override
  public Double visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");

    if (!option.isCall()) {
      return EqyOptBjerksundStenslandPresentValueCalculator.getInstance().visitEquityIndexFutureOption(option, data);
    }

    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double pointValue = option.getPointValue();

    return getPresetValue(pointValue, s, k, t, r, data);
  }

  private double getPresetValue(final double factor, final double spot, final double strike, final double time, final double interestRate, final StaticReplicationDataBundle data) {
    final ForwardCurve fCurve = data.getForwardCurve();
    double[] divTime = null;
    double[] divAmount = null;
    if (fCurve instanceof ForwardCurveAffineDividends) {
      final AffineDividends div = ((ForwardCurveAffineDividends) fCurve).getDividends();
      divTime = div.getTau();
      divAmount = div.getAlpha();
    } else {
      divTime = new double[] {0. };
      divAmount = new double[] {0. };
    }
    final double volatility = data.getVolatilitySurface().getVolatility(time, strike);

    return factor * MODEL.price(spot, strike, interestRate, time, volatility, divAmount, divTime);
  }

}
