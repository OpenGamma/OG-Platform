/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.PublicAPI;

/**
 * The result of a single target across all configurations of a view calculation. 
 */
@PublicAPI
public interface ViewTargetResultModel {

  /**
   * Gets the configuration names for which results for this target were requested. 
   * 
   * @return the configuration names, not {@code null}
   */
  Collection<String> getCalculationConfigurationNames();

  /**
   * Gets all of the values calculated for this target, for a given configuration.
   * 
   * @param calcConfigurationName the calculation configuration name, not {@code null}
   * @return the computed values, or {@code null} if the configuration name is unknown
   */
  Collection<ComputedValue> getAllValues(String calcConfigurationName);

}
