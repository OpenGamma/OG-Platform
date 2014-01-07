/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics;

import static com.opengamma.engine.value.ValueRequirementNames.DV01;
import static com.opengamma.engine.value.ValueRequirementNames.PV01;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.AbstractFunction;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Able to scale values produced by the rest of the OG-Financial package.
 */
public class DV01Function extends AbstractFunction.NonCompiledInvoker {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(DV01Function.class);

  @Override
  public String getShortName() {
    return "DV01Function";
  }

  @Override
  public ComputationTargetType getTargetType() {
    return ComputationTargetType.POSITION;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(DV01, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final Position position = target.getPosition();
    final ValueRequirement requirement = new ValueRequirement(PV01, ComputationTargetType.POSITION, position.getUniqueId(), desiredValue.getConstraints().withoutAny(
        ValuePropertyNames.FUNCTION).withoutAny(ValuePropertyNames.SHIFT));
    return Collections.singleton(requirement);
  }

  /**
   * Replaces the {@link ValuePropertyNames#FUNCTION} property.
   * @param input The input
   * @return The result properties
   */
  protected ValueProperties getResultProperties(final ValueSpecification input) {
    return input.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).withAny(ValuePropertyNames.SHIFT).get();
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification input = inputs.keySet().iterator().next();
    final ValueSpecification specification = new ValueSpecification(DV01, target.toSpecification(), getResultProperties(input));
    return Collections.singleton(specification);
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ComputedValue input = inputs.getAllValues().iterator().next();
    final Object value = input.getValue();
    final ValueSpecification specification = new ValueSpecification(DV01, target.toSpecification(), desiredValue.getConstraints());
    ComputedValue scaledValue = null;
    double shift;
    if (value instanceof Double) {
      Double doubleValue = (Double) value;
      final Set<String> shiftProperties = desiredValue.getConstraints().getValues(ValuePropertyNames.SHIFT);
      if (shiftProperties != null && shiftProperties.size() == 1) {
        final String shiftStr = Iterables.getOnlyElement(shiftProperties);
        try {
          shift = Double.parseDouble(shiftStr);
        } catch (final NumberFormatException nfe) {
          s_logger.error("Constraint Shift on DV01 not a value double, defaulting to 1d");
          shift = 1d;
        }
      } else {
        shift = 1d;
      }
      doubleValue *= shift;
      scaledValue = new ComputedValue(specification, doubleValue);
    } else {
      s_logger.error("DV01 is non numeric type {}, value {}", value.getClass(), value);
    }
    return Collections.singleton(scaledValue);
  }

}
