package com.opengamma.financial.value;

import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirementNames;

/**
 * Exposes the value of a target under the common value name {@link ValueRequirementNames#VALUE} by mapping from the
 * single satisfiable value named with one of the more descriptive value names. 
 */
public class ValueFunction extends ValueRenamingFunction {

  private static final Set<String> VALUE_NAMES = ImmutableSet.of(
      ValueRequirementNames.PRESENT_VALUE,
      ValueRequirementNames.FAIR_VALUE);
  
  public ValueFunction(ComputationTargetType targetType) {
    super(VALUE_NAMES, ValueRequirementNames.VALUE, targetType);
  }

}
