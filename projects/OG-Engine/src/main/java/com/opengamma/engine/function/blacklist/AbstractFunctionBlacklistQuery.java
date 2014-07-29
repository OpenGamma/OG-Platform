/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.depgraph.impl.DependencyNodeImpl;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Partial implementation of {@link FunctionBlacklistQuery}.
 */
public abstract class AbstractFunctionBlacklistQuery implements FunctionBlacklistQuery {

  @Override
  public boolean isBlacklisted(final DependencyNodeFunction function) {
    return isBlacklisted(function.getFunctionId(), function.getParameters());
  }

  @Override
  public boolean isBlacklisted(final DependencyNodeFunction function, final ComputationTargetSpecification target) {
    return isBlacklisted(function.getFunctionId(), function.getParameters(), target);
  }

  @Override
  public boolean isBlacklisted(final DependencyNodeFunction function, final ComputationTargetSpecification target, final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
    return isBlacklisted(function.getFunctionId(), function.getParameters(), target, inputs, outputs);
  }

  @Override
  public boolean isBlacklisted(final DependencyNodeFunction function, final ComputationTargetSpecification target, final Collection<ValueSpecification> inputs,
      final Collection<ValueSpecification> outputs) {
    return isBlacklisted(function.getFunctionId(), function.getParameters(), target, inputs, outputs);
  }

  @Override
  public boolean isBlacklisted(final DependencyNode node) {
    return isBlacklisted(node.getFunction(), node.getTarget(), DependencyNodeImpl.getInputValueArray(node), DependencyNodeImpl.getOutputValueArray(node));
  }

  @Override
  public boolean isBlacklisted(final CalculationJobItem jobItem) {
    return isBlacklisted(jobItem.getFunctionUniqueIdentifier(), jobItem.getFunctionParameters(), jobItem.getComputationTargetSpecification(), jobItem.getInputs(), jobItem.getOutputs());
  }

}
