/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity;

import com.opengamma.analytics.financial.ExerciseDecisionType;
import com.opengamma.analytics.financial.equity.option.EquityIndexFutureOption;
import com.opengamma.analytics.financial.equity.option.EquityIndexOption;
import com.opengamma.analytics.financial.equity.option.EquityOption;
import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BaroneAdesiWhaleyModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BjerksundStenslandModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the greeks of a commodity future option using the Barone-Adesi Whaley model {@link BaroneAdesiWhaleyModel}.
 * <p>
 * The greeks returned are delta, dual-delta, rho, carry rho, theta and vega.
 */
public final class EqyOptBjerksundStenslandGreekCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, GreekResultCollection> {
  /** A static instance of this calculator */
  private static final EqyOptBjerksundStenslandGreekCalculator INSTANCE = new EqyOptBjerksundStenslandGreekCalculator();
  /** The pricing model */
  private static final BjerksundStenslandModel MODEL = new BjerksundStenslandModel();

  /**
   * @return A static instance of this class
   */
  public static EqyOptBjerksundStenslandGreekCalculator getInstance() {
    return INSTANCE;
  }

  private EqyOptBjerksundStenslandGreekCalculator() {
  }

  @Override
  public GreekResultCollection visitEquityIndexOption(EquityIndexOption option, StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(option.getExerciseType() == ExerciseDecisionType.AMERICAN, "option must be American");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    return computeGreeks(k, t, isCall, data);
  }

  @Override
  public GreekResultCollection visitEquityOption(EquityOption option, StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    ArgumentChecker.isTrue(option.getExerciseType() == ExerciseDecisionType.AMERICAN, "option must be American");
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final boolean isCall = option.isCall();
    return computeGreeks(k, t, isCall, data);
  }

  /**
   * If MARKET_VALUE is available, volatility implied by Bjerksund-Stensland model is used. 
   * @param option Equity option
   * @param data Market data
   * @param impliedVol The implied volatility
   * @return Greeks
   */
  public GreekResultCollection getGreeksDirectEquityOption(final EquityOption option, final StaticReplicationDataBundle data, final double impliedVol) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final GreekResultCollection result = new GreekResultCollection();

    if (impliedVol == 0.) {
      result.put(Greek.DELTA, 0.);
      result.put(Greek.DUAL_DELTA, 0.);
      result.put(Greek.RHO, 0.);
      result.put(Greek.CARRY_RHO, 0.);
      result.put(Greek.THETA, 0.);
      result.put(Greek.VEGA, 0.);
      result.put(Greek.GAMMA, 0.);
    } else {
      final double s = data.getForwardCurve().getSpot();
      final double k = option.getStrike();
      final double t = option.getTimeToExpiry();
      final double r = data.getDiscountCurve().getInterestRate(t);
      double b = r;
      double modSpot = s;

      final ForwardCurve fCurve = data.getForwardCurve();
      if (fCurve instanceof ForwardCurveAffineDividends) {
        final AffineDividends div = ((ForwardCurveAffineDividends) data.getForwardCurve()).getDividends();
        final int number = div.getNumberOfDividends();
        int i = 0;
        while (i < number && div.getTau(i) < t) {
          modSpot = modSpot * (1. - div.getBeta(i)) - div.getAlpha(i) * data.getDiscountCurve().getDiscountFactor(div.getTau(i));
          ++i;
        }
      } else {
        b = Math.log(data.getForwardCurve().getForward(t) / s) / t;
      }
      final boolean isCall = option.isCall();
      final double[] greeks = MODEL.getPriceAdjoint(modSpot, k, r, b, t, impliedVol, isCall);
      result.put(Greek.DELTA, greeks[1]);
      result.put(Greek.DUAL_DELTA, greeks[2]);
      result.put(Greek.RHO, greeks[3] / 100. + greeks[4] / 100.);
      result.put(Greek.CARRY_RHO, greeks[4] / 100.);
      result.put(Greek.THETA, -greeks[5] / 365.);
      result.put(Greek.VEGA, greeks[6] / 100.);
      final double[] pdg = MODEL.getPriceDeltaGamma(modSpot, k, r, b, t, impliedVol, isCall);
      result.put(Greek.GAMMA, pdg[2]);
    }
    return result;
  }

  @Override
  public GreekResultCollection visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");
    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);
    final double b = r; //TODO
    final double volatility = data.getVolatilitySurface().getVolatility(t, k);
    final boolean isCall = option.isCall();
    final double[] greeks = MODEL.getPriceAdjoint(s, k, r, b, t, volatility, isCall);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, greeks[1]);
    result.put(Greek.DUAL_DELTA, greeks[2]);
    result.put(Greek.RHO, greeks[3] / 100.);
    result.put(Greek.CARRY_RHO, greeks[4] / 100.);
    result.put(Greek.THETA, -greeks[5] / 365.);
    result.put(Greek.VEGA, greeks[6] / 100.);
    final double[] pdg = MODEL.getPriceDeltaGamma(s, k, r, b, t, volatility, isCall);
    result.put(Greek.GAMMA, pdg[2]);
    return result;
  }

  private GreekResultCollection computeGreeks(double strike, double time, boolean isCall,
      StaticReplicationDataBundle data) {
    double s = data.getForwardCurve().getSpot();
    double r = data.getDiscountCurve().getInterestRate(time);
    double b = time > 0 ? Math.log(data.getForwardCurve().getForward(time) / s) / time : r;
    double volatility = data.getVolatilitySurface().getVolatility(time, strike);
    double[] greeks = MODEL.getPriceAdjoint(s, strike, r, b, time, volatility, isCall);
    GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, greeks[1]);
    result.put(Greek.DUAL_DELTA, greeks[2]);
    // The standard rho assumes that the yield, q, is fixed where b = r - q, 
    // thus carry rho should be added. 
    result.put(Greek.RHO, greeks[3] / 100. + greeks[4] / 100.);
    result.put(Greek.CARRY_RHO, greeks[4] / 100.);
    result.put(Greek.THETA, -greeks[5] / 365.);
    result.put(Greek.VEGA, greeks[6] / 100.);
    double[] pdg = MODEL.getPriceDeltaGamma(s, strike, r, b, time, volatility, isCall);
    result.put(Greek.GAMMA, pdg[2]);
    return result;
  }
}
