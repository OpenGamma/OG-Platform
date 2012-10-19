/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

/**
 * RESTful response wrapper that ensures conversion via Fudge.
 * <p>
 * This allows Fudge based responses to be sent by XML and JSON.
 */
public final class FudgeResponse {

  /**
   * The wrapped value.
   */
  private Object _value;

  /**
   * Creates an instance.
   * 
   * @param value  the value to return, not null
   */
  public FudgeResponse(Object value) {
    _value = value;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the value.
   * 
   * @return the value, not null
   */
  public Object getValue() {
    return _value;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof FudgeResponse) {
      FudgeResponse other = (FudgeResponse) obj;
      if (_value != null ? !_value.equals(other._value) : other._value != null) {
        return false;
      }
      return true;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return _value != null ? _value.hashCode() : 0;
  }

  @Override
  public String toString() {
    return "FudgeWrapper[" + _value + "]";
  }

}
