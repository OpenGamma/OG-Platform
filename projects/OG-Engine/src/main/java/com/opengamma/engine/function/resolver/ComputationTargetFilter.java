/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function.resolver;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.util.PublicAPI;

/**
 * A filtering strategy that can be used to only apply a {@link ResolutionRule} to a sub-set of the targets that the function would naturally apply to.
 */
@PublicAPI
public interface ComputationTargetFilter {

  /**
   * Tests the target for validity.
   * 
   * @param target the target to test, not null
   * @return true to apply to the target, false to reject
   */
  boolean accept(ComputationTarget target);

}
