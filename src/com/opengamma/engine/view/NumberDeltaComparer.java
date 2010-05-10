/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

/**
 * Allows two {@link Number}s to be compared to see whether they differ sufficiently for the change to be considered
 * a delta.
 *
 * @author jonathan
 */
public class NumberDeltaComparer implements DeltaComparer<Number> {

  private final double _multiplier;
  
  /**
   * Constructs a new {@link NumberDeltaComparer} for detecting differences to the given number of decimal places.
   * 
   * @param  decimalPlaces
   *         The number of places after the decimal point within which a change is considered to be a delta. If set to
   *         0, only the integral part of the values are considered. If set to a negative number, digits to the left of
   *         the decimal point become insignificant.
   */
  public NumberDeltaComparer(int decimalPlaces) {
    _multiplier = Math.pow(10, decimalPlaces);
  }
  
  @Override
  public boolean isDelta(Number previousValue, Number newValue) {
    if (previousValue == null && newValue == null) {
      return false;
    }
    if (previousValue == null || newValue == null) {
      return true;
    }
    
    long previousCompare = (long) (previousValue.doubleValue() * _multiplier);
    long newCompare = (long) (newValue.doubleValue() * _multiplier);
    return previousCompare != newCompare;
  }
  
}
