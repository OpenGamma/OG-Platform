/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;

/**
 * The response that a Calculation Node will return to invokers.
 *
 * @author kirk
 */
public class CalculationJobResult implements Serializable {
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

}
