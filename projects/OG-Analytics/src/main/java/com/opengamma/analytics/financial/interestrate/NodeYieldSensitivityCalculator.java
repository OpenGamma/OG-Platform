/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.DiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * For an instrument, calculates the sensitivity of a value (often either the present value (PV) or par rate) to the yield at the knot points of the interpolated yield curves.
 * The return format is a DoubleMatrix1D (i.e. a vector) with length equal to the total number of nodes in all the curves, and ordered as sensitivity to nodes of first curve, second curve etc.
 * The change of a curve due to the movement of a single knot is interpolator-dependent, and can affect the entire curve, so an instrument can have sensitivity to nodes at times (way)
 * beyond its maturity.
 * @deprecated {@link YieldCurveBundle} is deprecated
 */
@Deprecated
public abstract class NodeYieldSensitivityCalculator {

  /* package */
  NodeYieldSensitivityCalculator() {
  }

  public abstract DoubleMatrix1D calculateSensitivities(final InstrumentDerivative ird, final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves);

  /**
   * Computes the sensitivities to the yield at the node points for an instrument from a yield sensitivity calculator.
   * @param derivative The instrument.
   * @param calculator The yield sensitivity calculator.
   * @param fixedCurves The fixed curves.
   * @param interpolatedCurves The curves with respect to which the sensitivities should be computed. The curves should be based on InterpolatedDoublesCurve.
   * @return The yield sensitivities to the node points.
   */
  public DoubleMatrix1D calculateSensitivities(final InstrumentDerivative derivative, final InstrumentDerivativeVisitor<YieldCurveBundle, Map<String, List<DoublesPair>>> calculator,
      final YieldCurveBundle fixedCurves, final YieldCurveBundle interpolatedCurves) {
    Validate.notNull(derivative, "null InterestRateDerivative");
    Validate.notNull(calculator, "null calculator");
    Validate.notNull(interpolatedCurves, "interpolated curves");
    final YieldCurveBundle allCurves = interpolatedCurves.copy();
    if (fixedCurves != null) {
      for (final String name : interpolatedCurves.getAllNames()) {
        Validate.isTrue(!fixedCurves.containsName(name), "fixed curves contain a name that is also in interpolated curves");
      }
      allCurves.addAll(fixedCurves);
    }
    final Map<String, List<DoublesPair>> sensitivityMap = derivative.accept(calculator, allCurves);
    return curveToNodeSensitivities(sensitivityMap, interpolatedCurves);
  }

  /**
   * Computes the sensitivity to the yield at the node points from the sensitivities to the yield at arbitrary points.
   * @param curveSensitivities The sensitivity to the yield at arbitrary points on the different curves.
   * @param interpolatedCurves The curve bundle. The curves should be YieldCurve based on InterpolatedDoublesCurve.
   * @return The yield sensitivities to the node points.
   */
  public DoubleMatrix1D curveToNodeSensitivities(final Map<String, List<DoublesPair>> curveSensitivities, final YieldCurveBundle interpolatedCurves) {
    final List<Double> result = new ArrayList<>();
    for (final String name : interpolatedCurves.getAllNames()) { // loop over all curves (by name)
      final YieldAndDiscountCurve curve = interpolatedCurves.getCurve(name);
      // Split between Yield and Discount
      if (curve instanceof YieldCurve) {
        result.addAll(curveToNodeSensitivity(curveSensitivities.get(name), (YieldCurve) curve));
      } else if (curve instanceof DiscountCurve) {
        result.addAll(curveToNodeSensitivity(curveSensitivities.get(name), (DiscountCurve) curve));
      } else {
        throw new IllegalArgumentException("Can only handle YieldCurve and DiscountCurve at the moment");
      }
    }
    return new DoubleMatrix1D(result.toArray(new Double[result.size()]));
  }

  /**
   * Computes the node yield sensitivity for one YieldCurve.
   * @param sensitivityList The sensitivity to the yield at arbitrary points on the curve.
   * @param curve The YieldCurve.
   * @return The node sensitivity.
   */
  public List<Double> curveToNodeSensitivity(final List<DoublesPair> sensitivityList, final YieldCurve curve) {
    final List<Double> result = new ArrayList<>();
    if (!(curve.getCurve() instanceof InterpolatedDoublesCurve)) {
      throw new IllegalArgumentException("Can only handle interpolated curves at the moment");
    }
    final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve.getCurve();
    final Interpolator1D interpolator = interpolatedCurve.getInterpolator();
    final Interpolator1DDataBundle data = interpolatedCurve.getDataBundle();
    if (sensitivityList != null && sensitivityList.size() > 0) {
      final double[][] sensitivityYY = new double[sensitivityList.size()][];
      // Implementation note: Sensitivity of the interpolated yield to the node yields
      int k = 0;
      for (final DoublesPair timeAndS : sensitivityList) {
        sensitivityYY[k++] = interpolator.getNodeSensitivitiesForValue(data, timeAndS.getFirst());
      }
      for (int j = 0; j < sensitivityYY[0].length; j++) {
        double temp = 0.0;
        k = 0;
        for (final DoublesPair timeAndS : sensitivityList) {
          temp += timeAndS.getSecond() * sensitivityYY[k++][j];
        }
        result.add(temp);
      }
    } else {
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        result.add(0.);
      }
    }
    return result;
  }

  /**
   * Computes the node yield sensitivity for one YieldCurve.
   * @param sensitivityList The sensitivity to the yield at arbitrary points on the curve.
   * @param curve The YieldCurve.
   * @return The node sensitivity.
   */
  public List<Double> curveToNodeSensitivity(final List<DoublesPair> sensitivityList, final DiscountCurve curve) {
    final List<Double> result = new ArrayList<>();
    if (!(curve.getCurve() instanceof InterpolatedDoublesCurve)) {
      throw new IllegalArgumentException("Can only handle interpolated curves at the moment");
    }
    final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve.getCurve();
    final Interpolator1D interpolator = interpolatedCurve.getInterpolator();
    final Interpolator1DDataBundle data = interpolatedCurve.getDataBundle();
    if (sensitivityList != null && sensitivityList.size() > 0) {
      final double[][] sensitivityDD = new double[sensitivityList.size()][];
      // Implementation note: Sensitivity of the interpolated discount factor to the node discount factor
      final double[] df = new double[sensitivityList.size()];
      int k = 0;
      for (final DoublesPair timeAndS : sensitivityList) {
        df[k] = interpolator.interpolate(data, timeAndS.first);
        sensitivityDD[k++] = interpolator.getNodeSensitivitiesForValue(data, timeAndS.getFirst());
      }
      for (int j = 0; j < sensitivityDD[0].length; j++) {
        double temp = 0.0;
        k = 0;
        for (final DoublesPair timeAndS : sensitivityList) {
          temp += timeAndS.getSecond() / (timeAndS.first * df[k]) * sensitivityDD[k++][j] * (data.getKeys()[j] * data.getValues()[j]);
        }
        result.add(temp);
      }
    } else {
      for (int i = 0; i < interpolatedCurve.size(); i++) {
        result.add(0.);
      }
    }
    return result;
  }

  /**
   * Computes the node sensitivity from an InterestRateCurveSensitivity object and the corresponding yield curve bundle.
   * @param curveSensitivities The sensitivities.
   * @param interpolatedCurves The curve bundle.
   * @return The node sensitivities.
   */
  public DoubleMatrix1D curveToNodeSensitivities(final InterestRateCurveSensitivity curveSensitivities, final YieldCurveBundle interpolatedCurves) {
    return curveToNodeSensitivities(curveSensitivities.getSensitivities(), interpolatedCurves);
  }

  /**
   * Computes the node yield sensitivity for one YieldCurve.
   * @param curveSensitivities The sensitivity to the yield at arbitrary points on the curve.
   * @param curve The YieldCurve.
   * @return The node sensitivity.
   */
  public DoubleMatrix1D curveToNodeSensitivities(final List<DoublesPair> curveSensitivities, final YieldAndDiscountCurve curve) {
    final List<Double> result = new ArrayList<>();
    if (curve instanceof YieldCurve) {
      result.addAll(curveToNodeSensitivity(curveSensitivities, (YieldCurve) curve));
    } else if (curve instanceof DiscountCurve) {
      result.addAll(curveToNodeSensitivity(curveSensitivities, (DiscountCurve) curve));
    } else {
      throw new IllegalArgumentException("Can only handle YieldCurve and DiscountCurve at the moment");
    }
    return new DoubleMatrix1D(result.toArray(new Double[result.size()]));
  }

}
