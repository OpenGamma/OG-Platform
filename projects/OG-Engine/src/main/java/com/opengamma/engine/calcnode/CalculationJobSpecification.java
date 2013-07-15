/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.calcnode;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.threeten.bp.Instant;

import com.opengamma.id.UniqueId;

/**
 * A description of a job to be executed by a calculation node.
 * <p>
 * Providers of jobs pass over a full {@link CalculationJob} including a specification. The specification is returned
 * with the results as the correlation identifier between the result and the job.
 */
public final class CalculationJobSpecification {
  
  /**
   * The cycle identifier.
   */
  private final UniqueId _viewCycleId;
  /**
   * The calculation configuration name, unique within a given cycle identifier. This combined with the cycle identifier uniquely identifies the cache being used for the job.
   */
  private final String _calcConfigName;
  /**
   * The valuation time for the job.
   */
  private final Instant _valuationTime;
  /**
   * The unique job identifier within the system.
   */
  private final long _jobId;
  
  public CalculationJobSpecification(final UniqueId viewCycleId, final String calcConfigName, final Instant valuationTime, final long jobId) {
    // TODO kirk 2009-09-25 -- Check Inputs
    _viewCycleId = viewCycleId;
    _calcConfigName = calcConfigName;
    _valuationTime = valuationTime;
    _jobId = jobId;
  }
  
  public CalculationJobSpecification withJobId(final long jobId) {
    return new CalculationJobSpecification(getViewCycleId(), getCalcConfigName(), getValuationTime(), jobId);
  }

  /**
   * @return the unique identifier of the view cycle
   */
  public UniqueId getViewCycleId() {
    return _viewCycleId;
  }

  /**
   * @return the calculation configuration name
   */
  public String getCalcConfigName() {
    return _calcConfigName;
  }
  
  /**
   * @return the cycle valuation time
   */
  public Instant getValuationTime() {
    return _valuationTime;
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
    result = prime * result + _calcConfigName.hashCode();
    result = prime * result + (int) (_jobId ^ (_jobId >>> 32));
    result = prime * result + _viewCycleId.hashCode();
    result = prime * result + _valuationTime.hashCode();
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
    if (!(obj instanceof CalculationJobSpecification)) {
      return false;
    }
    CalculationJobSpecification other = (CalculationJobSpecification) obj;
    if (_jobId != other._jobId) {
      return false;
    }
    if (!_viewCycleId.equals(other._viewCycleId)) {
      return false;
    }
    if (!_calcConfigName.equals(other._calcConfigName)) {
      return false;
    }
    if (!_valuationTime.equals(other._valuationTime)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
  
}
