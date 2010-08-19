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
  
  /**
   * default unique id
   */
  public static final String UNIQUE_ID = "mock";
  
  private final ComputationTarget _target;
  private final Set<ValueSpecification> _requirements = new HashSet<ValueSpecification>();
  private final Set<ValueSpecification> _resultSpecs = new HashSet<ValueSpecification>();
  private final Set<ComputedValue> _results = new HashSet<ComputedValue>();
  private final Set<ValueSpecification> _requiredLiveData = new HashSet<ValueSpecification>();
  
  /**
   * @param target Target mock function applies to
   * @param output What the mock function outputs
   * @return A mock function with one input and one output
   */
  public static MockFunction getMockFunction(ComputationTarget target, Object output) {
    ValueRequirement outputReq = new ValueRequirement("OUTPUT", target.toSpecification());
    
    MockFunction fn = new MockFunction(target);
    fn.addResult(outputReq, output);
    return fn;
  }
  
  public static MockFunction getMockFunction(ComputationTarget target, Object output, ValueRequirement input) {
    MockFunction fn = getMockFunction(target, output);
    
    fn.addValueRequirement(input);
    return fn;
  }
  
  public static MockFunction getMockFunction(ComputationTarget target, Object output, MockFunction inputFunction) {
    MockFunction fn = getMockFunction(target, output);
    
    fn.addRequirements(inputFunction.getResultSpecs());
    return fn;
  }
  
  public MockFunction(ComputationTarget target) {
    _target = target;
    setUniqueIdentifier(UNIQUE_ID);
  }
  
  public void addValueRequirement(ValueRequirement requirement) {
    addValueRequirements(Collections.singleton(requirement));    
  }
  
  public void addValueRequirements(Collection<ValueRequirement> requirements) {
    for (ValueRequirement requirement : requirements) {
      addRequirement(toValueSpecification(requirement));      
    }
  }
  
  public void addRequirement(ValueSpecification requirement) {
    addRequirements(Collections.singleton(requirement));
  }
  
  public void addRequirements(Collection<ValueSpecification> requirements) {
    _requirements.addAll(requirements);
  }
  
  public ValueSpecification toValueSpecification(ValueRequirement requirement) {
    return new ValueSpecification(requirement, getUniqueIdentifier());
  }
  
  public ComputedValue getResult(ValueSpecification spec, Object result) {
    return new ComputedValue(spec, result);    
  }
  
  public void addResult(ValueRequirement value, Object result) {
    ValueSpecification resultSpec = toValueSpecification(value);
    ComputedValue computedValue = new ComputedValue(resultSpec, value);
    addResult(computedValue);
  }
  
  public void addResult(ComputedValue result) {
    addResults(Collections.singleton(result));
  }
  
  public void addResults(Collection<ComputedValue> results) {
    _results.addAll(results);
    for (ComputedValue result : _results) {
      _resultSpecs.add(result.getSpecification());
    }
  }
  
  public void addRequiredLiveData(Collection<ValueSpecification> requiredLiveData) {
    _requiredLiveData.addAll(requiredLiveData);
  }
  
  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    return ObjectUtils.equals(target.toSpecification(), _target.toSpecification());
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target) {
    Set<ValueRequirement> reqs = new HashSet<ValueRequirement>();
    for (ValueSpecification req : getRequirements()) {
      reqs.add(req.getRequirementSpecification());
    }
    return reqs;
  }
  
  public ValueSpecification getRequirement() {
    if (_requirements.size() != 1) {
      throw new IllegalStateException();
    }
    return _requirements.iterator().next();
  }
  
  public Set<ValueSpecification> getRequirements() {
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
  public Set<ValueSpecification> getRequiredLiveData() {
    return _requiredLiveData;
  }

}
