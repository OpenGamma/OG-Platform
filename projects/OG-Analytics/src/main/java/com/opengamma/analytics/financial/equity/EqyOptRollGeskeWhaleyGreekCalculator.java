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
import com.opengamma.analytics.financial.greeks.Greek;
import com.opengamma.analytics.financial.greeks.GreekResultCollection;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.ForwardCurveAffineDividends;
import com.opengamma.analytics.financial.model.option.pricing.analytic.BaroneAdesiWhaleyModel;
import com.opengamma.analytics.financial.model.option.pricing.analytic.RollGeskeWhaleyModel;
import com.opengamma.util.ArgumentChecker;

/**
 * Calculates the greeks using the Roll-Geske-Whaley model {@link BaroneAdesiWhaleyModel}.
 * <p>
 * The greeks returned are delta, dual-delta, rho, theta and vega.
 */
public final class EqyOptRollGeskeWhaleyGreekCalculator extends InstrumentDerivativeVisitorAdapter<StaticReplicationDataBundle, GreekResultCollection> {
  /** A static instance of this calculator */
  private static final EqyOptRollGeskeWhaleyGreekCalculator INSTANCE = new EqyOptRollGeskeWhaleyGreekCalculator();
  /** The pricing model */
  private static final RollGeskeWhaleyModel MODEL = new RollGeskeWhaleyModel();

  /**
   * @return A static instance of this class
   */
  public static EqyOptRollGeskeWhaleyGreekCalculator getInstance() {
    return INSTANCE;
  }

  private EqyOptRollGeskeWhaleyGreekCalculator() {
  }

  @Override
  public GreekResultCollection visitEquityIndexOption(final EquityIndexOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");

    if (!option.isCall()) {
      return EqyOptBjerksundStenslandGreekCalculator.getInstance().visitEquityIndexOption(option, data);
    }

    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);

    final ForwardCurve fCurve = data.getForwardCurve();
    double[] divTime = null;
    double[] divAmount = null;
    if (fCurve instanceof ForwardCurveAffineDividends) {
      final AffineDividends div = ((ForwardCurveAffineDividends) data.getForwardCurve()).getDividends();
      divTime = div.getTau();
      divAmount = div.getAlpha();
    } else {
      divTime = new double[] {0. };
      divAmount = new double[] {0. };
    }

    final double volatility = data.getVolatilitySurface().getVolatility(t, k);

    final double[] greeks = MODEL.getPriceAdjoint(s, k, r, t, volatility, divAmount, divTime);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, greeks[1]);
    result.put(Greek.DUAL_DELTA, greeks[2]);
    result.put(Greek.RHO, greeks[3] / 100.);
    result.put(Greek.CARRY_RHO, 0.);
    result.put(Greek.THETA, -greeks[4] / 365. - greeks[5] / 365.);
    result.put(Greek.VEGA, greeks[6] / 100.);
    result.put(Greek.GAMMA, greeks[7]);
    return result;
  }

  @Override
  public GreekResultCollection visitEquityOption(final EquityOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");

    if (!option.isCall()) {
      final double k = option.getStrike();
      final double t = option.getTimeToExpiry();
      return EqyOptBjerksundStenslandGreekCalculator.getInstance().getGreeksDirectEquityOption(option, data, data.getVolatilitySurface().getVolatility(t, k));
    }

    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getTimeToExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);

    final ForwardCurve fCurve = data.getForwardCurve();
    double[] divTime = null;
    double[] divAmount = null;
    if (fCurve instanceof ForwardCurveAffineDividends) {
      final AffineDividends div = ((ForwardCurveAffineDividends) data.getForwardCurve()).getDividends();
      divTime = div.getTau();
      divAmount = div.getAlpha();
    } else {
      divTime = new double[] {0. };
      divAmount = new double[] {0. };
    }

    final double volatility = data.getVolatilitySurface().getVolatility(t, k);

    final double[] greeks = MODEL.getPriceAdjoint(s, k, r, t, volatility, divAmount, divTime);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, greeks[1]);
    result.put(Greek.DUAL_DELTA, greeks[2]);
    result.put(Greek.RHO, greeks[3] / 100.);
    result.put(Greek.CARRY_RHO, 0.);
    result.put(Greek.THETA, -greeks[4] / 365. - greeks[5] / 365.);
    result.put(Greek.VEGA, greeks[6] / 100.);
    result.put(Greek.GAMMA, greeks[7]);
    return result;
  }

  @Override
  public GreekResultCollection visitEquityIndexFutureOption(final EquityIndexFutureOption option, final StaticReplicationDataBundle data) {
    ArgumentChecker.notNull(option, "option");
    ArgumentChecker.notNull(data, "data");

    if (!option.isCall()) {
      return EqyOptBjerksundStenslandGreekCalculator.getInstance().visitEquityIndexFutureOption(option, data);
    }

    final double s = data.getForwardCurve().getSpot();
    final double k = option.getStrike();
    final double t = option.getExpiry();
    final double r = data.getDiscountCurve().getInterestRate(t);

    final ForwardCurve fCurve = data.getForwardCurve();
    double[] divTime = null;
    double[] divAmount = null;
    if (fCurve instanceof ForwardCurveAffineDividends) {
      final AffineDividends div = ((ForwardCurveAffineDividends) data.getForwardCurve()).getDividends();
      divTime = div.getTau();
      divAmount = div.getAlpha();
    } else {
      divTime = new double[] {0. };
      divAmount = new double[] {0. };
    }

    final double volatility = data.getVolatilitySurface().getVolatility(t, k);

    final double[] greeks = MODEL.getPriceAdjoint(s, k, r, t, volatility, divAmount, divTime);
    final GreekResultCollection result = new GreekResultCollection();
    result.put(Greek.DELTA, greeks[1]);
    result.put(Greek.DUAL_DELTA, greeks[2]);
    result.put(Greek.RHO, greeks[3] / 100.);
    result.put(Greek.CARRY_RHO, 0.);
    result.put(Greek.THETA, -greeks[4] / 365. - greeks[5] / 365.);
    result.put(Greek.VEGA, greeks[6] / 100.);
    result.put(Greek.GAMMA, greeks[7]);
    return result;
  }

  /**
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
      if (!option.isCall()) {
        return EqyOptBjerksundStenslandGreekCalculator.getInstance().getGreeksDirectEquityOption(option, data, impliedVol);
      }

      final double s = data.getForwardCurve().getSpot();
      final double k = option.getStrike();
      final double t = option.getTimeToExpiry();
      final double r = data.getDiscountCurve().getInterestRate(t);

      final ForwardCurve fCurve = data.getForwardCurve();
      double[] divTime = null;
      double[] divAmount = null;
      if (fCurve instanceof ForwardCurveAffineDividends) {
        final AffineDividends div = ((ForwardCurveAffineDividends) data.getForwardCurve()).getDividends();
        divTime = div.getTau();
        divAmount = div.getAlpha();
      } else {
        divTime = new double[] {0. };
        divAmount = new double[] {0. };
      }

      final double[] greeks = MODEL.getPriceAdjoint(s, k, r, t, impliedVol, divAmount, divTime);
      result.put(Greek.DELTA, greeks[1]);
      result.put(Greek.DUAL_DELTA, greeks[2]);
      result.put(Greek.RHO, greeks[3] / 100.);
      result.put(Greek.CARRY_RHO, 0.);
      result.put(Greek.THETA, -greeks[4] / 365. - greeks[5] / 365.);
      result.put(Greek.VEGA, greeks[6] / 100.);
      result.put(Greek.GAMMA, greeks[7]);
    }
    return result;
  }
}
