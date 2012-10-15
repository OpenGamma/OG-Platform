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
public class ForwardPriceRenamingFunction extends ValueRenamingFunction {

  private static final Set<String> VALUE_NAMES_TO_CHANGE = ImmutableSet.of(
      ValueRequirementNames.FORWARD,
      ValueRequirementNames.UNDERLYING_MARKET_PRICE);

  public ForwardPriceRenamingFunction() {
    super(VALUE_NAMES_TO_CHANGE, ValueRequirementNames.FORWARD_PRICE, ComputationTargetType.SECURITY.or(ComputationTargetType.POSITION).or(ComputationTargetType.TRADE));
  }

}
