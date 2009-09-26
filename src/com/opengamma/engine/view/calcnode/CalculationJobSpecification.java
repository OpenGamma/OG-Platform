/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

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
  
  public CalculationJobSpecification(CalculationJobSpecification other) {
    this(other._viewName, other._iterationTimestamp, other._jobId);
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + (int) (_iterationTimestamp ^ (_iterationTimestamp >>> 32));
    result = prime * result + (int) (_jobId ^ (_jobId >>> 32));
    result = prime * result + ((_viewName == null) ? 0 : _viewName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    CalculationJobSpecification other = (CalculationJobSpecification) obj;
    if (_iterationTimestamp != other._iterationTimestamp)
      return false;
    if (_jobId != other._jobId)
      return false;
    if(!ObjectUtils.equals(_viewName, other._viewName)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
