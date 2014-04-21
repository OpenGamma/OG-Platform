/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.future;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Provides sensitivity of FutureSecurity price with respect to itself, i.e. always unity. This is essential in order to show aggregate position in this underlying in a derivatives portfolio.
 * 
 * @author casey
 */
public class FutureSecurityDeltaFunction extends AbstractFunction.NonCompiledInvoker {

  private String getValueRequirementName() {
    return ValueRequirementNames.DELTA;
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    ValueProperties properties = desiredValue.getConstraints();
    String scaleProperty = Double.toString(1);
    double scaleFactor = 1.0;
    if (target.getSecurity() instanceof InterestRateFutureSecurity) {
      // Add scaling and adjust properties to reflect
      final Set<String> scaleValue = desiredValue.getConstraints().getValues(ValuePropertyNames.SCALE);
      if (scaleValue != null && scaleValue.size() > 0) {
        scaleProperty = Iterables.getOnlyElement(scaleValue);
        scaleFactor = Double.parseDouble(scaleProperty);
      }
      properties = properties.copy().withoutAny(ValuePropertyNames.SCALE).with(ValuePropertyNames.SCALE, scaleProperty).get();
    }
    final ValueSpecification valueSpecification = new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties);
    final ComputedValue result = new ComputedValue(valueSpecification, scaleFactor);
    return Sets.newHashSet(result);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FUTURE_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {

    ValueProperties properties = (target.getSecurity() instanceof InterestRateFutureSecurity) ?
        createValueProperties().withAny(ValuePropertyNames.SCALE).get() : createValueProperties().get();

    return Collections.singleton(new ValueSpecification(getValueRequirementName(), target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(FunctionCompilationContext context, ComputationTarget target, ValueRequirement desiredValue) {
    if (target.getSecurity() instanceof InterestRateFutureSecurity) {
      // Confirm Scale is set, by user or by default
      final ValueProperties constraints = desiredValue.getConstraints();
      final Set<String> scale = constraints.getValues(ValuePropertyNames.SCALE);
      if (scale == null || scale.size() != 1) {
        s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.SCALE);
        return null;
      }
    }
    return Collections.emptySet();
  }

  private static final Logger s_logger = LoggerFactory.getLogger(FutureSecurityDeltaFunction.class);
}
