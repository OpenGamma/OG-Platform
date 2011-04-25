/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.calcnode;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.UniqueIdentifier;

/**
 * A description of a job that will be executed by a Calculation Node.
 * Providers of jobs pass over a full {@link CalculationJob}, and the
 * specification of that job is returned by the Calculation Node.
 */
public class CalculationJobSpecification implements Serializable {
  
  private final UniqueIdentifier _viewProcessId;
  private final String _calcConfigName;
  private final long _iterationTimestamp;
  private final long _jobId;
  
  public CalculationJobSpecification(UniqueIdentifier viewProcessId, String calcConfigName, long iterationTimestamp, long jobId) {
    // TODO kirk 2009-09-25 -- Check Inputs
    _viewProcessId = viewProcessId;
    _calcConfigName = calcConfigName;
    _iterationTimestamp = iterationTimestamp;
    _jobId = jobId;
  }
  
  public CalculationJobSpecification(CalculationJobSpecification other) {
    this(other._viewProcessId, other._calcConfigName, other._iterationTimestamp, other._jobId);
  }

  /**
   * @return the unique identifier of the view process
   */
  public UniqueIdentifier getViewProcessId() {
    return _viewProcessId;
  }

  /**
   * @return the calcConfigName
   */
  public String getCalcConfigName() {
    return _calcConfigName;
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
    result = prime * result + ((_viewProcessId == null) ? 0 : _viewProcessId.hashCode());
    result = prime * result + ((_calcConfigName == null) ? 0 : _calcConfigName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof CalculationJobSpecification)) {
      return false;
    }
    CalculationJobSpecification other = (CalculationJobSpecification) obj;
    return (_iterationTimestamp == other._iterationTimestamp)
           && (_jobId == other._jobId)
           && ObjectUtils.equals(_viewProcessId, other._viewProcessId)
           && ObjectUtils.equals(_calcConfigName, other._calcConfigName);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
}
