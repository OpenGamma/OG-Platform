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

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionAccumulator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
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
public class SummingFunction extends MissingInputsFunction {

  /**
   * Main implementation.
   */
  protected static class Impl extends AbstractFunction.NonCompiledInvoker {

    private final String _requirementName;

    protected Impl(final String requirementName) {
      _requirementName = requirementName;
    }

    private static CompiledFunctionDefinition of(final String requirementName) {
      return new Impl(requirementName);
    }

    protected String getRequirementName() {
      return _requirementName;
    }

    @Override
    public String getShortName() {
      return "Sum(" + _requirementName + ")";
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
      // Requirement has all constraints asked of us
      final ValueProperties resultConstraints = desiredValue.getConstraints();
      final PortfolioNode node = target.getPortfolioNode();
      final Set<Position> allPositions = PositionAccumulator.getAccumulatedPositions(node);
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      for (final Position position : allPositions) {
        requirements.add(new ValueRequirement(getRequirementName(), ComputationTargetType.POSITION, position.getUniqueId(), resultConstraints));
      }
      return requirements;
    }

    protected Object addValue(final Object previousSum, final Object currentValue) {
      return SumUtils.addValue(previousSum, currentValue, getRequirementName());
    }

    protected ValueProperties.Builder createValueProperties(final ValueProperties inputProperties) {
      return inputProperties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId());
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      // Result properties are anything that was common to the input requirements
      ValueProperties common = null;
      for (ValueSpecification input : inputs.keySet()) {
        common = SumUtils.addProperties(common, input.getProperties());
      }
      if (common == null) {
        // Can't have been any inputs ... ?
        return null;
      }
      return Collections.singleton(new ValueSpecification(getRequirementName(), target.toSpecification(), createValueProperties(common).get()));
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      Object value = null;
      ValueProperties properties = null;
      for (ComputedValue input : inputs.getAllValues()) {
        value = addValue(value, input.getValue());
        properties = SumUtils.addProperties(properties, input.getSpecification().getProperties());
      }
      if (properties == null) {
        // Can't have been any inputs ... ?
        return null;
      }
      for (ValueSpecification input : inputs.getMissingValues()) {
        properties = SumUtils.addProperties(properties, input.getProperties());
      }
      return Collections.singleton(new ComputedValue(new ValueSpecification(getRequirementName(), target.toSpecification(), createValueProperties(properties).get()), value));
    }

  }

  public SummingFunction(final String requirementName) {
    super(Impl.of(requirementName));
  }

}
