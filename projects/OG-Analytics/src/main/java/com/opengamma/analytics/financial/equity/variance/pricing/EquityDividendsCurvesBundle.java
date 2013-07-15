/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import static com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils.getLowerBoundIndex;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * Contains forward, interest rate and dividend information.
 */
public class EquityDividendsCurvesBundle {
  /** The forward curve function */
  private final Function1D<Double, Double> _f;
  /** The growth factor curve function */
  private final Function1D<Double, Double> _r;
  /** The growth factor discounted cash dividends curve function */
  private final Function1D<Double, Double> _d;

  /**
   * @param spot The spot, greater than zero
   * @param discountCurve The discount curve, not null
   * @param dividends The dividends, not null
   */
  public EquityDividendsCurvesBundle(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends) {

    ArgumentChecker.isTrue(spot > 0, "Negative spot. S_0 = {}", spot);
    ArgumentChecker.notNull(discountCurve, "null discount curve");
    ArgumentChecker.notNull(dividends, "null dividends");

    _r = getGrowthFactorCurve(discountCurve, dividends);
    _d = getDiscountedCashDividendsCurve(_r, dividends);
    _f = getForwardCurve(spot, _r, dividends);
  }

  /**
   * Gets the forward curve.
   * @return the forward curve
   */
  public Function1D<Double, Double> getF() {
    return _f;
  }

  /**
   * Gets the growth factor curve
   * @return the growth factor curve
   */
  public Function1D<Double, Double> getR() {
    return _r;
  }

  /**
   * Gets the growth factor discounted cash dividends curve
   * @return the growth factor discounted cash dividends curve
   */
  public Function1D<Double, Double> getD() {
    return _d;
  }

  /**
   * Gets the forward value
   * @param t time
   * @return the forward value
   */
  public double getF(final double t) {
    return _f.evaluate(t);
  }

  /**
   * Gets the Growth Factor value
   * @param t time
   * @return the growth factor value
   */
  public double getR(final double t) {
    return _r.evaluate(t);
  }

  /**
   * Gets the growth factor discounted cash dividends value
   * @param t time
   * @return the growth factor discounted cash dividends value
   */
  public double getD(final double t) {
    return _d.evaluate(t);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _d.hashCode();
    result = prime * result + _f.hashCode();
    result = prime * result + _r.hashCode();
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    EquityDividendsCurvesBundle other = (EquityDividendsCurvesBundle) obj;
    if (!ObjectUtils.equals(_d, other._d)) {
      return false;
    }
    if (!ObjectUtils.equals(_f, other._f)) {
      return false;
    }
    if (!ObjectUtils.equals(_r, other._r)) {
      return false;
    }
    return true;
  }

  private static Function1D<Double, Double> getForwardCurve(final double spot, final Function1D<Double, Double> growthFactorCurve, final AffineDividends dividends) {

    if (dividends.getNumberOfDividends() == 0) {
      return new Function1D<Double, Double>() {
        @Override
        public Double evaluate(Double t) {
          return spot * growthFactorCurve.evaluate(t);
        }
      };
    }

    final int n = dividends.getNumberOfDividends();
    final double[] accum = new double[n];
    double sum = dividends.getAlpha(0) / growthFactorCurve.evaluate(dividends.getTau(0));
    accum[0] = sum;
    for (int i = 1; i < n; i++) {
      sum += dividends.getAlpha(i) / growthFactorCurve.evaluate(dividends.getTau(i));
      accum[i] = sum;
    }

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        if (t < dividends.getTau(0)) {
          return spot * growthFactorCurve.evaluate(t);
        }
        int index = getLowerBoundIndex(dividends.getTau(), t);
        double total = accum[index];
        return growthFactorCurve.evaluate(t) * (spot - total);
      }
    };

  }

  private static Function1D<Double, Double> getDiscountedCashDividendsCurve(final Function1D<Double, Double> growthFactorCurve, final AffineDividends dividends) {

    if (dividends.getNumberOfDividends() == 0) {
      return new Function1D<Double, Double>() {
        @Override
        public Double evaluate(Double t) {
          return 0.0;
        }
      };
    }

    final int n = dividends.getNumberOfDividends();
    final double[] accum = new double[n];
    double sum = dividends.getAlpha(n - 1) / growthFactorCurve.evaluate(dividends.getTau(n - 1));
    accum[n - 1] = sum;
    for (int i = n - 2; i >= 0; i--) {
      sum += dividends.getAlpha(i) / growthFactorCurve.evaluate(dividends.getTau(i));
      accum[i] = sum;
    }

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        if (t >= dividends.getTau(n - 1)) {
          return 0.0;
        }
        if (t < dividends.getTau(0)) {
          return growthFactorCurve.evaluate(t) * accum[0];
        }
        int index = getLowerBoundIndex(dividends.getTau(), t) + 1;
        double total = accum[index];
        return growthFactorCurve.evaluate(t) * total;
      }
    };

  }

  private static Function1D<Double, Double> getGrowthFactorCurve(final YieldAndDiscountCurve discCurve, final AffineDividends dividends) {

    if (dividends.getNumberOfDividends() == 0) {
      return new Function1D<Double, Double>() {
        @Override
        public Double evaluate(Double t) {
          return 1.0 / discCurve.getDiscountFactor(t);
        }
      };
    }
    final int n = dividends.getNumberOfDividends();
    final double[] accum = new double[n];
    double prod = (1 - dividends.getBeta(0));
    accum[0] = prod;
    for (int i = 1; i < n; i++) {
      prod *= (1 - dividends.getBeta(i));
      accum[i] = prod;
    }

    return new Function1D<Double, Double>() {
      @Override
      public Double evaluate(Double t) {
        if (t < dividends.getTau(0)) {
          return 1.0 / discCurve.getDiscountFactor(t);
        }
        int index = getLowerBoundIndex(dividends.getTau(), t);
        double total = accum[index];
        return total / discCurve.getDiscountFactor(t);
      }
    };
  }

}
