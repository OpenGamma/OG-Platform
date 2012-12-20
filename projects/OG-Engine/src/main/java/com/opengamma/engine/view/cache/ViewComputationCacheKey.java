/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import java.io.Serializable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;

/**
 * Identifies a {@link ViewComputationCache}.
 */
/*package*/ class ViewComputationCacheKey implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private final UniqueId _viewCycleId;
  private final String _calculationConfigurationName;
  
  public ViewComputationCacheKey(UniqueId viewCycleId, String calculationConfigurationName) {
    ArgumentChecker.notNull(viewCycleId, "viewCycleId");
    ArgumentChecker.notNull(calculationConfigurationName, "calculationConfigurationName");
    _viewCycleId = viewCycleId;
    _calculationConfigurationName = calculationConfigurationName;
  }

  /**
   * Gets the unique identifer of the view cycle.
   * 
   * @return the unique identifier of the view cycle, not null
   */
  public UniqueId getViewCycleId() {
    return _viewCycleId;
  }

  /**
   * Gets the calculation configuration name.
   * 
   * @return the calculation configuration name, not null
   */
  public String getCalculationConfigurationName() {
    return _calculationConfigurationName;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + _calculationConfigurationName.hashCode();
    result = prime * result + _viewCycleId.hashCode();
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
    if (!(obj instanceof ViewComputationCacheKey)) {
      return false;
    }
    ViewComputationCacheKey other = (ViewComputationCacheKey) obj;
    if (!_calculationConfigurationName.equals(other._calculationConfigurationName)) {
      return false;
    }
    if (!_viewCycleId.equals(other._viewCycleId)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }

}
