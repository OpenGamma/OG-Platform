/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

/**
 * RESTful response wrapper that ensures conversion via Fudge.
 * <p>
 * This allows Fudge basd responses to be sent by XML and JSON.
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
  public String toString() {
    return "FudgeWrapper[" + _value + "]";
  }

}
