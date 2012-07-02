/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.equity.variance.pricing;

import static com.opengamma.analytics.financial.model.volatility.smile.fitting.interpolation.SurfaceArrayUtils.getLowerBoundIndex;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 */
public class EquityDividendsCurvesBundle {

  private final Function1D<Double, Double> _f;
  private final Function1D<Double, Double> _r;
  private final Function1D<Double, Double> _d;

  public EquityDividendsCurvesBundle(final double spot, final YieldAndDiscountCurve discountCurve, final AffineDividends dividends) {

    ArgumentChecker.isTrue(spot > 0, "Negative spot. S_0 = {}", spot);
    ArgumentChecker.notNull(discountCurve, "null discount curve");
    ArgumentChecker.notNull(dividends, "null dividends");

    _r = getGrowthFactorCurve(discountCurve, dividends);
    _d = getDiscountedCashDividendsCurve(_r, dividends);
    _f = getForwardCurve(spot, _r, dividends);
  }

  /**
   * Gets the forward curve
   * .
   * @return the forward curve
   */
  public Function1D<Double, Double> getF() {
    return _f;
  }

  /**
   * Gets the Growth Factor Curve
   * @return the Growth Factor Curve
   */
  public Function1D<Double, Double> getR() {
    return _r;
  }

  /**
   * Gets the Growth Factor Discounted Cash Dividends Curve
   * @return the Growth Factor Discounted Cash Dividends Curve
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
   * @return the Growth Factor value
   */
  public double getR(final double t) {
    return _r.evaluate(t);
  }

  /**
   * Gets the Growth Factor Discounted Cash Dividends value
  * @param t time
   * @return the Growth Factor Discounted Cash Dividends value
   */
  public double getD(final double t) {
    return _d.evaluate(t);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_d == null) ? 0 : _d.hashCode());
    result = prime * result + ((_f == null) ? 0 : _f.hashCode());
    result = prime * result + ((_r == null) ? 0 : _r.hashCode());
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
    if (_d == null) {
      if (other._d != null) {
        return false;
      }
    } else if (!_d.equals(other._d)) {
      return false;
    }
    if (_f == null) {
      if (other._f != null) {
        return false;
      }
    } else if (!_f.equals(other._f)) {
      return false;
    }
    if (_r == null) {
      if (other._r != null) {
        return false;
      }
    } else if (!_r.equals(other._r)) {
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
        double sum = accum[index];
        return growthFactorCurve.evaluate(t) * (spot - sum);
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
        double sum = accum[index];
        return growthFactorCurve.evaluate(t) * sum;
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
        double prod = accum[index];
        return prod / discCurve.getDiscountFactor(t);
      }
    };
  }

}
