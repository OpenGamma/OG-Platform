/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import java.util.Arrays;

import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class MultiCurveBundle {
  private final SingleCurveBundle[] _curveBundles;
  private final int _numberOfInstruments;
  private final int _size;

  public MultiCurveBundle(final SingleCurveBundle[] curveBundles) {
    ArgumentChecker.notNull(curveBundles, "curve bundles");
    _curveBundles = curveBundles;
    _size = curveBundles.length;
    int n = 0;
    for (final SingleCurveBundle bundle : curveBundles) {
      n += bundle.size();
    }
    _numberOfInstruments = n;
  }

  public SingleCurveBundle getCurveBundle(final int n) {
    return _curveBundles[n];
  }

  public SingleCurveBundle[] getCurveBundles() {
    return _curveBundles;
  }

  public int size() {
    return _size;
  }

  public int getNumberOfInstruments() {
    return _numberOfInstruments;
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
    final MultiCurveBundle other = (MultiCurveBundle) obj;
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
