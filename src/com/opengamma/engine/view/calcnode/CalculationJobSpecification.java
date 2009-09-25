/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;

/**
 * 
 *
 * @author kirk
 */
public class CalculationJobSpecification implements Serializable {
  private final String _viewName;
  private final long _iterationTimestamp;
  private final long _jobId;
  
  public CalculationJobSpecification(String viewName, long iterationTimestamp, long jobId) {
    // TODO kirk 2009-09-25 -- Check Inputs
    _viewName = viewName;
    _iterationTimestamp = iterationTimestamp;
    _jobId = jobId;
  }

  /**
   * @return the viewName
   */
  public String getViewName() {
    return _viewName;
  }

  /**
   * @return the iterationTimestamp
   */
  public long getIterationTimestamp() {
    return _iterationTimestamp;
  }

  /**
   * @return the jobId
   */
  public long getJobId() {
    return _jobId;
  }

  // TODO kirk 2009-09-29 -- HashCode, Equals, toString()
}
