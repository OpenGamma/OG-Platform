/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
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

/**
 * 
 */
public class MultipleYieldCurveFinderDataBundle {
  private final List<InstrumentDerivative> _derivatives;
  private final double[] _marketValues;
  private final YieldCurveBundle _knownCurves;
  private final LinkedHashMap<String, double[]> _unknownCurveNodePoints;
  private final LinkedHashMap<String, Interpolator1D> _unknownCurveInterpolators;
  private final int _totalNodes;
  private final List<String> _names;
  private final boolean _useFiniteDifferenceByDefault;

//  public MultipleYieldCurveFinderDataBundle(final List<InterestRateDerivative> derivatives, final double[] marketValues, final YieldCurveBundle knownCurves,
//      final LinkedHashMap<String, double[]> unknownCurveNodePoints, final LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators) {
//    this(derivatives, marketValues, knownCurves, unknownCurveNodePoints, unknownCurveInterpolators, false);
//  }
  
  public MultipleYieldCurveFinderDataBundle(final List<InstrumentDerivative> derivatives, final double[] marketValues, final YieldCurveBundle knownCurves,
      final LinkedHashMap<String, double[]> unknownCurveNodePoints, final LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators, boolean useFiniteDifferenceByDefault) {
    Validate.notNull(derivatives);
    Validate.noNullElements(derivatives);
    Validate.notNull(marketValues, "market values null");
    Validate.notNull(unknownCurveNodePoints, "unknown curve node points");
    Validate.notNull(unknownCurveInterpolators, "unknown curve interpolators");
    Validate.notEmpty(unknownCurveNodePoints, "unknown curve node points");
    Validate.notEmpty(unknownCurveInterpolators, "unknown curve interpolators");
    Validate.isTrue(derivatives.size() == marketValues.length, "marketValues wrong length; must be one par rate per derivative (have " + marketValues.length + " values for " 
        + derivatives.size() + " derivatives)");

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
    final Iterator<Entry<String, double[]>> nodePointsIterator = unknownCurveNodePoints.entrySet().iterator();
    final Iterator<Entry<String, Interpolator1D>> unknownCurvesIterator = unknownCurveInterpolators.entrySet().iterator();
    _names = new ArrayList<String>();
    while (nodePointsIterator.hasNext()) {
      final Entry<String, double[]> entry1 = nodePointsIterator.next();
      final Entry<String, Interpolator1D> entry2 = unknownCurvesIterator.next();
      final String name1 = entry1.getKey();
      if (!name1.equals(entry2.getKey())) {
        throw new IllegalArgumentException("Names must be the same");
      }
      Validate.notNull(entry1.getValue(), "curve node points for " + name1);
      Validate.notNull(entry2.getValue(), "interpolator for " + name1);
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
    _useFiniteDifferenceByDefault = useFiniteDifferenceByDefault;
  }

  public List<InstrumentDerivative> getDerivatives() {
    return _derivatives;
  }

  public YieldCurveBundle getKnownCurves() {
    return _knownCurves;
  }

  public LinkedHashMap<String, double[]> getUnknownCurveNodePoints() {
    return _unknownCurveNodePoints;
  }

  public LinkedHashMap<String, Interpolator1D> getUnknownCurveInterpolators() {
    return _unknownCurveInterpolators;
  }

  public int getNumInstruments() {
    return _derivatives.size();
  }

  public int getTotalNodes() {
    return _totalNodes;
  }

  public InstrumentDerivative getDerivative(final int i) {
    return _derivatives.get(i);
  }

  public double getMarketValue(final int i) {
    return _marketValues[i];
  }

  public boolean useFiniteDifferenceForNodeSensitivities() {
    return _useFiniteDifferenceByDefault;
  }
  
  public double[] getCurveNodePointsForCurve(final String name) {
    Validate.notNull(name, "name");
    final double[] result = _unknownCurveNodePoints.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Data for name " + name + " not found");
    }
    return result;
  }

  public Interpolator1D getInterpolatorForCurve(final String name) {
    Validate.notNull(name, "name");
    final Interpolator1D result = _unknownCurveInterpolators.get(name);
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
    result = prime * result + (_useFiniteDifferenceByDefault ? 1231 : 1237);
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
    if (_useFiniteDifferenceByDefault != other._useFiniteDifferenceByDefault) {
      return false;
    }
    return true;
  }
}
