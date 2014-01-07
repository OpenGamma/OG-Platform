/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import static com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils.getLowerBoundIndex;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.equity.variance.pricing.AffineDividends;
import com.opengamma.analytics.math.curve.ConstantDoublesCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.FunctionalDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.util.serialization.InvokedSerializedForm;
import com.opengamma.util.ArgumentChecker;

/**
 * ForwardCurve for Equity assets that are modelled to pay known discrete dividends 
 * with an affine form: d(i) = alpha[i] + beta[i]*share_price(tau[i])
 */
public class ForwardCurveAffineDividends extends ForwardCurve {

  private final YieldAndDiscountCurve _riskFreeCurve;
  private final AffineDividends _dividends;

  public ForwardCurveAffineDividends(final double spot, final YieldAndDiscountCurve riskFreeCurve, final AffineDividends dividends) {
    super(getForwardCurve(spot, riskFreeCurve, dividends));
    _riskFreeCurve = riskFreeCurve;
    _dividends = dividends;
  }

  /**
   * @param spot The spot, greater than zero
   * @param riskFreeCurve The discount curve, not null
   * @param dividends The dividends, not null
   * @return FunctionalDoublesCurve with discrete dividends of an affine form: d(i) = alpha[i] + beta[i]*share_price(tau[i]) 
   */
  protected static Curve<Double, Double> getForwardCurve(final double spot, final YieldAndDiscountCurve riskFreeCurve, final AffineDividends dividends) {
    ArgumentChecker.isTrue(spot > 0, "Negative spot. S_0 = {}", spot);
    ArgumentChecker.notNull(riskFreeCurve, "null risk free curve");
    ArgumentChecker.notNull(dividends, "null dividends");
    if (dividends.getNumberOfDividends() == 0) {
      return getForwardCurve(spot, riskFreeCurve, YieldCurve.from(ConstantDoublesCurve.from(0.0)));
    }
    final Function1D<Double, Double> f = new Function1D<Double, Double>() {

      @Override
      public Double evaluate(final Double t) {
        final int n = dividends.getNumberOfDividends();
        final double[] growthFactor = new double[n];
        final double[] accumProd = new double[n];
        final double[] accumSum = new double[n];
        double prod = 1.0;
        double sum = 0.0;
        for (int i = 0; i < n; i++) {
          prod *= (1 - dividends.getBeta(i));
          accumProd[i] = prod;
          growthFactor[i] = prod / riskFreeCurve.getDiscountFactor(dividends.getTau(i));
          sum += dividends.getAlpha(i) / growthFactor[i];
          accumSum[i] = sum;
        }
        if (t < dividends.getTau(0)) {
          return spot / riskFreeCurve.getDiscountFactor(t);
        }
        final int index = getLowerBoundIndex(dividends.getTau(), t);
        final double total = accumSum[index];
        return accumProd[index] / riskFreeCurve.getDiscountFactor(t) * (spot - total);
      }
    };

    return new FunctionalDoublesCurve(f) {
      public Object writeReplace() {
        return new InvokedSerializedForm(ForwardCurveAffineDividends.class, "getForwardCurve", spot, riskFreeCurve, dividends);
      }
    };
  }

  public YieldAndDiscountCurve getRiskFreeCurve() {
    return _riskFreeCurve;
  }

  public AffineDividends getDividends() {
    return _dividends;
  }

  
  /**
   * Shift the forward curve by a fractional amount, shift, such that the new curve F'(T) = (1 + shift) * F(T), has
   * an unchanged drift.
   * @param shift The fractional shift amount, i.e. 0.1 will produce a curve 10% larger than the original
   * @return The shifted curve
   */
  @Override
  public ForwardCurveAffineDividends withFractionalShift(final double shift) {
    ArgumentChecker.isTrue(shift > -1, "shift must be > -1");
    return new ForwardCurveAffineDividends((1 + shift) * getSpot(),  getRiskFreeCurve(), getDividends());
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + getDriftCurve().hashCode();
    result = prime * result + getForwardCurve().hashCode();
    long temp;
    temp = Double.doubleToLongBits(getSpot());
    result = prime * result + (int) (temp ^ (temp >>> 32));
    result = prime * result + getRiskFreeCurve().hashCode();
    result = prime * result + getDividends().hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ForwardCurveAffineDividends other = (ForwardCurveAffineDividends) obj;
    if (!ObjectUtils.equals(getRiskFreeCurve(), other.getRiskFreeCurve())) {
      return false;
    }
    if (!ObjectUtils.equals(getDividends(), other.getDividends())) {
      return false;
    }
    if (Double.doubleToLongBits(getSpot()) != Double.doubleToLongBits(other.getSpot())) {
      return false;
    }
    return true;
  }

}
