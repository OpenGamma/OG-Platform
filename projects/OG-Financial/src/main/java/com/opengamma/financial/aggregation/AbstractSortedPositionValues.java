/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Base class for functions common to {@link SortedPositionValues} and {@link SlicedPositionValues}.
 */
public abstract class AbstractSortedPositionValues extends AbstractFunction.NonCompiledInvoker {

  /**
   * Property name identifying the value that was produced from the positions.
   */
  public static final String VALUE_NAME_PROPERTY = "Value";

  protected static String getConstraint(final ValueProperties constraints, final String name) {
    final Set<String> values = constraints.getValues(name);
    if ((values == null) || (values.size() != 1)) {
      return null;
    }
    return values.iterator().next();
  }

  protected static Integer getIntegerConstraint(final ValueProperties constraints, final String name) {
    final Set<String> values = constraints.getValues(name);
    if ((values == null) || (values.size() != 1)) {
      return null;
    }
    return Integer.parseInt(values.iterator().next());
  }

  protected abstract String getValueName();

  protected abstract Set<ValueRequirement> getRequirements(String valueName, ComputationTarget target, ValueProperties constraints);

  protected ValueProperties.Builder createRawResultsProperties() {
    return createValueProperties();
  }

  protected abstract void composeValueProperties(ValueProperties.Builder builder, ValueSpecification inputValue);

  // AbstractFunction

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return true;
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueProperties constraints = desiredValue.getConstraints();
    final String valueName = SortedPositionValues.getConstraint(constraints, VALUE_NAME_PROPERTY);
    if (valueName == null) {
      return null;
    }
    return getRequirements(valueName, target, constraints);
  }

  /**
   * Initial result set is anything. All behavior is controlled by constraints on the desired value.
   * 
   * @param context the function compilation context
   * @param target the computation target 
   * @return a single element set containing the result with the infinite property set
   */
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(ValueSpecification.of(getValueName(), ComputationTargetType.PORTFOLIO_NODE, target.getUniqueId(), ValueProperties.all()));
  }

  /**
   * Resolved output is the value and properties from the input values. Properties from the underlying input values are prefixed with the
   * value name, e.g. "FairValue.Currency" to avoid any potential collisions with properties defined on this function.
   * 
   * @param context the function compilation context
   * @param target the computation target
   * @param inputs the resolved inputs
   * @return a single element set containing the result with the properties from the component positions
   */
  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueProperties.Builder properties = createRawResultsProperties();
    for (ValueSpecification inputValue : inputs.keySet()) {
      composeValueProperties(properties, inputValue);
    }
    return Collections.singleton(ValueSpecification.of(getValueName(), ComputationTargetType.PORTFOLIO_NODE, target.getUniqueId(), properties.get()));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

}
