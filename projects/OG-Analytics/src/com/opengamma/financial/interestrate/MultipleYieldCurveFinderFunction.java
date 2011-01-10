/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.YieldCurve;
import com.opengamma.math.curve.InterpolatedDoublesCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class MultipleYieldCurveFinderFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {
  private final InterestRateDerivativeVisitor<YieldCurveBundle, Double> _calculator;
  private final MultipleYieldCurveFinderDataBundle _data;

  public MultipleYieldCurveFinderFunction(final MultipleYieldCurveFinderDataBundle data, final InterestRateDerivativeVisitor<YieldCurveBundle, Double> calculator) {
    Validate.notNull(data, "data");
    Validate.notNull(calculator, "calculator");
    _calculator = calculator;
    _data = data;
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
    Validate.notNull(x);
    final int totalNodes = _data.getTotalNodes();

    if (x.getNumberOfElements() != totalNodes) {
      throw new IllegalArgumentException("vector is wrong length");
    }

    final YieldCurveBundle curves = new YieldCurveBundle();
    int index = 0;
    final List<String> curveNames = _data.getCurveNames();
    for (final String name : curveNames) {
      final Interpolator1D<? extends Interpolator1DDataBundle> interpolator = _data.getInterpolatorForCurve(name);
      final double[] nodes = _data.getCurveNodePointsForCurve(name);
      final double[] yields = Arrays.copyOfRange(x.getData(), index, index + nodes.length);
      index += nodes.length;
      final YieldCurve curve = new YieldCurve(InterpolatedDoublesCurve.from(nodes, yields, interpolator));
      curves.setCurve(name, curve);
    }

    // set any known (i.e. fixed) curves
    final YieldCurveBundle knownCurves = _data.getKnownCurves();
    if (_data.getKnownCurves() != null) {
      curves.addAll(knownCurves);
    }

    final double[] res = new double[_data.getNumInstruments()];
    for (int i = 0; i < _data.getNumInstruments(); i++) {
      res[i] = _calculator.visit(_data.getDerivative(i), curves) - _data.getMarketValue(i);
    }

    return new DoubleMatrix1D(res);
  }
}
