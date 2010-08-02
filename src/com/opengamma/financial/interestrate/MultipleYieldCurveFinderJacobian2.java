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
public class MultipleYieldCurveFinderJacobian2 implements JacobianCalculator {

  private final PresentValueSensitivityCalculator _pvsCalculator = new PresentValueSensitivityCalculator();
  private final int _nPoints;
  private final Map<String, FixedNodeInterpolator1D> _unknownCurves;
  // private final Set<String> _unknownCurveNames;
  private YieldCurveBundle _knownCurves;
  private final List<InterestRateDerivative> _derivatives;

  public MultipleYieldCurveFinderJacobian2(final List<InterestRateDerivative> derivatives, LinkedHashMap<String, FixedNodeInterpolator1D> unknownCurves, YieldCurveBundle knownCurves) {
    Validate.notNull(derivatives);
    Validate.noNullElements(derivatives);
    Validate.notEmpty(unknownCurves, "No curves to solve for");

    if (knownCurves != null) {
      for (String name : knownCurves.getAllNames()) {
        if (unknownCurves.containsKey(name)) {
          throw new IllegalArgumentException("Curve name in known set matches one to be solved for");
        }
      }
      _knownCurves = knownCurves;
    }
    _nPoints = derivatives.size();
    _derivatives = derivatives;
    // _unknownCurveNames = unknownCurves.keySet();

    int nNodes = 0;
    for (FixedNodeInterpolator1D nodes : unknownCurves.values()) {
      nNodes += nodes.getNumberOfNodes();
    }
    if (nNodes != _nPoints) {
      throw new IllegalArgumentException("Total number of nodes does not match number of instruments");
    }
    _unknownCurves = unknownCurves;
  }

  @SuppressWarnings("unchecked")
  @Override
  public DoubleMatrix2D evaluate(DoubleMatrix1D x, Function1D<DoubleMatrix1D, DoubleMatrix1D>... functions) {
    Validate.notNull(x);

    if (x.getNumberOfElements() != _nPoints) {
      throw new IllegalArgumentException("vector is wrong length");
    }

    YieldCurveBundle curves = new YieldCurveBundle();
    int index = 0;
    Set<Entry<String, FixedNodeInterpolator1D>> entrySet = _unknownCurves.entrySet();
    Iterator<Entry<String, FixedNodeInterpolator1D>> iterator = entrySet.iterator();
    while (iterator.hasNext()) {
      Entry<String, FixedNodeInterpolator1D> temp = iterator.next();
      FixedNodeInterpolator1D fixedNodeInterpolator = temp.getValue();
      double[] yields = Arrays.copyOfRange(x.getData(), index, index + fixedNodeInterpolator.getNumberOfNodes());
      index += fixedNodeInterpolator.getNumberOfNodes();
      InterpolatedYieldAndDiscountCurve curve = new InterpolatedYieldCurve(fixedNodeInterpolator.getNodePositions(), yields,
          (Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities>) fixedNodeInterpolator.getUnderlyingInterpolator());
      curves.setCurve(temp.getKey(), curve);
    }

    // set any known (i.e. fixed) curves
    if (_knownCurves != null) {
      curves.addAll(_knownCurves);
    }

    double[][] res = new double[_nPoints][_nPoints];

    for (int i = 0; i < _nPoints; i++) { // loop over all instruments
      Map<String, List<Pair<Double, Double>>> senseMap = _pvsCalculator.getSensitivity(_derivatives.get(i), curves);

      iterator = entrySet.iterator();
      int offset = 0;
      while (iterator.hasNext()) { // loop over all curves (by name)
        Entry<String, FixedNodeInterpolator1D> namedCurve = iterator.next();
        String name = namedCurve.getKey();
        if (senseMap.containsKey(name)) {

          InterpolatedYieldAndDiscountCurve curve = (InterpolatedYieldAndDiscountCurve) curves.getCurve(name);
          Interpolator1DDataBundle data = curve.getDataBundles().values().iterator().next();
          // Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities> interpolator = (Interpolator1D<? extends Interpolator1DDataBundle, ? extends
          // InterpolationResultWithSensitivities>)
          Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities> interpolator = 
            (Interpolator1D<Interpolator1DDataBundle, ? extends InterpolationResultWithSensitivities>) curve.getInterpolators().values().iterator().next();
          List<Pair<Double, Double>> senseList = senseMap.get(name);
          double[][] sensitivity = new double[senseList.size()][];
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
