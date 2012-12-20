/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.property;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;

/**
 * Injects the default aggregation style when omitted.
 */
public class AggregationDefaultPropertyFunction extends StaticDefaultPropertyFunction {
  
  private final Set<String> _styles;

  public AggregationDefaultPropertyFunction(final String valueName, final String style) {
    super(ComputationTargetType.PORTFOLIO_NODE, ValuePropertyNames.AGGREGATION, true, valueName);
    _styles = Collections.singleton(style);
  }

  public AggregationDefaultPropertyFunction(final String valueName, final String... styles) {
    super(ComputationTargetType.PORTFOLIO_NODE, ValuePropertyNames.AGGREGATION, true, valueName);
    _styles = new HashSet<String>(Arrays.asList(styles));
  }

  @Override
  protected Set<String> getDefaultValue(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    return _styles;
  }

}
