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
import com.opengamma.math.interpolation.FixedNodeInterpolator1D;
import com.opengamma.math.interpolation.InterpolationResultWithSensitivities;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;
import com.opengamma.math.matrix.DoubleMatrix2D;
import com.opengamma.math.rootfinding.newton.JacobianCalculator;
import com.opengamma.util.tuple.Pair;

/**
 * 
 */
public class MultipleYieldCurveFinderJacobian implements JacobianCalculator {

  private final InterestRateCurveSensitivityCalculator _rateSensitivityCalculator = new InterestRateCurveSensitivityCalculator();
  private final int _nPoints;
  private final Map<String, FixedNodeInterpolator1D> _unknownCurves;
  private YieldCurveBundle _knownCurves;
  private final List<InterestRateDerivative> _derivatives;

  public MultipleYieldCurveFinderJacobian(final List<InterestRateDerivative> derivatives, final LinkedHashMap<String, FixedNodeInterpolator1D> unknownCurves, final YieldCurveBundle knownCurves) {
    Validate.notNull(derivatives);
    Validate.noNullElements(derivatives);
    Validate.notEmpty(unknownCurves, "No curves to solve for");

    if (knownCurves != null) {
      for (final String name : knownCurves.getAllNames()) {
        if (unknownCurves.containsKey(name)) {
          throw new IllegalArgumentException("Curve name in known set matches one to be solved for");
        }
      }
      _knownCurves = knownCurves;
    }
    _nPoints = derivatives.size();
    _derivatives = derivatives;

    int nNodes = 0;
    for (final FixedNodeInterpolator1D nodes : unknownCurves.values()) {
      nNodes += nodes.getNumberOfNodes();
    }
    if (nNodes != _nPoints) {
      throw new IllegalArgumentException("Total number of nodes does not match number of instruments");
    }
    _unknownCurves = unknownCurves;
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
    final Set<Entry<String, FixedNodeInterpolator1D>> entrySet = _unknownCurves.entrySet();
    Iterator<Entry<String, FixedNodeInterpolator1D>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      final Entry<String, FixedNodeInterpolator1D> temp = iterator.next();
      final FixedNodeInterpolator1D fixedNodeInterpolator = temp.getValue();
      final double[] yields = Arrays.copyOfRange(x.getData(), index, index + fixedNodeInterpolator.getNumberOfNodes());
      index += fixedNodeInterpolator.getNumberOfNodes();
      final InterpolatedYieldAndDiscountCurve curve = new InterpolatedYieldCurve(fixedNodeInterpolator.getNodePositions(), yields,
          (Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities>) fixedNodeInterpolator.getUnderlyingInterpolator());
      curves.setCurve(temp.getKey(), curve);
    }

    // set any known (i.e. fixed) curves
    if (_knownCurves != null) {
      curves.addAll(_knownCurves);
    }

    final double[][] res = new double[_nPoints][_nPoints];

    for (int i = 0; i < _nPoints; i++) { // loop over all instruments
      final Map<String, List<Pair<Double, Double>>> senseMap = _rateSensitivityCalculator.getSensitivity(_derivatives.get(i), curves);

      iterator = entrySet.iterator();
      int offset = 0;
      while (iterator.hasNext()) { // loop over all curves (by name)
        final Entry<String, FixedNodeInterpolator1D> namedCurve = iterator.next();
        final String name = namedCurve.getKey();
        if (senseMap.containsKey(name)) {

          final InterpolatedYieldAndDiscountCurve curve = (InterpolatedYieldAndDiscountCurve) curves.getCurve(name);
          final Interpolator1DDataBundle data = curve.getDataBundles().values().iterator().next();
          // Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities> interpolator = (Interpolator1D<? extends Interpolator1DDataBundle, ? extends
          // InterpolationResultWithSensitivities>)
          final Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities> interpolator =
            (Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities>) curve.getInterpolators().values().iterator().next();
          final List<Pair<Double, Double>> senseList = senseMap.get(name);
          final double[][] sensitivity = new double[senseList.size()][];
          int k = 0;
          for (final Pair<Double, Double> timeAndDF : senseList) {
            sensitivity[k++] = interpolator.interpolate(data, timeAndDF.getFirst()).getSensitivities();
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
        offset += namedCurve.getValue().getNumberOfNodes();
      }
    }

    return new DoubleMatrix2D(res);
  }

}
