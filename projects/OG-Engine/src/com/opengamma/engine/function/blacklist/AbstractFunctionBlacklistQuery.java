/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Set;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.function.ParameterizedFunction;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.engine.view.calcnode.CalculationJobItem;

/**
 * Partial implementation of {@link FunctionBlacklistQuery}.
 */
public abstract class AbstractFunctionBlacklistQuery implements FunctionBlacklistQuery {

  @Override
  public boolean isBlacklisted(final ParameterizedFunction function) {
    return isBlacklisted(function.getFunction().getFunctionDefinition().getUniqueId(), function.getParameters());
  }

  @Override
  public boolean isBlacklisted(final ParameterizedFunction function, final ComputationTargetSpecification target) {
    return isBlacklisted(function.getFunction().getFunctionDefinition().getUniqueId(), function.getParameters(), target);
  }

  @Override
  public boolean isBlacklisted(final ParameterizedFunction function, final ComputationTargetSpecification target, final Set<ValueSpecification> inputs, final Set<ValueSpecification> outputs) {
    return isBlacklisted(function.getFunction().getFunctionDefinition().getUniqueId(), function.getParameters(), target, inputs, outputs);
  }

  @Override
  public boolean isBlacklisted(final DependencyNode node) {
    return isBlacklisted(node.getFunction(), node.getComputationTarget(), node.getInputValues(), node.getOutputValues());
  }

  @Override
  public boolean isBlacklisted(final CalculationJobItem jobItem) {
    return isBlacklisted(jobItem.getFunctionUniqueIdentifier(), jobItem.getFunctionParameters(), jobItem.getComputationTargetSpecification(), jobItem.getInputs(), jobItem.getOutputs());
  }

}
