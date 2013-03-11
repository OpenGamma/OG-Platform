/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.value;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Exposes the value of a target under the common value name {@link ValueRequirementNames#VALUE} by mapping from the
 * single satisfiable value named with one of the more descriptive value names. 
 */
public class ValueFunction extends ValueRenamingFunction {

  private static final Set<String> VALUE_NAMES = ImmutableSet.of(
      ValueRequirementNames.PRESENT_VALUE,
      ValueRequirementNames.FAIR_VALUE);
  
  public ValueFunction() {
    super(VALUE_NAMES, ValueRequirementNames.VALUE, ComputationTargetType.POSITION.or(ComputationTargetType.SECURITY).or(ComputationTargetType.TRADE));
  }

}
