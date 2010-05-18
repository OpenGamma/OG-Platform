/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.ObjectUtils;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * A function suitable for use in mock environments.
 *
 */
public class MockFunction extends AbstractFunction implements FunctionInvoker {
  private final ComputationTarget _target;
  private final Set<ValueRequirement> _requirements = new HashSet<ValueRequirement>();
  private final Set<ValueSpecification> _resultSpecs = new HashSet<ValueSpecification>();
  private final Set<ComputedValue> _results = new HashSet<ComputedValue>();
  
  public MockFunction(ComputationTarget target) {
    this(target, Collections.<ValueRequirement>emptySet(), Collections.<ComputedValue>emptySet());
  }

  public MockFunction(ComputationTarget target, Collection<ValueRequirement> requirements, Collection<ComputedValue> results) {
    _target = target;
    _requirements.addAll(requirements);
    _results.addAll(results);
    for (ComputedValue result : _results) {
      _resultSpecs.add(result.getSpecification());
    }
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return ObjectUtils.equals(target, _target);
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    return _requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return _resultSpecs;
  }

  @Override
  public String getShortName() {
    return "Fn for " + _target;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _target.getType();
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (ComputedValue result : _results) {
      if(desiredValues.contains(result.getSpecification().getRequirementSpecification())) {
        results.add(result);
      }
    }
    return results;
  }

}
