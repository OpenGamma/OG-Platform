/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.Validate;

import com.opengamma.analytics.financial.model.interestrate.curve.YieldAndDiscountCurve;
import com.opengamma.analytics.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.analytics.math.curve.Curve;
import com.opengamma.analytics.math.curve.InterpolatedDoublesCurve;
import com.opengamma.analytics.math.function.Function1D;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.analytics.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.analytics.math.matrix.DoubleMatrix1D;
import com.opengamma.analytics.math.matrix.DoubleMatrix2D;
import com.opengamma.util.tuple.DoublesPair;

/**
 * Jacobian matrix builder for multi-curves construction when the calculator returns an InterestRateCurveSensitivity object.
 */
public class MultipleYieldCurveFinderIRSJacobian extends Function1D<DoubleMatrix1D, DoubleMatrix2D> {

  /**
   * The sensitivity calculator.
   */
  private final InstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> _calculator;
  /**
   * The multi-curves data.
   */
  private final MultipleYieldCurveFinderDataBundle _data;
  private final YieldCurveBundleBuildingFunction _curveBuilderFunction;

  public MultipleYieldCurveFinderIRSJacobian(final MultipleYieldCurveFinderDataBundle data, final InstrumentDerivativeVisitor<YieldCurveBundle, InterestRateCurveSensitivity> calculator) {
    Validate.notNull(data, "data");
    Validate.notNull(calculator, "calculator");
    _data = data;
    _calculator = calculator;
    _curveBuilderFunction = new InterpolatedYieldCurveBuildingFunction(data.getUnknownCurveNodePoints(), data.getUnknownCurveInterpolators());
  }

  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x) {

    YieldCurveBundle curves = _curveBuilderFunction.evaluate(x);

    final YieldCurveBundle knownCurves = _data.getKnownCurves();
    // set any known (i.e. fixed) curves
    if (knownCurves != null) {
      curves.addAll(knownCurves);
    }

    final int totalNodes = _data.getTotalNodes();
    final List<String> curveNames = _data.getCurveNames();

    final double[][] res = new double[_data.getNumInstruments()][totalNodes];
    for (int i = 0; i < _data.getNumInstruments(); i++) { // loop over all instruments
      InstrumentDerivative deriv = _data.getDerivative(i);
      final Map<String, List<DoublesPair>> senseMap = _calculator.visit(deriv, curves).getSensitivities();
      int offset = 0;
      for (final String name : curveNames) { // loop over all curves (by name)
        if (senseMap.containsKey(name)) {
          final YieldAndDiscountCurve ydCurve = curves.getCurve(name);
          if (!(ydCurve instanceof YieldCurve)) {
            throw new IllegalArgumentException("Can only handle YieldCurve");
          }
          final Curve<Double, Double> curve = ((YieldCurve) ydCurve).getCurve();
          if (!(curve instanceof InterpolatedDoublesCurve)) {
            throw new IllegalArgumentException("Can only handle InterpolatedDoublesCurve");
          }
          final InterpolatedDoublesCurve interpolatedCurve = (InterpolatedDoublesCurve) curve;
          final Interpolator1DDataBundle data = interpolatedCurve.getDataBundle();
          final Interpolator1D sensitivityCalculator = _data.getInterpolatorForCurve(name);
          final List<DoublesPair> senseList = senseMap.get(name);
          if (senseList.size() != 0) {
            final double[][] sensitivity = new double[senseList.size()][];
            int k = 0;
            for (final DoublesPair timeAndDF : senseList) {
              sensitivity[k++] = sensitivityCalculator.getNodeSensitivitiesForValue(data, timeAndDF.getFirst(), _data.useFiniteDifferenceForNodeSensitivities());
            }
            for (int j = 0; j < sensitivity[0].length; j++) {
              double temp = 0.0;
              k = 0;
              for (final DoublesPair timeAndDF : senseList) {
                temp += timeAndDF.getSecond() * sensitivity[k++][j];
              }
              res[i][j + offset] = temp;
            }
          }
        }
        offset += _data.getCurveNodePointsForCurve(name).length;
      }
    }
    return new DoubleMatrix2D(res);
  }
}
