/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import java.util.ArrayList;
import java.util.Arrays;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.util.ArgumentChecker;

/**
 * @param <T> The type of the curve generator
 */
public class MultiCurveBundle<T extends GeneratorCurve> {

  private final SingleCurveBundle<T>[] _curveBundles;
  /** The total number of instruments in the MultiCurveBundle. It is the sum of the number of instruments in each single bundle **/
  private final int _numberOfInstruments;
  /** The size of the MultiCurveBundle, i.e. the number of SingleCurveBundle **/
  private final int _size;
  /** The names of the curves **/
  private final ArrayList<String> _curveNames;

  public MultiCurveBundle(final SingleCurveBundle<T>[] curveBundles) {
    ArgumentChecker.notNull(curveBundles, "curve bundles");
    _curveBundles = curveBundles;
    _size = curveBundles.length;
    _curveNames = new ArrayList<>();
    int n = 0;
    for (final SingleCurveBundle<T> bundle : curveBundles) {
      n += bundle.size();
      _curveNames.add(bundle.getCurveName());
    }
    _numberOfInstruments = n;
  }

  public SingleCurveBundle<T> getCurveBundle(final int n) {
    return _curveBundles[n];
  }

  public SingleCurveBundle<T>[] getCurveBundles() {
    return _curveBundles;
  }

  public int size() {
    return _size;
  }

  public int getNumberOfInstruments() {
    return _numberOfInstruments;
  }

  /**
   * Returns the list of names of the multiple curves in the bundle.
   * @return The name list.
   */
  public ArrayList<String> getNames() {
    return _curveNames;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + Arrays.hashCode(_curveBundles);
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
    if (!(obj instanceof MultiCurveBundle)) {
      return false;
    }
    final MultiCurveBundle<?> other = (MultiCurveBundle<?>) obj;
    if (_numberOfInstruments != other._numberOfInstruments) {
      return false;
    }
    if (_size != other._size) {
      return false;
    }
    if (!Arrays.deepEquals(_curveBundles, other._curveBundles)) {
      return false;
    }
    return true;
  }

}
