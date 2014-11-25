/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.model.interestrate.curve;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.math.curve.DoublesCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

/**
 * The implementation of a YieldAndDiscount curve where the curve is stored with maturities and zero-coupon continuously-compounded rates.
 */
public class YieldCurve extends YieldAndDiscountCurve {

  /**
   * The curve storing the required data in the zero-coupon continuously compounded convention.
   */
  private final DoublesCurve _curve;

  /**
   * @param name The curve name.
   * @param yieldCurve Curve containing continuously-compounded rates against maturities. Rates are unitless (eg 0.02 for two percent) and maturities are in years.
   */
  public YieldCurve(final String name, final DoublesCurve yieldCurve) {
    super(name);
    ArgumentChecker.notNull(yieldCurve, "Curve");
    _curve = yieldCurve;
  }

  /**
   * Builder from a DoublesCurve using the name of the DoublesCurve as the name of the YieldCurve.
   * @param yieldCurve The underlying curve based on yields (continuously-compounded).
   * @return The yield curve.
   */
  public static YieldCurve from(final DoublesCurve yieldCurve) {
    ArgumentChecker.notNull(yieldCurve, "Curve");
    return new YieldCurve(yieldCurve.getName(), yieldCurve);
  }

  /**
   * Builder of an interpolated yield  (continuously compounded) curve from discount factors.
   * @param nodePoints The node points for the interpolated curve.
   * @param discountFactors The discount factors at the node points.
   * @param interpolator The yield (cc) interpolator.
   * @param name The curve name.
   * @return The yield curve.
   */
  public static YieldCurve fromDiscountFactorInterpolated(final double[] nodePoints, final double[] discountFactors, final Interpolator1D interpolator, final String name) {
    final int nbDF = discountFactors.length;
    ArgumentChecker.isTrue(nodePoints.length == nbDF, "Yields array of incorrect length");
    final double[] yields = new double[nbDF];
    for (int loopy = 0; loopy < nbDF; loopy++) {
      yields[loopy] = -Math.log(discountFactors[loopy]) / nodePoints[loopy];
    }
    final InterpolatedDoublesCurve curve = new InterpolatedDoublesCurve(nodePoints, yields, interpolator, false);
    return new YieldCurve(name, curve);
  }

  @Override
  public double getInterestRate(final Double t) {
    return getCurve().getYValue(t);
  }

  @Override
  public double getForwardRate(final double t) {
    final DoublesCurve curve = getCurve();
    return curve.getYValue(t) + t * curve.getDyDx(t);
  }

  @Override
  public double[] getInterestRateParameterSensitivity(final double t) {
    return ArrayUtils.toPrimitive(_curve.getYValueParameterSensitivity(t));
  }

  @Override
  public int getNumberOfParameters() {
    return _curve.size();
  }

  @Override
  public List<String> getUnderlyingCurvesNames() {
    return new ArrayList<>();
  }

  /**
   * Gets the underlying curve.
   * @return The curve.
   */
  public DoublesCurve getCurve() {
    return _curve;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curve.hashCode();
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
    final YieldCurve other = (YieldCurve) obj;
    return ObjectUtils.equals(_curve, other._curve);
  }

  @Override
  public String toString() {
    return "YieldCurve [_curve=" + _curve + "]";
  }
}
