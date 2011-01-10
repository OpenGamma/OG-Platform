/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.interestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.Validate;

import com.opengamma.math.interpolation.Interpolator1D;
import com.opengamma.math.interpolation.data.Interpolator1DDataBundle;
import com.opengamma.math.interpolation.sensitivity.Interpolator1DNodeSensitivityCalculator;

/**
 * 
 */
public class MultipleYieldCurveFinderDataBundle {

  private final List<InterestRateDerivative> _derivatives;
  private final double[] _marketValues;
  private final YieldCurveBundle _knownCurves;
  private final LinkedHashMap<String, double[]> _unknownCurveNodePoints;
  private final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> _unknownCurveInterpolators;
  private final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> _unknownCurveNodeSensitivityCalculators;
  private final int _totalNodes;
  private final List<String> _names;

  public MultipleYieldCurveFinderDataBundle(final List<InterestRateDerivative> derivatives, final YieldCurveBundle knownCurves, final LinkedHashMap<String, double[]> unknownCurveNodePoints,
      final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> unknownCurveInterpolators,
      final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> unknownCurveNodeSensitivityCalculators) {
    this(derivatives, new double[derivatives == null ? 0 : derivatives.size()], knownCurves, unknownCurveNodePoints, unknownCurveInterpolators, unknownCurveNodeSensitivityCalculators);
  }

  public MultipleYieldCurveFinderDataBundle(final List<InterestRateDerivative> derivatives, final double[] marketValues, final YieldCurveBundle knownCurves,
      final LinkedHashMap<String, double[]> unknownCurveNodePoints, final LinkedHashMap<String, Interpolator1D<? extends Interpolator1DDataBundle>> unknownCurveInterpolators,
      final LinkedHashMap<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> unknownCurveNodeSensitivityCalculators) {
    Validate.notNull(derivatives);
    Validate.noNullElements(derivatives);
    Validate.notNull(marketValues, "market values null");
    Validate.notNull(unknownCurveNodePoints, "unknown curve node points");
    Validate.notNull(unknownCurveInterpolators, "unknown curve interpolators");
    Validate.notNull(unknownCurveNodeSensitivityCalculators, "unknown curve sensitivity calculators");
    Validate.notEmpty(unknownCurveNodePoints, "unknown curve node points");
    Validate.notEmpty(unknownCurveInterpolators, "unknown curve interpolators");
    Validate.notEmpty(unknownCurveNodeSensitivityCalculators, "unknown curve sensitivity calculators");
    Validate.isTrue(derivatives.size() == marketValues.length, "marketValues wrong length");

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
    _marketValues = marketValues;
    if (unknownCurveNodePoints.size() != unknownCurveInterpolators.size()) {
      throw new IllegalArgumentException("Number of unknown curves not the same as curve interpolators");
    }
    if (unknownCurveNodePoints.size() != unknownCurveNodeSensitivityCalculators.size()) {
      throw new IllegalArgumentException("Number of unknown curve not the same as curve sensitivity calculators");
    }
    final Iterator<Entry<String, double[]>> nodePointsIterator = unknownCurveNodePoints.entrySet().iterator();
    final Iterator<Entry<String, Interpolator1D<? extends Interpolator1DDataBundle>>> unknownCurvesIterator = unknownCurveInterpolators.entrySet().iterator();
    final Iterator<Entry<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>>> unknownNodeSensitivityCalculatorIterator = unknownCurveNodeSensitivityCalculators
        .entrySet().iterator();
    _names = new ArrayList<String>();
    while (nodePointsIterator.hasNext()) {
      final Entry<String, double[]> entry1 = nodePointsIterator.next();
      final Entry<String, Interpolator1D<? extends Interpolator1DDataBundle>> entry2 = unknownCurvesIterator.next();
      final Entry<String, Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle>> entry3 = unknownNodeSensitivityCalculatorIterator.next();
      final String name1 = entry1.getKey();
      if (!name1.equals(entry2.getKey()) || !name1.equals(entry3.getKey())) {
        throw new IllegalArgumentException("Names must be the same");
      }
      Validate.notNull(entry1.getValue(), "curve node points for " + name1);
      Validate.notNull(entry2.getValue(), "interpolator for " + name1);
      Validate.notNull(entry3.getValue(), "sensitivity calculator for " + name1);
      _names.add(name1);
    }
    int nNodes = 0;
    for (final double[] nodes : unknownCurveNodePoints.values()) {
      nNodes += nodes.length;
    }
    if (nNodes > derivatives.size()) {
      throw new IllegalArgumentException("Total number of nodes (" + nNodes + ") is greater than the number of instruments (" + derivatives.size() + ")");
    }
    _totalNodes = nNodes;
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

  public int getNumInstruments() {
    return _derivatives.size();
  }

  public int getTotalNodes() {
    return _totalNodes;
  }

  public InterestRateDerivative getDerivative(final int i) {
    return _derivatives.get(i);
  }

  public double getMarketValue(final int i) {
    return _marketValues[i];
  }

  public double[] getCurveNodePointsForCurve(final String name) {
    Validate.notNull(name, "name");
    final double[] result = _unknownCurveNodePoints.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Data for name " + name + " not found");
    }
    return result;
  }

  public Interpolator1D<? extends Interpolator1DDataBundle> getInterpolatorForCurve(final String name) {
    Validate.notNull(name, "name");
    final Interpolator1D<? extends Interpolator1DDataBundle> result = _unknownCurveInterpolators.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Data for name " + name + " not found");
    }
    return result;
  }

  public Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> getSensitivityCalculatorForName(final String name) {
    Validate.notNull(name, "name");
    final Interpolator1DNodeSensitivityCalculator<? extends Interpolator1DDataBundle> result = _unknownCurveNodeSensitivityCalculators.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Data for name " + name + " not found");
    }
    return result;
  }

  public List<String> getCurveNames() {
    return _names;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((_derivatives == null) ? 0 : _derivatives.hashCode());
    result = prime * result + ((_knownCurves == null) ? 0 : _knownCurves.hashCode());
    result = prime * result + Arrays.hashCode(_marketValues);
    result = prime * result + ((_unknownCurveInterpolators == null) ? 0 : _unknownCurveInterpolators.hashCode());
    result = prime * result + ((_unknownCurveNodePoints == null) ? 0 : _unknownCurveNodePoints.hashCode());
    result = prime * result + ((_unknownCurveNodeSensitivityCalculators == null) ? 0 : _unknownCurveNodeSensitivityCalculators.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    MultipleYieldCurveFinderDataBundle other = (MultipleYieldCurveFinderDataBundle) obj;

    if (!Arrays.equals(_marketValues, other._marketValues)) {
      return false;
    }
    if (!ObjectUtils.equals(_derivatives, other._derivatives)) {
      return false;
    }
    if (!ObjectUtils.equals(_knownCurves, other._knownCurves)) {
      return false;
    }
    if (!ObjectUtils.equals(_unknownCurveInterpolators, other._unknownCurveInterpolators)) {
      return false;
    }
    if (!ObjectUtils.equals(_unknownCurveNodePoints, other._unknownCurveNodePoints)) {
      return false;
    }
    if (!ObjectUtils.equals(_unknownCurveNodeSensitivityCalculators, other._unknownCurveNodeSensitivityCalculators)) {
      return false;
    }
    return true;
  }
}
