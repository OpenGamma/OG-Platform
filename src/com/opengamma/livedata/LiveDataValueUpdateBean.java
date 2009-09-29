/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.io.Serializable;

import com.opengamma.fudge.FudgeMsg;

/**
 * A simple JavaBean-based implementation of {@link LiveDataValueUpdate}.
 *
 * @author kirk
 */
public class LiveDataValueUpdateBean implements LiveDataValueUpdate,
    Serializable {
  private final long _relevantTimestamp;
  private final LiveDataSpecification _specification;
  private final FudgeMsg _fields;
  
  public LiveDataValueUpdateBean(long relevantTimestamp, LiveDataSpecification specification, FudgeMsg fields) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _relevantTimestamp = relevantTimestamp;
    _specification = specification;
    _fields = fields;
  }

  @Override
  public FudgeMsg getFields() {
    return _fields;
  }

  @Override
  public long getRelevantTimestamp() {
    return _relevantTimestamp;
  }

  @Override
  public LiveDataSpecification getSpecification() {
    return _specification;
  }

}
