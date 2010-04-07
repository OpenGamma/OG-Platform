/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;

/**
 * The response that a Calculation Node will return to invokers.
 *
 * @author kirk
 */
public class CalculationJobResult implements Serializable {
  public static final String INVOCATION_RESULT_FIELD_NAME = "result";
  public static final String DURATION_FIELD_NAME = "duration";
  
  private final CalculationJobSpecification _specification;
  private final InvocationResult _result;
  private final long _duration;
  
  public CalculationJobResult(
      CalculationJobSpecification specification,
      InvocationResult result,
      long duration) {
    // TODO kirk 2009-09-29 -- Check Inputs.
    _specification = specification;
    _result = result;
    _duration = duration;
  }

  /**
   * @return the specification
   */
  public CalculationJobSpecification getSpecification() {
    return _specification;
  }

  /**
   * @return the result
   */
  public InvocationResult getResult() {
    return _result;
  }

  /**
   * @return the duration
   */
  public long getDuration() {
    return _duration;
  }

  public FudgeFieldContainer toFudgeMsg(FudgeContext fudgeContext) {
    MutableFudgeFieldContainer msg = fudgeContext.newMessage();
    getSpecification().writeFields(msg);
    msg.add(INVOCATION_RESULT_FIELD_NAME, getResult().name());
    msg.add(DURATION_FIELD_NAME, getDuration());
    return msg;
  }
  
  public static CalculationJobResult fromFudgeMsg(FudgeFieldContainer msg) {
    CalculationJobSpecification jobSpec = CalculationJobSpecification.fromFudgeMsg(msg);
    
    InvocationResult result = InvocationResult.valueOf(msg.getString(INVOCATION_RESULT_FIELD_NAME));
    long duration = msg.getLong(DURATION_FIELD_NAME);
    
    return new CalculationJobResult(jobSpec, result, duration);
  }
}
