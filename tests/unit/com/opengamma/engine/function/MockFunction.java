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
  private final Set<ValueRequirement> _requiredLiveData = new HashSet<ValueRequirement>();
  
  public MockFunction(ComputationTarget target) {
    this(target, Collections.<ValueRequirement>emptySet());
  }
  
  public MockFunction(ComputationTarget target, Set<ValueRequirement> requiredLiveData) {
    this(target, Collections.<ValueRequirement>emptySet(), Collections.<ComputedValue>emptySet(), requiredLiveData);
  }
  
  public MockFunction(ComputationTarget target, Set<ValueRequirement> requirements, Collection<ComputedValue> results) {
    this(target, requirements, results, Collections.<ValueRequirement>emptySet());
  }

  public MockFunction(ComputationTarget target, 
      Collection<ValueRequirement> requirements, 
      Collection<ComputedValue> results,
      Collection<ValueRequirement> requiredLiveData) {
    _target = target;
    _requirements.addAll(requirements);
    _results.addAll(results);
    for (ComputedValue result : _results) {
      _resultSpecs.add(result.getSpecification());
    }
    _requiredLiveData.addAll(requiredLiveData);
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return ObjectUtils.equals(target.toSpecification(), _target.toSpecification());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    return getRequirements();
  }
  
  public ValueRequirement getRequirement() {
    if (_requirements.size() != 1) {
      throw new IllegalStateException();
    }
    return _requirements.iterator().next();
  }
  
  public Set<ValueRequirement> getRequirements() {
    return _requirements;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return getResultSpecs();
  }
  
  public Set<ValueSpecification> getResultSpecs() {
    return _resultSpecs;
  }
  
  public ValueSpecification getResultSpec() {
    if (_resultSpecs.size() != 1) {
      throw new IllegalStateException();
    }
    return _resultSpecs.iterator().next();
  }
  
  public Set<ComputedValue> getResults() {
    return _results;
  }
  
  public ComputedValue getResult() {
    if (_results.size() != 1) {
      throw new IllegalStateException();
    }
    return _results.iterator().next();
  }

  @Override
  public String getShortName() {
    return "Fn for " + _target;
  }

  @Override
  public ComputationTargetType getTargetType() {
    return _target.getType();
  }
  
  public ComputationTarget getTarget() {
    return _target;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs,
      ComputationTarget target, Set<ValueRequirement> desiredValues) {
    Set<ComputedValue> results = new HashSet<ComputedValue>();
    for (ComputedValue result : _results) {
      if (desiredValues.contains(result.getSpecification().getRequirementSpecification())) {
        results.add(result);
      }
    }
    return results;
  }
  
  @Override
  public Set<ValueRequirement> getRequiredLiveData() {
    return _requiredLiveData;
  }

}
