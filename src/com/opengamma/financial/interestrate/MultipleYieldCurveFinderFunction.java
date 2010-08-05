/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class MultipleYieldCurveFinderFunction extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {

  private final int _nPoints;
  private final LinkedHashMap<String, double[]> _unknownCurveNodePoints;
  private final LinkedHashMap<String, Interpolator1D> _unknownCurveInterpolators;
  private final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator> _unknownCurveNodeSensitivityCalculators;
  private YieldCurveBundle _knownCurves;
  private final List<InterestRateDerivative> _derivatives;
  private final InterestRateDerivativeVisitor<Double> _calculator;

  public MultipleYieldCurveFinderFunction(final List<InterestRateDerivative> derivatives, final LinkedHashMap<String, double[]> unknownCurveNodePoints,
      final LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators, final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator> unknownCurveNodeSensitivityCalculators,
      final YieldCurveBundle knownCurves, final InterestRateDerivativeVisitor<Double> calculator) {
    Validate.notNull(derivatives);
    Validate.noNullElements(derivatives);
    Validate.notNull(calculator);
    Validate.notNull(unknownCurveNodePoints);
    Validate.notNull(unknownCurveInterpolators);
    Validate.notNull(unknownCurveNodeSensitivityCalculators);
    Validate.notEmpty(unknownCurveInterpolators, "No curves to solve for");

    _nPoints = derivatives.size();

    if (knownCurves != null) {
      for (final String name : knownCurves.getAllNames()) {
        if (unknownCurveInterpolators.containsKey(name)) {
          throw new IllegalArgumentException("Curve name in known set matches one to be solved for");
        }
      }
      _knownCurves = knownCurves;
    }

    _derivatives = derivatives;

    //TODO this logic should not be in here
    if (unknownCurveNodePoints.size() != unknownCurveInterpolators.size()) {
      throw new IllegalArgumentException("Number of unknown curves not the same as curve interpolators");
    }
    if (unknownCurveNodePoints.size() != unknownCurveNodeSensitivityCalculators.size()) {
      throw new IllegalArgumentException("Number of unknown curve not the same as curve sensitivity calculators");
    }
    final Iterator<String> nodePointsIterator = unknownCurveNodePoints.keySet().iterator();
    final Iterator<String> unknownCurvesIterator = unknownCurveInterpolators.keySet().iterator();
    final Iterator<String> unknownNodeSensitivityCalculatorIterator = unknownCurveNodeSensitivityCalculators.keySet().iterator();
    while (nodePointsIterator.hasNext()) {
      final String name1 = nodePointsIterator.next();
      final String name2 = unknownCurvesIterator.next();
      final String name3 = unknownNodeSensitivityCalculatorIterator.next();
      if (name1 != name2 || name1 != name3) {
        throw new IllegalArgumentException("Names must be the same");
      }
    }
    ////////////////////////////////////////////////////////////

    int nNodes = 0;
    for (final double[] nodes : unknownCurveNodePoints.values()) {
      nNodes += nodes.length;
    }
    if (nNodes != _nPoints) {
      throw new IllegalArgumentException("Total number of nodes does not match number of instruments: " + nNodes + ", " + _nPoints);
    }
    _unknownCurveNodePoints = unknownCurveNodePoints;
    _unknownCurveInterpolators = unknownCurveInterpolators;
    _calculator = calculator;
    _unknownCurveNodeSensitivityCalculators = unknownCurveNodeSensitivityCalculators;
  }

  @Override
  public DoubleMatrix1D evaluate(final DoubleMatrix1D x) {
    Validate.notNull(x);

    if (x.getNumberOfElements() != _nPoints) {
      throw new IllegalArgumentException("vector is wrong length");
    }

    final YieldCurveBundle curves = new YieldCurveBundle();
    int index = 0;
    final Iterator<Entry<String, Interpolator1D>> interator = _unknownCurveInterpolators.entrySet().iterator();
    while (interator.hasNext()) {
      final Entry<String, Interpolator1D> temp = interator.next();
      final Interpolator1D interpolator = temp.getValue();
      final double[] nodes = _unknownCurveNodePoints.get(temp.getKey());
      final double[] yields = Arrays.copyOfRange(x.getData(), index, index + nodes.length);
      index += nodes.length;
      final InterpolatedYieldCurve curve = new InterpolatedYieldCurve(nodes, yields, interpolator);
      curves.setCurve(temp.getKey(), curve);
    }

    // set any known (i.e. fixed) curves
    if (_knownCurves != null) {
      curves.addAll(_knownCurves);
    }

    final double[] res = new double[_nPoints];
    for (int i = 0; i < _nPoints; i++) {
      res[i] = _calculator.getValue(_derivatives.get(i), curves);
    }

    return new DoubleMatrix1D(res);
  }
}
