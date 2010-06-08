/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.math.interpolation;

import java.io.Serializable;

/**
 * 
 */
public final class InterpolationBoundedValues implements Serializable {
  private final Double _lowerBoundKey;
  private final Double _lowerBoundValue;
  private final Double _higherKey;
  private final Double _higherValue;
  
  public InterpolationBoundedValues(Double lowerBoundKey, Double lowerBoundValue, Double higherKey, Double higherValue) {
    _lowerBoundKey = lowerBoundKey;
    _lowerBoundValue = lowerBoundValue;
    _higherKey = higherKey;
    _higherValue = higherValue;
  }
  
  /**
   * @return the _lowerBoundKey
   */
  public Double getLowerBoundKey() {
    return _lowerBoundKey;
  }
  /**
   * @return the _lowerBoundValue
   */
  public Double getLowerBoundValue() {
    return _lowerBoundValue;
  }
  /**
   * @return the _nextKey
   */
  public Double getHigherKey() {
    return _higherKey;
  }
  /**
   * @return the _nextValue
   */
  public Double getHigherValue() {
    return _higherValue;
  }

}
