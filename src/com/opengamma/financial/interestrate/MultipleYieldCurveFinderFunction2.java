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
import com.opengamma.math.interpolation.FixedNodeInterpolator1D;
import com.opengamma.math.interpolation.InterpolationResult;
import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.Interpolator1DDataBundle;
import com.opengamma.math.matrix.DoubleMatrix1D;

/**
 * 
 */
public class MultipleYieldCurveFinderFunction2 extends Function1D<DoubleMatrix1D, DoubleMatrix1D> {
  private final int _nPoints;
  private final LinkedHashMap<String, FixedNodeInterpolator1D> _unknownCurves;
  private YieldCurveBundle _knownCurves;
  private final List<InterestRateDerivative> _derivatives;
  private final PresentValueCalculator _pvc = new PresentValueCalculator();

  public MultipleYieldCurveFinderFunction2(final List<InterestRateDerivative> derivatives, LinkedHashMap<String, FixedNodeInterpolator1D> unknownCurves, YieldCurveBundle knownCurves) {
    Validate.notNull(derivatives);
    Validate.noNullElements(derivatives);

    Validate.notEmpty(unknownCurves, "No curves to solve for");

    _nPoints = derivatives.size();

    if (knownCurves != null) {
      for (String name : knownCurves.getAllNames()) {
        if (unknownCurves.containsKey(name)) {
          throw new IllegalArgumentException("Curve name in known set matches one to be solved for");
        }
      }
      _knownCurves = knownCurves;
    }
    _derivatives = derivatives;

    int nNodes = 0;
    for (FixedNodeInterpolator1D nodes : unknownCurves.values()) {
      nNodes += nodes.getNumberOfNodes();
    }
    if (nNodes != _nPoints) {
      throw new IllegalArgumentException("Total number of nodes does not match number of instruments");
    }
    _unknownCurves = unknownCurves;
  }

  @Override
  public DoubleMatrix1D evaluate(DoubleMatrix1D x) {
    Validate.notNull(x);

    if (x.getNumberOfElements() != _nPoints) {
      throw new IllegalArgumentException("vector is wrong length");
    }

    YieldCurveBundle curves = new YieldCurveBundle();
    int index = 0;
    Iterator<Entry<String, FixedNodeInterpolator1D>> interator = _unknownCurves.entrySet().iterator();
    while (interator.hasNext()) {
      Entry<String, FixedNodeInterpolator1D> temp = interator.next();
      FixedNodeInterpolator1D fixedNodeInterpolator = temp.getValue();
      double[] yields = Arrays.copyOfRange(x.getData(), index, index + fixedNodeInterpolator.getNumberOfNodes());
      index += fixedNodeInterpolator.getNumberOfNodes();
      InterpolatedYieldCurve curve = new InterpolatedYieldCurve(fixedNodeInterpolator.getNodePositions(), yields,
          (Interpolator1D<? extends Interpolator1DDataBundle, ? extends InterpolationResult>) fixedNodeInterpolator.getUnderlyingInterpolator());
      curves.setCurve(temp.getKey(), curve);
    }

    // set any known (i.e. fixed) curves
    if (_knownCurves != null) {
      curves.addAll(_knownCurves);
    }

    double[] res = new double[_nPoints];
    for (int i = 0; i < _nPoints; i++) {
      res[i] = _pvc.getPresentValue(_derivatives.get(i), curves);
    }

    return new DoubleMatrix1D(res);
  }
}
