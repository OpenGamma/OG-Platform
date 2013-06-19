/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Exists to serve other test classes with instances from
 * the depgraph package, the visibility of which would 
 * otherwise be restricted.
 */
public class ResolutionFailureObjectFactory {
  
  /**
   * @param valueRequirement the value requirement
   * @return An unsatisfied resolution failure instance.
   */
  public static ResolutionFailure unsatisfiedResolutionFailure(final ValueRequirement valueRequirement) {
    return ResolutionFailureImpl.unsatisfied(valueRequirement);
  }
  
  
}
