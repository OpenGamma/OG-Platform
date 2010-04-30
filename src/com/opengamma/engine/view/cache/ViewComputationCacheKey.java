/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.io.Serializable;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.util.ArgumentChecker;

/**
 * 
 *
 * @author kirk
 */
/*package*/ class ViewComputationCacheKey implements Serializable {
  private final String _viewName;
  private final String _calculationConfigurationName;
  private final long _snapshotTimestamp;
  
  public ViewComputationCacheKey(String viewName, String calculationConfigurationName, long snapshotTimestamp) {
    ArgumentChecker.notNull(viewName, "view name");
    ArgumentChecker.notNull(calculationConfigurationName, "calculation configuration name");
    
    _viewName = viewName;
    _calculationConfigurationName = calculationConfigurationName;
    _snapshotTimestamp = snapshotTimestamp;
  }

  /**
   * @return the viewName
   */
  public String getViewName() {
    return _viewName;
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
    result = prime * result + ((_viewName == null) ? 0 : _viewName.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ViewComputationCacheKey other = (ViewComputationCacheKey) obj;
    if (_snapshotTimestamp != other._snapshotTimestamp) {
      return false;
    }
    if(!ObjectUtils.equals(_viewName, other._viewName)) {
      return false;
    }
    if(!ObjectUtils.equals(_calculationConfigurationName, other._calculationConfigurationName)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
