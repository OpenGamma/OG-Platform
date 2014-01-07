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

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.property.UnitProperties;

/**
 * Able to sum a particular requirement name from a set of underlying positions. If any values are not produced (because of missing market data or computation errors) a partial sum is produced.
 */
public class SummingFunction extends MissingInputsFunction {

  public static final String IGNORE_ROOT_NODE = "SummingFunction.IGNORE_ROOT_NODE";

  /**
   * The number of positions that made up the sum.
   */
  private static final String POSITION_COUNT = "PositionCount";

  /**
   * Main implementation.
   */
  protected static class Impl extends AbstractFunction.NonCompiledInvoker {

    private final String _requirementName;
    private final String[] _homogenousProperties = UnitProperties.unitPropertyNames();

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
      // Applies to any portfolio node, except the root if "Don't aggregate root node" is set
      Portfolio portfolio = context.getPortfolio();
      if (portfolio == null || portfolio.getAttributes().get(IGNORE_ROOT_NODE) == null) {
        return true;
      } else {
        return target.getPortfolioNode().getParentNodeId() != null;
      }
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
      return Collections.singleton(new ValueSpecification(getRequirementName(), target.toSpecification(), ValueProperties.all()));
    }

    @Override
    public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
      final PortfolioNode node = target.getPortfolioNode();
      final Set<ValueRequirement> requirements = new HashSet<ValueRequirement>();
      // Requirement has all constraints asked of us
      final ValueProperties.Builder resultConstraintsBuilder = desiredValue.getConstraints().copy();
      for (final String homogenousProperty : _homogenousProperties) {
        // TODO: this should probably only be optional if absent from the desired constraints
        resultConstraintsBuilder.withOptional(homogenousProperty);
      }
      ValueProperties resultConstraints = resultConstraintsBuilder.get();
      for (final Position position : node.getPositions()) {
        requirements.add(new ValueRequirement(getRequirementName(), ComputationTargetType.POSITION, position.getUniqueId().toLatest(), resultConstraints));
      }
      for (final PortfolioNode childNode : node.getChildNodes()) {
        requirements.add(new ValueRequirement(getRequirementName(), ComputationTargetType.PORTFOLIO_NODE, childNode.getUniqueId(), resultConstraints));
      }
      return requirements;
    }

    protected Object addValue(final Object previousSum, final Object currentValue) {
      return SumUtils.addValue(previousSum, currentValue, getRequirementName());
    }

    protected ValueProperties.Builder createValueProperties(final ValueProperties inputProperties, final int componentCount) {
      return inputProperties.copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).withoutAny(POSITION_COUNT)
          .with(POSITION_COUNT, Integer.toString(componentCount));
    }

    @Override
    public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
      int positionCount = 0;
      // Result properties are anything that was common to the input requirements
      ValueProperties common = null;
      final boolean[] homogenousProperties = new boolean[_homogenousProperties.length];
      for (final ValueSpecification input : inputs.keySet()) {
        final ValueProperties properties = input.getProperties();
        if (properties.isDefined(POSITION_COUNT)) {
          final String inputPositionCountValue = properties.getStrictValue(POSITION_COUNT);
          if (inputPositionCountValue != null) {
            final int inputPositionCount = Integer.parseInt(inputPositionCountValue);
            if (inputPositionCount == 0) {
              // Ignore this one
              continue;
            }
            positionCount += inputPositionCount;
          }
        } else {
          positionCount++;
        }
        if (common == null) {
          common = properties;
          for (int i = 0; i < homogenousProperties.length; i++) {
            homogenousProperties[i] = properties.isDefined(_homogenousProperties[i]);
          }
        } else {
          for (int i = 0; i < homogenousProperties.length; i++) {
            if (properties.isDefined(_homogenousProperties[i]) != homogenousProperties[i]) {
              // Either defines one of the properties that something else doesn't, or doesn't define
              // one that something else does
              return null;
            }
          }
          common = SumUtils.addProperties(common, properties);
        }
      }
      if (common == null) {
        // Can't have been any inputs - the sum will be zero with any properties the caller wants
        return Collections.singleton(new ValueSpecification(getRequirementName(), target.toSpecification(), createValueProperties().with(POSITION_COUNT, "0").get()));
      }
      for (int i = 0; i < homogenousProperties.length; i++) {
        if (homogenousProperties[i] == Boolean.TRUE) {
          if (!common.isDefined(_homogenousProperties[i])) {
            // No common intersection of values for homogenous property
            return null;
          }
        }
      }
      return Collections.singleton(new ValueSpecification(getRequirementName(), target.toSpecification(), createValueProperties(common, positionCount).get()));
    }

    @Override
    public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
      final ValueRequirement desiredValue = desiredValues.iterator().next();
      Object value = null;
      for (final ComputedValue input : inputs.getAllValues()) {
        final Object inputValue = input.getValue();
        if (inputValue instanceof String) {
          // Treat the empty string as a special case - it's used in place of 0 when there are no valid inputs
          if (((String) inputValue).length() == 0) {
            continue;
          }
        }
        value = addValue(value, input.getValue());
      }
      if (value == null) {
        // Can't have been any non-zero inputs - the sum is logical zero
        value = "";
      }
      return Collections.singleton(new ComputedValue(new ValueSpecification(getRequirementName(), target.toSpecification(), desiredValue.getConstraints()), value));
    }

  }

  public SummingFunction(final String requirementName) {
    super(Impl.of(requirementName));
  }

}
