/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.depgraph.ambiguity;

import com.opengamma.engine.value.ValueRequirement;

/**
 * Service for checking whether, and how, any given value requirement may be ambiguous.
 */
public interface RequirementAmbiguityChecker {

  FullRequirementResolution resolve(ValueRequirement requirement);

}
