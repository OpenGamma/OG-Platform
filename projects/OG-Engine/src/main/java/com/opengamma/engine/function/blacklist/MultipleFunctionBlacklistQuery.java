/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function.blacklist;

import java.util.ArrayList;
import java.util.Collection;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.calcnode.CalculationJobItem;
import com.opengamma.engine.depgraph.DependencyNode;
import com.opengamma.engine.depgraph.DependencyNodeFunction;
import com.opengamma.engine.function.FunctionParameters;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;

/**
 * Wraps multiple {@link FunctionBlacklistQuery} instances up as a single point that can be used to test a potential invocation.
 */
public class MultipleFunctionBlacklistQuery implements FunctionBlacklistQuery {

  private final Collection<FunctionBlacklistQuery> _underlying;

  public MultipleFunctionBlacklistQuery(final Collection<FunctionBlacklistQuery> underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    _underlying = new ArrayList<FunctionBlacklistQuery>(underlying);
  }

  private Collection<FunctionBlacklistQuery> getUnderlying() {
    return _underlying;
  }

  @Override
  public boolean isEmpty() {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (!underlying.isEmpty()) {
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(functionIdentifier, functionParameters)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final DependencyNodeFunction function) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(function)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final ComputationTargetSpecification target) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(target)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(functionIdentifier, functionParameters, target)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final DependencyNodeFunction function, final ComputationTargetSpecification target) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(function, target)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target, final ValueSpecification[] inputs,
      final ValueSpecification[] outputs) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final String functionIdentifier, final FunctionParameters functionParameters, final ComputationTargetSpecification target, final Collection<ValueSpecification> inputs,
      final Collection<ValueSpecification> outputs) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(functionIdentifier, functionParameters, target, inputs, outputs)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final DependencyNodeFunction function, final ComputationTargetSpecification target, final ValueSpecification[] inputs, final ValueSpecification[] outputs) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(function, target, inputs, outputs)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final DependencyNodeFunction function, final ComputationTargetSpecification target, final Collection<ValueSpecification> inputs,
      final Collection<ValueSpecification> outputs) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(function, target, inputs, outputs)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final DependencyNode node) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(node)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean isBlacklisted(final CalculationJobItem jobItem) {
    for (FunctionBlacklistQuery underlying : getUnderlying()) {
      if (underlying.isBlacklisted(jobItem)) {
        return true;
      }
    }
    return false;
  }

}
