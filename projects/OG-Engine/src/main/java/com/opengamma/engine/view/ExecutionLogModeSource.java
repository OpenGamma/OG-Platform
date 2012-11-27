/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view;

import java.util.Set;

import com.opengamma.engine.value.ValueSpecification;

/**
 * Represents a queryable source of the execution log mode to apply for a value specification.
 */
public interface ExecutionLogModeSource {

  /**
   * Gets the log mode for a single value specification.
   * 
   * @param valueSpec  the value specification, not null
   * @return the log mode, not null
   */
  ExecutionLogMode getLogMode(ValueSpecification valueSpec);
  
  /**
   * Gets the maximum log mode of a set of value specifications.
   * 
   * @param valueSpecs  the set of value specifications, not null
   * @return the log mode, not null
   */
  ExecutionLogMode getLogMode(Set<ValueSpecification> valueSpecs);
  
}
