/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;

import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;

/**
 * 
 */
public class MultipleYieldCurveFinderDataBundle {
  private final List<InterestRateDerivative> _derivatives;
  private final YieldCurveBundle _knownCurves;
  private final LinkedHashMap<String, double[]> _unknownCurveNodePoints;
  private final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> _unknownCurveInterpolators;
  private final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> _unknownCurveNodeSensitivityCalculators;
  private final int _totalNodes;
  private final List<String> _names;

  public MultipleYieldCurveFinderDataBundle(final List<InterestRateDerivative> derivatives, final YieldCurveBundle knownCurves, final LinkedHashMap<String, double[]> unknownCurveNodePoints,
      final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> unknownCurveInterpolators,
      final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> unknownCurveNodeSensitivityCalculators) {
    Validate.notNull(derivatives);
    Validate.noNullElements(derivatives);
    Validate.notNull(unknownCurveNodePoints, "unknown curve node points");
    Validate.notNull(unknownCurveInterpolators, "unknown curve interpolators");
    Validate.notNull(unknownCurveNodeSensitivityCalculators, "unknown curve sensitivity calculators");
    Validate.notEmpty(unknownCurveNodePoints, "unknown curve node points");
    Validate.notEmpty(unknownCurveInterpolators, "unknown curve interpolators");
    Validate.notEmpty(unknownCurveNodeSensitivityCalculators, "unknown curve sensitivity calculators");
    _totalNodes = derivatives.size();
    if (knownCurves != null) {
      for (final String name : knownCurves.getAllNames()) {
        if (unknownCurveInterpolators.containsKey(name)) {
          throw new IllegalArgumentException("Curve name in known set matches one to be solved for");
        }
      }
      _knownCurves = knownCurves;
    } else {
      _knownCurves = null;
    }
    _derivatives = derivatives;
    if (unknownCurveNodePoints.size() != unknownCurveInterpolators.size()) {
      throw new IllegalArgumentException("Number of unknown curves not the same as curve interpolators");
    }
    if (unknownCurveNodePoints.size() != unknownCurveNodeSensitivityCalculators.size()) {
      throw new IllegalArgumentException("Number of unknown curve not the same as curve sensitivity calculators");
    }
    final Iterator<String> nodePointsIterator = unknownCurveNodePoints.keySet().iterator();
    final Iterator<String> unknownCurvesIterator = unknownCurveInterpolators.keySet().iterator();
    final Iterator<String> unknownNodeSensitivityCalculatorIterator = unknownCurveNodeSensitivityCalculators.keySet().iterator();
    _names = new ArrayList<String>();
    while (nodePointsIterator.hasNext()) {
      final String name1 = nodePointsIterator.next();
      final String name2 = unknownCurvesIterator.next();
      final String name3 = unknownNodeSensitivityCalculatorIterator.next();
      if (name1 != name2 || name1 != name3) {
        throw new IllegalArgumentException("Names must be the same");
      }
      _names.add(name1);
    }
    int nNodes = 0;
    for (final double[] nodes : unknownCurveNodePoints.values()) {
      nNodes += nodes.length;
    }
    if (nNodes != _totalNodes) {
      throw new IllegalArgumentException("Total number of nodes does not match number of instruments: " + nNodes + ", " + _totalNodes);
    }
    _unknownCurveNodePoints = unknownCurveNodePoints;
    _unknownCurveInterpolators = unknownCurveInterpolators;
    _unknownCurveNodeSensitivityCalculators = unknownCurveNodeSensitivityCalculators;
  }

  public List<InterestRateDerivative> getDerivatives() {
    return _derivatives;
  }

  public YieldCurveBundle getKnownCurves() {
    return _knownCurves;
  }

  public LinkedHashMap<String, double[]> getUnknownCurveNodePoints() {
    return _unknownCurveNodePoints;
  }

  public LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> getUnknownCurveInterpolators() {
    return _unknownCurveInterpolators;
  }

  public LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> getUnknownCurveNodeSensitivityCalculators() {
    return _unknownCurveNodeSensitivityCalculators;
  }

  public int getTotalNodes() {
    return _totalNodes;
  }

  public InterestRateDerivative getDerivative(final int i) {
    return _derivatives.get(i);
  }

  public double[] getCurveNodePointsForCurve(final String name) {
    Validate.notNull(name, "name");
    return _unknownCurveNodePoints.get(name);
  }

  public Interpolator1D<? extends Interpolator1DDataBundle> getInterpolatorForCurve(final String name) {
    Validate.notNull(name, "name");
    return _unknownCurveInterpolators.get(name);
  }

  public Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> getSensitivityCalculatorForName(final String name) {
    Validate.notNull(name, "name");
    return _unknownCurveNodeSensitivityCalculators.get(name);
  }

  public List<String> getCurveNames() {
    return _names;
  }
}
