/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.exclusion;

import com.opengamma.engine.function.FunctionDefinition;

/**
 * Source of {@link ExclusionGroup} instances.
 */
public interface FunctionExclusionGroups {

  /**
   * Returns the exclusion group that a function definition is part of.
   * 
   * @param function the function to test, not null
   * @return the exclusion group, or null if the function is not part of an exclusion group
   */
  FunctionExclusionGroup getExclusionGroup(FunctionDefinition function);

}
