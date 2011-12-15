/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Sets;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionAccumulator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Able to sum a particular requirement name from a set of underlying positions.
 * If any values are not produced (because of missing market data or computation
 * errors) a partial sum is produced.
 */
public class MissingInputSummingFunction extends AbstractFunction.NonCompiledInvoker {

  // TODO: this could be written to replace the standard summing function as it is
  // capable of producing both sets of outputs. The FilteredSummingFunction can then
  // extend from this to get similar "missing" behavior.
  
  /**
   * Value of the {@link ValuePropertyNames#AGGREGATION} property set on the
   * output produced. This must be explicitly requested if this function is to
   * be used. Default system behavior favors the conventional aggregates
   * that <em>fail to produce output</em> if there are computation errors.
   */
  public static final String AGGREGATION_STYLE_MISSING = "MissingInputs";
  
  private final String _requirementName;

  public MissingInputSummingFunction(final String requirementName) {
    _requirementName = requirementName;
  }

  protected String getRequirementName() {
    return _requirementName;
  }
  
  @Override
  public String getShortName() {
    return "MissingSum(" + _requirementName + ")";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    // Applies to any portfolio node
    return true;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(getRequirementName(), target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // User must have requested our aggregation style
    final ValueProperties resultConstraints = desiredValue.getConstraints();
    final Set<String> aggregationStyle = resultConstraints.getValues(ValuePropertyNames.AGGREGATION);
    if ((aggregationStyle == null) || !aggregationStyle.contains(AGGREGATION_STYLE_MISSING)) {
      return null;
    }
    // Requirement has all constraints asked of us (minus the aggregation style)
    final ValueProperties requirementConstraints = resultConstraints.withoutAny(ValuePropertyNames.AGGREGATION);
    final PortfolioNode node = target.getPortfolioNode();
    final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
    final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
    for (final Position position : allPositions) {
      requirements.add(new ValueRequirement(getRequirementName(), ComputationTargetType.POSITION, position.getUniqueId(), requirementConstraints));
    }
    return requirements;
  }

  protected Object addValue(final Object previousSum, final Object currentValue) {
    return SumUtils.addValue(previousSum, currentValue, getRequirementName());
  }

  protected ValueProperties addProperties(final ValueProperties previousIntersection, final ValueProperties currentProperties) {
    if (previousIntersection == null) {
      return currentProperties;
    } else {
      return previousIntersection.intersect(currentProperties);
    }
  }

  protected ValueProperties.Builder createValueProperties(final ValueProperties inputProperties) {
    return inputProperties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId());
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    // Result properties are anything that was common to the input requirements
    ValueProperties common = null;
    for (ValueSpecification input : inputs.keySet()) {
      common = addProperties(common, input.getProperties());
    }
    if (common == null) {
      // Can't have been any inputs ... ?
      return null;
    }
    final ValueProperties.Builder outputProperties = createValueProperties(common);
    // Two outputs are possible - the full output and the "missing" output version
    final Set<ValueSpecification> results = Sets.newHashSetWithExpectedSize(2);
    outputProperties.with(ValuePropertyNames.AGGREGATION, AGGREGATION_STYLE_MISSING);
    results.add(new ValueSpecification(getRequirementName(), target.toSpecification(), outputProperties.get()));
    outputProperties.withoutAny(ValuePropertyNames.AGGREGATION).with(ValuePropertyNames.AGGREGATION, SummingFunction.AGGREGATION_STYLE_FULL);
    results.add(new ValueSpecification(getRequirementName(), target.toSpecification(), outputProperties.get()));
    return results;
  }

  @Override
  public boolean canHandleMissingInputs() {
    return true;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    Object value = null;
    ValueProperties properties = null;
    for (ComputedValue input : inputs.getAllValues()) {
      value = addValue(value, input.getValue());
      properties = addProperties(properties, input.getSpecification().getProperties());
    }
    if (properties == null) {
      // Can't have been any inputs ... ?
      return null;
    }
    // Produce the outputs requested
    final Set<ComputedValue> results = Sets.newHashSetWithExpectedSize(desiredValues.size());
    final ValueProperties.Builder resultProperties = createValueProperties(properties);
    final ComputationTargetSpecification targetSpecification = target.toSpecification();
    for (ValueRequirement desiredValue : desiredValues) {
      final String aggregation = desiredValue.getConstraint(ValuePropertyNames.AGGREGATION);
      if (AGGREGATION_STYLE_MISSING.equals(aggregation)) {
        results.add(new ComputedValue(new ValueSpecification(getRequirementName(), targetSpecification, resultProperties.withoutAny(ValuePropertyNames.AGGREGATION)
            .with(ValuePropertyNames.AGGREGATION, aggregation).get()), value));
      } else if (SummingFunction.AGGREGATION_STYLE_FULL.equals(aggregation)) {
        if (inputs.getMissingValues().isEmpty()) {
          // Can only produce this output if there were no missing inputs
          results.add(new ComputedValue(new ValueSpecification(getRequirementName(), targetSpecification, resultProperties.withoutAny(ValuePropertyNames.AGGREGATION)
              .with(ValuePropertyNames.AGGREGATION, aggregation).get()), value));
        }
      }
    }
    return results;
  }

}
