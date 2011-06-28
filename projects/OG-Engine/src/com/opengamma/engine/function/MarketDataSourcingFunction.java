/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import java.util.Collections;
import java.util.Set;

import org.apache.commons.lang.NotImplementedException;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.marketdata.MarketDataProvider;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.tuple.Pair;

/**
 * Special case of a function implementation that is never executed by the graph executor but
 * used to source data from a {@link MarketDataProvider}.
 */
public class MarketDataSourcingFunction extends AbstractFunction.NonCompiledInvoker {

  /**
   * Function unique ID
   */
  public static final String UNIQUE_ID = "MarketDataSourcingFunction";

  private final Pair<ValueRequirement, ValueSpecification> _value;

  public MarketDataSourcingFunction(ValueRequirement requirement) {
    ArgumentChecker.notNull(requirement, "Value Requirement");
    setUniqueId(UNIQUE_ID);
    _value = Pair.of(requirement, new ValueSpecification(requirement, getUniqueId()));
  }

  protected MarketDataSourcingFunction(String uniqueId, ValueRequirement requirement, ValueSpecification specification) {
    ArgumentChecker.notNull(requirement, "Value requirement");
    ArgumentChecker.notNull(specification, "Value specification");
    assert requirement.isSatisfiedBy(specification);
    setUniqueId(uniqueId);
    _value = Pair.of(requirement, specification);
  }

  /**
   * Returns the value requirement (to be passed to a market data provider) and resultant specification to be passed
   * to dependent nodes in the graph.
   * 
   * @return the requirement and specification, not {@code null}
   */
  public Pair<ValueRequirement, ValueSpecification> getMarketDataRequirement() {
    return _value;
  }

  public ValueSpecification getResult() {
    return getMarketDataRequirement().getSecond();
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
    return getMarketDataRequirement().getSecond().getTargetSpecification().getType();
  }

  @Override
  public final Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) {
    throw new NotImplementedException("MarketDataSourcingFunction should never be executed.");
  }

}
