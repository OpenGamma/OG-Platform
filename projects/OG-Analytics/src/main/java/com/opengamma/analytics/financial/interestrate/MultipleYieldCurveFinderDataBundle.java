/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.interestrate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.math.interpolation.Interpolator1D;
import com.opengamma.util.ArgumentChecker;

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
  private final FXMatrix _fxMatrix;

  public MultipleYieldCurveFinderDataBundle(final List<InstrumentDerivative> derivatives, final double[] marketValues, final YieldCurveBundle knownCurves,
      final LinkedHashMap<String, double[]> unknownCurveNodePoints, final LinkedHashMap<String, Interpolator1D> unknownCurveInterpolators, final boolean useFiniteDifferenceByDefault,
      final FXMatrix fxMatrix) {
    ArgumentChecker.notNull(derivatives, "derivatives");
    ArgumentChecker.noNulls(derivatives, "derivatives");
    ArgumentChecker.notNull(marketValues, "market values null");
    ArgumentChecker.notNull(unknownCurveNodePoints, "unknown curve node points");
    ArgumentChecker.notNull(unknownCurveInterpolators, "unknown curve interpolators");
    ArgumentChecker.notEmpty(unknownCurveNodePoints, "unknown curve node points");
    ArgumentChecker.notEmpty(unknownCurveInterpolators, "unknown curve interpolators");
    ArgumentChecker.isTrue(derivatives.size() == marketValues.length, "marketValues wrong length; must be one par rate per derivative (have {} values for {} derivatives",
        marketValues.length, derivatives.size());
    ArgumentChecker.notNull(fxMatrix, "FX matrix");
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
      ArgumentChecker.notNull(entry1.getValue(), "curve node points for " + name1);
      ArgumentChecker.notNull(entry2.getValue(), "interpolator for " + name1);
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
    _fxMatrix = fxMatrix;
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

  public FXMatrix getFXMatrix() {
    return _fxMatrix;
  }

  public double[] getCurveNodePointsForCurve(final String name) {
    ArgumentChecker.notNull(name, "name");
    final double[] result = _unknownCurveNodePoints.get(name);
    if (result == null) {
      throw new IllegalArgumentException("Data for name " + name + " not found");
    }
    return result;
  }

  public Interpolator1D getInterpolatorForCurve(final String name) {
    ArgumentChecker.notNull(name, "name");
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
    result = prime * result + _derivatives.hashCode();
    result = prime * result + ((_knownCurves == null) ? 0 : _knownCurves.hashCode());
    result = prime * result + Arrays.hashCode(_marketValues);
    result = prime * result + _unknownCurveInterpolators.hashCode();
    result = prime * result + _unknownCurveNodePoints.hashCode();
    result = prime * result + (_useFiniteDifferenceByDefault ? 1231 : 1237);
    result = prime * result + _fxMatrix.hashCode();
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final MultipleYieldCurveFinderDataBundle other = (MultipleYieldCurveFinderDataBundle) obj;

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
    if (!ObjectUtils.equals(_fxMatrix, other._fxMatrix)) {
      return false;
    }
    return true;
  }
}
