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
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.value.MarketDataRequirementNames;
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
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.future.FutureSecurity;
import com.opengamma.financial.security.future.InterestRateFutureSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 * Calculates the Value (or Dollar) Delta of a FutureSecurity. The value delta is defined as the Delta (dV/dS) multiplied by the spot, S. As dS/dS == 1, ValueDelta = S, the spot value of the security.
 * ValueDelta can be roughly described as the delta hedge of the position expressed in currency value. It indicates how much currency must be used in order to delta hedge a position.
 */
public class FutureSecurityValueDeltaFunction extends AbstractFunction.NonCompiledInvoker {

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FUTURE_SECURITY;
  }

  @Override
  public Set<ValueSpecification> getResults(FunctionCompilationContext context, ComputationTarget target) {

    ValueProperties.Builder properties = createValueProperties().with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());

    if (target.getSecurity() instanceof InterestRateFutureSecurity) {
      properties.withAny(ValuePropertyNames.SCALE);
    }

    return Collections.singleton(new ValueSpecification(ValueRequirementNames.VALUE_DELTA, target.toSpecification(), properties.get()));
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
    return Collections.singleton(new ValueRequirement(MarketDataRequirementNames.MARKET_VALUE, getTargetType(), target.getUniqueId()));
  }

  @Override
  public Set<ComputedValue> execute(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
    FutureSecurity security = (FutureSecurity) target.getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    ValueProperties.Builder properties = desiredValue.getConstraints().copy()
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(security).getCode());

    String scaleProperty = Double.toString(1);
    double scaleFactor = 1.0;
    if (target.getSecurity() instanceof InterestRateFutureSecurity) {
      // Add scaling and adjust properties to reflect
      final Set<String> scaleValue = desiredValue.getConstraints().getValues(ValuePropertyNames.SCALE);
      if (scaleValue != null && scaleValue.size() > 0) {
        scaleProperty = Iterables.getOnlyElement(scaleValue);
        scaleFactor = Double.parseDouble(scaleProperty);
      }
      properties = properties.withoutAny(ValuePropertyNames.SCALE).with(ValuePropertyNames.SCALE, scaleProperty);
    }

    final ValueSpecification valueSpecification = new ValueSpecification(ValueRequirementNames.VALUE_DELTA, target.toSpecification(), properties.get());

    // Get Market Value
    final Object marketValueObject = inputs.getValue(MarketDataRequirementNames.MARKET_VALUE);
    if (marketValueObject == null) {
      throw new OpenGammaRuntimeException("Could not get market value");
    }
    final Double marketValue = (Double) marketValueObject;

    final ComputedValue result = new ComputedValue(valueSpecification, scaleFactor * marketValue * security.getUnitAmount());
    return Collections.singleton(result);
  }

  private static final Logger s_logger = LoggerFactory.getLogger(FutureSecurityValueDeltaFunction.class);
}
