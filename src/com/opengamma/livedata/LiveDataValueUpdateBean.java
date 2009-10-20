/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata;

import java.io.Serializable;

import org.fudgemsg.FudgeFieldContainer;


/**
 * A simple JavaBean-based implementation of {@link LiveDataValueUpdate}.
 *
 * @author kirk
 */
public class LiveDataValueUpdateBean implements LiveDataValueUpdate,
    Serializable {
  private final long _relevantTimestamp;
  private final LiveDataSpecification _specification;
  private final FudgeFieldContainer _fieldContainer;
  
  public LiveDataValueUpdateBean(long relevantTimestamp, LiveDataSpecification specification, FudgeFieldContainer fieldContainer) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _relevantTimestamp = relevantTimestamp;
    _specification = specification;
    _fieldContainer = fieldContainer;
  }

  @Override
  public FudgeFieldContainer getFields() {
    return _fieldContainer;
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
