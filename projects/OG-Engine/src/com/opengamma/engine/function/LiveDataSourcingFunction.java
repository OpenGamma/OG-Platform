/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Special case of a function implementation that is never executed by the graph executor but
 * used to source information from a live data provider.
 */
public class LiveDataSourcingFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Function unique ID
   */
  public static final String UNIQUE_ID = "LiveDataSourcingFunction";

  private final Pair<ValueRequirement, ValueSpecification> _value;

  public LiveDataSourcingFunction(ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "Value Requirement");
    setUniqueIdentifier(UNIQUE_ID);
    _value = Pair.of(requirement, new ValueSpecification(requirement, getUniqueIdentifier()));
  }

  // [ENG-216] Constructor to allow sub-classing for when the getRequiredLiveData method is removed
  protected LiveDataSourcingFunction(String uniqueId, ValueRequirement requirement, ValueSpecification specification) {
    ArgumentChecker.notNull(requirement, "Value requirement");
    ArgumentChecker.notNull(specification, "Value specification");
    assert requirement.isSatisfiedBy(specification);
    setUniqueIdentifier(uniqueId);
    _value = Pair.of(requirement, specification);
  }

  /**
   * Returns the value requirement (to be passed to a live data provider) and resultant specification to be passed
   * to dependent nodes in the graph.
   * 
   * @return the requirement and specification, not {@code null}
   */
  public Pair<ValueRequirement, ValueSpecification> getLiveDataRequirement() {
    return _value;
  }

  public ValueSpecification getResult() {
    return getLiveDataRequirement().getSecond();
  }

  @Override
  public boolean canApplyTo(FunctionCompilationContext context, ComputationTarget target) {
    // Special pseudo-function. If constructed, we apply.
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, final ValueRequirement desiredValue) {
    // None by design.
    return Collections.emptySet();
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {
    return Collections.singleton(getResult());
  }

  @Override
  public ComputationTargetType getTargetType() {
    return getLiveDataRequirement().getSecond().getTargetSpecification().getType();
  }

  @Override
  public final Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    throw new NotImplementedException("LiveDataSourcingFunction should never be executed.");
  }

  @Override
  public final Set<ValueSpecification> getRequiredLiveData() {
    throw new NotImplementedException("Deprecated method should not be called");
  }

}
