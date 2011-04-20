/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 */
/*package*/ class ViewComputationCacheKey implements Serializable {
  private final UniqueIdentifier _viewProcessId;
  private final String _calculationConfigurationName;
  private final long _snapshotTimestamp;
  
  public ViewComputationCacheKey(UniqueIdentifier viewProcessId, String calculationConfigurationName, long snapshotTimestamp) {
    ArgumentChecker.notNull(viewProcessId, "viewProcessId");
    ArgumentChecker.notNull(calculationConfigurationName, "calculation configuration name");
    
    _viewProcessId = viewProcessId;
    _calculationConfigurationName = calculationConfigurationName;
    _snapshotTimestamp = snapshotTimestamp;
  }

  /**
   * @return the view process id
   */
  public UniqueIdentifier getViewProcessId() {
    return _viewProcessId;
  }

  /**
   * @return the calculationConfigurationName
   */
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  /**
   * @return the snapshotTimestamp
   */
  public long getSnapshotTimestamp() {
    return _snapshotTimestamp;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime
        * result
        + ((_calculationConfigurationName == null) ? 0
            : _calculationConfigurationName.hashCode());
    result = prime * result
        + (int) (_snapshotTimestamp ^ (_snapshotTimestamp >>> 32));
    result = prime * result + ((_viewProcessId == null) ? 0 : _viewProcessId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof ViewComputationCacheKey)) {
      return false;
    }
    ViewComputationCacheKey other = (ViewComputationCacheKey) obj;
    return (_snapshotTimestamp == other._snapshotTimestamp)
          && ObjectUtils.equals(_viewProcessId, other._viewProcessId)
          && ObjectUtils.equals(_calculationConfigurationName, other._calculationConfigurationName);
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
