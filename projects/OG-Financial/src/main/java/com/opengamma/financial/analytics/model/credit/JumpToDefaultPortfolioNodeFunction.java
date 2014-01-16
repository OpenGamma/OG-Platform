/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.JUMP_TO_DEFAULT;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PositionAccumulator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.StringLabelledMatrix1D;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class JumpToDefaultPortfolioNodeFunction extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(JumpToDefaultPortfolioNodeFunction.class);

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
    final int size = inputs.getAllValues().size();
    final String[] entityNames = new String[size];
    final double[] mcas = new double[size];
    final Collection<ComputedValue> values = inputs.getAllValues();
    int i = 0;
    for (final ComputedValue value : values) {
      final ValueSpecification spec = value.getSpecification();
      final Object jtdObject = value.getValue();
      if (jtdObject instanceof Double) {
//        final Currency resultCurrency = Currency.of(spec.getProperty(CURRENCY));
        final double jtd = (Double) jtdObject;
        entityNames[i] = Integer.toString(i);
        mcas[i++] = jtd;
      } else {
        s_logger.error("Jump to default value was not a double");
      }
    }
    final StringLabelledMatrix1D matrix = new StringLabelledMatrix1D(entityNames, mcas);
    final ValueSpecification spec = new ValueSpecification(JUMP_TO_DEFAULT, target.toSpecification(), desiredValue.getConstraints().copy().get());
    return Collections.singleton(new ComputedValue(spec, matrix));
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.PORTFOLIO_NODE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    final ValueProperties properties = createValueProperties()
        .withAny(CURRENCY)
        .withOptional(CURRENCY)
        .get();
    return Collections.singleton(new ValueSpecification(JUMP_TO_DEFAULT, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final PortfolioNode node = target.getPortfolioNode();
    final ValueProperties.Builder properties = ValueProperties.builder();
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> currencies = constraints.getValues(CURRENCY);
    if (currencies != null && currencies.size() == 1) {
      properties.with(CURRENCY, currencies);
    }
    final Set<Position> positions = PositionAccumulator.getAccumulatedPositions(node);
    final Set<ValueRequirement> requirements = Sets.newHashSetWithExpectedSize(positions.size());
    for (final Position position : positions) {
      requirements.add(new ValueRequirement(JUMP_TO_DEFAULT, ComputationTargetSpecification.of(position), properties.get()));
    }
    return requirements;
  }

  /**
   * Jump to default is valid only for certain security types. By allowing missing requirements, someone
   * extending the list of relevant types does not have to remember to add logic to only ask for JtD on
   * those types and does not have to explore the portfolio structure to eliminate irrelevant trade types
   * in getRequirements().
   * {@inheritDoc}
   */
  @Override
  public boolean canHandleMissingRequirements() {
    return true;
  }
}
