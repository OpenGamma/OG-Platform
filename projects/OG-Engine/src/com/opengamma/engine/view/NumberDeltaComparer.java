/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

/**
 * Allows two {@link Number}s to be compared to see whether they differ sufficiently for the change to be considered
 * a delta.
 */
public class NumberDeltaComparer implements DeltaComparer<Number> {

  private static final String DECIMAL_PLACES_FIELD = "decimalPlaces";
  
  private final int _decimalPlaces;
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
    _decimalPlaces = decimalPlaces;
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
  
  public FudgeMsg toFudgeMsg(FudgeSerializer fudgeContext) {
    MutableFudgeMsg msg = fudgeContext.newMessage();
    msg.add(DECIMAL_PLACES_FIELD, _decimalPlaces);
    return msg;
  }
  
  public static NumberDeltaComparer fromFudgeMsg(FudgeDeserializer fudgeContext, FudgeMsg msg) {
    return new NumberDeltaComparer(msg.getInt(DECIMAL_PLACES_FIELD));
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _decimalPlaces;
    long temp;
    temp = Double.doubleToLongBits(_multiplier);
    result = prime * result + (int) (temp ^ (temp >>> 32));
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof NumberDeltaComparer)) {
      return false;
    }
    NumberDeltaComparer other = (NumberDeltaComparer) obj;
    return ObjectUtils.equals(_decimalPlaces, other._decimalPlaces);
  }
  
  
  
}
