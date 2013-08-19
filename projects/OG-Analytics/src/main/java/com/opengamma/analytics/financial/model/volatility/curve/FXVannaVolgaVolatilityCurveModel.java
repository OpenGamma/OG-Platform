/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.volatility.curve;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.option.definition.FXOptionDataBundle;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.statistics.distribution.NormalDistribution;
import com.opengamma.analytics.math.statistics.distribution.ProbabilityDistribution;
import com.opengamma.util.time.DateUtils;

/**
 * 
 */
public class FXVannaVolgaVolatilityCurveModel implements VolatilityCurveModel<FXVannaVolgaVolatilityCurveDataBundle, FXOptionDataBundle> {
  private static final ProbabilityDistribution<Double> NORMAL = new NormalDistribution(0, 1);

  @Override
  public VolatilityCurve getCurve(final FXVannaVolgaVolatilityCurveDataBundle marketQuotes, final FXOptionDataBundle data) {
    Validate.notNull(marketQuotes);
    Validate.notNull(data);
    final double sigmaRR = marketQuotes.getRiskReversal();
    final double sigmaATM = marketQuotes.getAtTheMoney();
    final double sigmaVWB = marketQuotes.getVegaWeightedButterfly();
    final double sigmaDeltaCall = sigmaVWB + sigmaATM + 0.5 * sigmaRR;
    final double sigmaDeltaPut = sigmaDeltaCall - sigmaRR;
    final double t = DateUtils.getDifferenceInYears(data.getDate(), marketQuotes.getMaturity());
    if (t < 0) {
      throw new IllegalArgumentException("Cannot have date after time to maturity");
    }
    final double sqrtT = Math.sqrt(t);
    final double s = data.getSpot();
    final double rd = data.getInterestRate(t);
    final double rf = data.getForeignInterestRate(t);
    final double alpha = -NORMAL.getInverseCDF(Math.exp(rf * t) * marketQuotes.getDelta());
    final double k1 = s * Math.exp(-alpha * sigmaDeltaPut * sqrtT + t * (rd - rf + 0.5 * sigmaDeltaPut * sigmaDeltaPut));
    final double k2 = s * Math.exp(t * (rd - rf + 0.5 * sigmaATM * sigmaATM));
    final double k3 = s * Math.exp(alpha * sigmaDeltaCall * sqrtT + t * (rd - rf + 0.5 * sigmaDeltaCall * sigmaDeltaCall));
    final double lnk21 = Math.log(k2 / k1);
    final double lnk31 = Math.log(k3 / k1);
    final double lnk32 = Math.log(k3 / k2);
    final double sigma = sigmaATM;
    return new VolatilityCurve(FunctionalDoublesCurve.from(new Function1D<Double, Double>() {

      @SuppressWarnings("synthetic-access")
      @Override
      public Double evaluate(final Double x) {
        Validate.notNull(x);

        final double k = x;
        final double a1 = Math.log(k2 / k) * Math.log(k3 / k) / lnk21 / lnk31;
        final double a2 = Math.log(k / k1) * Math.log(k3 / k) / lnk21 / lnk32;
        final double a3 = Math.log(k / k1) * Math.log(k / k2) / lnk31 / lnk32;
        final double x1 = a1 * sigmaDeltaPut;
        final double x2 = a2 * sigmaATM;
        final double x3 = a3 * sigmaDeltaCall;
        final double e1 = x1 + x2 + x3 - sigma;
        final double d1k1 = getD1(s, k1, t, rd, rf, sigma, sqrtT);
        final double d1k2 = getD1(s, k2, t, rd, rf, sigma, sqrtT);
        final double d1k3 = getD1(s, k3, t, rd, rf, sigma, sqrtT);
        final double x4 = a1 * d1k1 * getD2(d1k1, sigma, sqrtT) * (sigmaDeltaPut - sigma) * (sigmaDeltaPut - sigma);
        final double x5 = a2 * d1k2 * getD2(d1k2, sigma, sqrtT) * (sigmaATM - sigma) * (sigmaATM - sigma);
        final double x6 = a3 * d1k3 * getD2(d1k3, sigma, sqrtT) * (sigmaDeltaCall - sigma) * (sigmaDeltaCall - sigma);
        final double e2 = x4 + x5 + x6;
        final double d1k = getD1(s, k, t, rd, rf, sigma, sqrtT);
        final double d2k = getD2(d1k, sigma, sqrtT);
        return sigma + (-sigma + Math.sqrt(sigma * sigma + d1k * d2k * (2 * sigma * e1 + e2))) / d1k / d2k;
      }

    }));
  }

  private double getD1(final double s, final double k, final double t, final double rd, final double rf, final double sigma, final double sqrtT) {
    return (Math.log(s / k) + t * (rd - rf + 0.5 * sigma * sigma)) / sigma / sqrtT;
  }

  private double getD2(final double d1, final double sigma, final double sqrtT) {
    return d1 - sigma * sqrtT;
  }
}
