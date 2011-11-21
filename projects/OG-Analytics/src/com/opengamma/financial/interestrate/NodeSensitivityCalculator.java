/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, this calculates the sensitivity of either the present value (PV) or par Rate to the yield at the knot points of the interpolated yield curves. The return format is
 * a DoubleMatrix1D (i.e. a vector) with length equal to the total number of knots in all the curves, and ordered as sensitivity to knots of first curve, second curve etc. 
 * The change of a curve due to the movement of a single knot is interpolator-dependent, and can affect the entire curve, so an instrument can have sensitivity to knots at times (way)
 * beyond its maturity 
 */
public abstract class NodeSensitivityCalculator {

  /* package */NodeSensitivityCalculator() {
  }

  public abstract DoubleMatrix1D calculateSensitivities(final InstrumentDerivative ird, final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves);

  public DoubleMatrix1D calculateSensitivities(final InstrumentDerivative ird, final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> calculator,
      final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves) {
    Validate.notNull(ird, "null InterestRateDerivative");
    Validate.notNull(calculator, "null calculator");
    Validate.notNull(interpolatedCurves, "interpolated curves");
    final YieldCurveBundle allCurves = (interpolatedCurves instanceof SABRInterestRateDataBundle) ? new SABRInterestRateDataBundle((SABRInterestRateDataBundle) interpolatedCurves)
        : new YieldCurveBundle(interpolatedCurves);
    if (fixedCurves != null) {
      for (final String name : interpolatedCurves.getAllNames()) {
        Validate.isTrue(!fixedCurves.containsName(name), "fixed curves contain a name that is also in interpolated curves");
      }
      allCurves.addAll(fixedCurves);
    }
    final Map<String, List<DoublesPair>> sensitivityMap = calculator.visit(ird, allCurves);
    return curveToNodeSensitivities(sensitivityMap, interpolatedCurves);
  }

  public DoubleMatrix1D curveToNodeSensitivities(final Map<String, List<DoublesPair>> curveSensitivities, final YieldCurveBundle interpolatedCurves) {
    final List<Double> result = new ArrayList<Double>();
    for (final String name : interpolatedCurves.getAllNames()) { // loop over all curves (by name)
      final YieldAndDiscountCurve curve = interpolatedCurves.getCurve(name);
      if (!(curve.getCurve() instanceof InterpolatedDoublesCurve)) {
        throw new IllegalArgumentException("Can only handle interpolated curves at the moment");
      }
      final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve.getCurve();
      final Interpolator1D interpolator = interpolatedCurve.getInterpolator();
      final Interpolator1DDataBundle data = interpolatedCurve.getDataBundle();
      final List<DoublesPair> sensitivityList = curveSensitivities.get(name);
      if (sensitivityList != null && sensitivityList.size() > 0) {
        final double[][] sensitivity = new double[sensitivityList.size()][];
        int k = 0;
        for (final DoublesPair timeAndDF : sensitivityList) {
          sensitivity[k++] = interpolator.getNodeSensitivitiesForValue(data, timeAndDF.getFirst());
        }
        for (int j = 0; j < sensitivity[0].length; j++) {
          double temp = 0.0;
          k = 0;
          for (final DoublesPair timeAndDF : sensitivityList) {
            temp += timeAndDF.getSecond() * sensitivity[k++][j];
          }
          result.add(temp);
        }
      } else {
        for (int i = 0; i < interpolatedCurve.size(); i++) {
          result.add(0.);
        }
      }
    }
    return new DoubleMatrix1D(result.toArray(new Double[0]));
  }

  public DoubleMatrix1D curveToNodeSensitivities(final List<DoublesPair> curveSensitivities, final YieldAndDiscountCurve yieldCurve) {
    if (!(yieldCurve.getCurve() instanceof InterpolatedDoublesCurve)) {
      throw new IllegalArgumentException("Can only handle InterpolatedDoublesCurve");
    }
    final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) yieldCurve.getCurve();
    final double[] result = new double[interpolatedCurve.size()];
    final Interpolator1D interpolator = interpolatedCurve.getInterpolator();
    final Interpolator1DDataBundle data = interpolatedCurve.getDataBundle();
    if (curveSensitivities == null) {
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        result[i] = 0;
      }
    } else {
      final double[][] sensitivity = new double[curveSensitivities.size()][];
      int k = 0;
      for (final DoublesPair timeAndDF : curveSensitivities) {
        sensitivity[k++] = interpolator.getNodeSensitivitiesForValue(data, timeAndDF.getFirst());
      }
      for (int j = 0; j < sensitivity[0].length; j++) {
        double temp = 0.0;
        k = 0;
        for (final DoublesPair timeAndDF : curveSensitivities) {
          temp += timeAndDF.getSecond() * sensitivity[k++][j];
        }
        result[j] = temp;
      }
    }
    return new DoubleMatrix1D(result);
  }

}
