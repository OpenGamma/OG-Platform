/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Collection;
import java.util.Map;

import com.opengamma.engine.value.ComputedValue;
import com.opengamma.util.PublicAPI;

/**
 * The result of a single target across all configurations of a view calculation. 
 */
@PublicAPI
public interface ViewTargetResultModel {

  /**
   * Returns all configuration names this target was defined in.
   * 
   * @return the configuration names
   */
  Collection<String> getCalculationConfigurationNames();

  /**
   * Returns all of the values calculated for a target for a given configuration.
   * 
   * @param calcConfigurationName the calculation configuration
   * @return the values as a map of value name to computed value, or {@code null} if the configuration name is invalid
   */
  Map<String, ComputedValue> getValues(String calcConfigurationName);

}
