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
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.apache.commons.lang.Validate;

import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldAndDiscountCurve;
import com.opengamma.financial.model.interestrate.curve.InterpolatedYieldCurve;
import com.opengamma.math.function.Function1D;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class MultipleYieldCurveFinderJacobian implements JacobianCalculator {
  private final InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> _calculator;
  private final int _nPoints;
  private final Map<String, Interpolator1D> _unknownCurveInterpolators;
  private final Map<String, double[]> _unknownCurveNodePoints;
  private final Map<String, Interpolator1DNodeSensitivityCalculator> _unknownCurveSensitivityCalculators;
  private YieldCurveBundle _knownCurves;
  private final List<InterestRateDerivative> _derivatives;

  public MultipleYieldCurveFinderJacobian(final List<InterestRateDerivative> derivatives, final LinkedHashMap<String, double[]> unknownCurveNodePoints,
      final LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators, final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator> unknownCurveSensitivityCalculators,
      final YieldCurveBundle knownCurves, final InterestRateDerivativeVisitor<Map<String, List<Pair<Double, Double>>>> calculator) {
    Validate.notNull(derivatives);
    Validate.noNullElements(derivatives);
    Validate.notNull(calculator);
    Validate.notEmpty(unknownCurveInterpolators, "No curves to solve for");

    if (knownCurves != null) {
      for (final String name : knownCurves.getAllNames()) {
        if (unknownCurveInterpolators.containsKey(name)) {
          throw new IllegalArgumentException("Curve name in known set matches one to be solved for");
        }
      }
      _knownCurves = knownCurves;
    }
    _calculator = calculator;
    _nPoints = derivatives.size();
    _derivatives = derivatives;

    //TODO this logic should not be in here
    if (unknownCurveNodePoints.size() != unknownCurveInterpolators.size()) {
      throw new IllegalArgumentException("Number of unknown curves not the same as curve interpolators");
    }
    if (unknownCurveNodePoints.size() != unknownCurveSensitivityCalculators.size()) {
      throw new IllegalArgumentException("Number of unknown curve not the same as curve sensitivity calculators");
    }
    final Iterator<String> nodePointsIterator = unknownCurveNodePoints.keySet().iterator();
    final Iterator<String> unknownCurvesIterator = unknownCurveInterpolators.keySet().iterator();
    final Iterator<String> unknownNodeSensitivityCalculatorIterator = unknownCurveSensitivityCalculators.keySet().iterator();
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
      throw new IllegalArgumentException("Total number of nodes does not match number of instruments");
    }
    _unknownCurveInterpolators = unknownCurveInterpolators;
    _unknownCurveNodePoints = unknownCurveNodePoints;
    _unknownCurveSensitivityCalculators = unknownCurveSensitivityCalculators;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DoubleMatrix2D evaluate(final DoubleMatrix1D x, final Function1D<DoubleMatrix1D, DoubleMatrix1D>... functions) {
    Validate.notNull(x);

    if (x.getNumberOfElements() != _nPoints) {
      throw new IllegalArgumentException("vector is wrong length");
    }

    final YieldCurveBundle curves = new YieldCurveBundle();
    int index = 0;
    final Set<Entry<String, Interpolator1D>> entrySet = _unknownCurveInterpolators.entrySet();
    Iterator<Entry<String, Interpolator1D>> iterator = entrySet.iterator();
    int numberOfNodes;
    double[] unknownCurveNodePoints;
    while (iterator.hasNext()) {
      final Entry<String, Interpolator1D> temp = iterator.next();
      final Interpolator1D interpolator = temp.getValue();
      unknownCurveNodePoints = _unknownCurveNodePoints.get(temp.getKey());
      numberOfNodes = unknownCurveNodePoints.length;
      final double[] yields = Arrays.copyOfRange(x.getData(), index, index + numberOfNodes);
      index += numberOfNodes;
      final InterpolatedYieldAndDiscountCurve curve = new InterpolatedYieldCurve(unknownCurveNodePoints, yields, interpolator);
      curves.setCurve(temp.getKey(), curve);
    }

    // set any known (i.e. fixed) curves
    if (_knownCurves != null) {
      curves.addAll(_knownCurves);
    }

    final double[][] res = new double[_nPoints][_nPoints];

    for (int i = 0; i < _nPoints; i++) { // loop over all instruments
      final Map<String, List<Pair<Double, Double>>> senseMap = _calculator.getValue(_derivatives.get(i), curves);

      iterator = entrySet.iterator();
      int offset = 0;
      while (iterator.hasNext()) { // loop over all curves (by name)
        final Entry<String, Interpolator1D> namedCurve = iterator.next();
        final String name = namedCurve.getKey();
        if (senseMap.containsKey(name)) {

          final InterpolatedYieldAndDiscountCurve curve = (InterpolatedYieldAndDiscountCurve) curves.getCurve(name);
          final Interpolator1DDataBundle data = curve.getDataBundles().values().iterator().next();
          final Interpolator1DNodeSensitivityCalculator sensitivityCalculator = _unknownCurveSensitivityCalculators.get(name);
          //final Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities> interpolator = (Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities>) curve
          //    .getInterpolators().values().iterator().next();
          final List<Pair<Double, Double>> senseList = senseMap.get(name);
          final double[][] sensitivity = new double[senseList.size()][];
          int k = 0;
          for (final Pair<Double, Double> timeAndDF : senseList) {
            //            sensitivity[k++] = interpolator.interpolate(data, timeAndDF.getFirst()).getSensitivities();
            sensitivity[k++] = sensitivityCalculator.calculate(data, timeAndDF.getFirst());
          }
          for (int j = 0; j < sensitivity[0].length; j++) {
            double temp = 0.0;
            k = 0;
            for (final Pair<Double, Double> timeAndDF : senseList) {
              temp += timeAndDF.getSecond() * sensitivity[k++][j];
            }
            res[i][j + offset] = temp;
          }
        }
        //        offset += namedCurve.getValue().getNumberOfNodes();
        offset += _unknownCurveNodePoints.get(namedCurve.getKey()).length;
      }
    }

    return new DoubleMatrix2D(res);
  }

}
