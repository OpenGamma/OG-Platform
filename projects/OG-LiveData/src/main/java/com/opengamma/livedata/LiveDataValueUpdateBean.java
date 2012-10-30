/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.fudgemsg.FudgeMsg;

import com.opengamma.util.PublicAPI;

/**
 * A simple implementation of a market data update sent from server to client. 
 */
@PublicAPI
public class LiveDataValueUpdateBean implements LiveDataValueUpdate, Serializable {

  /** Serialization version. */
  private static final long serialVersionUID = 1L;

  /**
   * The sequence number.
   */
  private final long _sequenceNumber;
  /**
   * The live data specification.
   */
  private final LiveDataSpecification _specification;
  /**
   * The data fields.
   */
  private final FudgeMsg _fieldContainer;

  /**
   * Creates an instance.
   * 
   * @param sequenceNumber  the sequence number
   * @param specification  the specification
   * @param fieldContainer  the fields held as a Fudge message
   */
  public LiveDataValueUpdateBean(long sequenceNumber, LiveDataSpecification specification, FudgeMsg fieldContainer) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _sequenceNumber = sequenceNumber;
    _specification = specification;
    _fieldContainer = fieldContainer;
  }

  //-------------------------------------------------------------------------
  @Override
  public long getSequenceNumber() {
    return _sequenceNumber;
  }

  @Override
  public LiveDataSpecification getSpecification() {
    return _specification;
  }

  @Override
  public FudgeMsg getFields() {
    return _fieldContainer;
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj instanceof LiveDataValueUpdateBean) {
      LiveDataValueUpdateBean other = (LiveDataValueUpdateBean) obj;
      return _sequenceNumber == other._sequenceNumber &&
          ObjectUtils.equals(_specification, other._specification) &&
          ObjectUtils.equals(_fieldContainer, other._fieldContainer);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return ((int) (_sequenceNumber ^ (_sequenceNumber >>> 32))) ^ ObjectUtils.hashCode(_specification) ^ ObjectUtils.hashCode(_fieldContainer);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("LiveDataValueUpdateBean[");
    buf.append(_sequenceNumber);
    buf.append(", ");
    buf.append(_specification);
    buf.append(", ");
    buf.append(_fieldContainer);
    buf.append("]");
    return buf.toString();
  }

}
