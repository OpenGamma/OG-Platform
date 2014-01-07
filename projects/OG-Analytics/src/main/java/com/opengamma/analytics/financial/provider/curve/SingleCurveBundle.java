/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.analytics.financial.provider.curve;

import java.util.Arrays;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.analytics.financial.curve.interestrate.generator.GeneratorCurve;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.util.ArgumentChecker;

/**
 * @param <T> The type of the curve generator
 */
public class SingleCurveBundle<T extends GeneratorCurve> {
  private final String _curveName;
  private final InstrumentDerivative[] _derivatives;
  private final T _curveGenerator;
  private final double[] _startingPoints;
  private final int _size;

  public SingleCurveBundle(final String curveName, final InstrumentDerivative[] derivatives,
      final double[] startingPoint, final T curveGenerator) {
    ArgumentChecker.notNull(curveName, "curve name");
    ArgumentChecker.notNull(derivatives, "derivatives");
    ArgumentChecker.notNull(startingPoint, "starting point");
    ArgumentChecker.notNull(curveGenerator, "curve generator");
    ArgumentChecker.isTrue(derivatives.length == startingPoint.length, "Must have one starting point per derivative");
    _curveName = curveName;
    _derivatives = derivatives;
    _startingPoints = startingPoint;
    _curveGenerator = curveGenerator;
    _size = derivatives.length;
  }

  /**
   * Gets the curve name.
   * @return The curve name
   */
  public String getCurveName() {
    return _curveName;
  }

  /**
   * Gets the instruments that are used in curve construction.
   * @return The instruments
   */
  public InstrumentDerivative[] getDerivatives() {
    return _derivatives;
  }

  /**
   * Gets the starting points used by the root-finder for each instrument.
   * @return The starting points
   */
  public double[] getStartingPoint() {
    return _startingPoints;
  }

  /**
   * Gets the curve generator.
   * @return The curve generator
   */
  public T getCurveGenerator() {
    return _curveGenerator;
  }

  public int size() {
    return _size;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _curveGenerator.hashCode();
    result = prime * result + _curveName.hashCode();
    result = prime * result + Arrays.hashCode(_derivatives);
    result = prime * result + Arrays.hashCode(_startingPoints);
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
    if (!(obj instanceof SingleCurveBundle)) {
      return false;
    }
    final SingleCurveBundle<?> other = (SingleCurveBundle<?>) obj;
    if (_size != other._size) {
      return false;
    }
    if (!ObjectUtils.equals(_curveName, other._curveName)) {
      return false;
    }
    if (!Arrays.equals(_startingPoints, other._startingPoints)) {
      return false;
    }
    if (!Arrays.deepEquals(_derivatives, other._derivatives)) {
      return false;
    }
    if (!ObjectUtils.equals(_curveGenerator, other._curveGenerator)) {
      return false;
    }
    return true;
  }

}
