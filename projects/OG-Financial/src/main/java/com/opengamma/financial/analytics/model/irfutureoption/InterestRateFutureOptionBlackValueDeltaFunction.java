/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingValueDeltaIRFutureOptionFunction;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;

/**
 * Calculates the value delta of an {@link IRFutureOptionSecurity} using the Black Delta as input.
 * <p>
 * See {@link InterestRateFutureOptionBlackPositionDeltaFunction}
 *
 * @deprecated Use {@link BlackDiscountingValueDeltaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackValueDeltaFunction extends InterestRateFutureOptionBlackFunction {
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackValueDeltaFunction.class);

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_DELTA}
   */
  public InterestRateFutureOptionBlackValueDeltaFunction() {
    super(ValueRequirementNames.VALUE_DELTA, true);
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    // First, confirm Scale is set, by user or by default
    final ValueProperties constraints = desiredValue.getConstraints();
    final Set<String> scale = constraints.getValues(ValuePropertyNames.SCALE);
    if (scale != null) {
      // changed because want to use a default directly in the function
      if (scale.size() != 1) {
        s_logger.info("Could not find {} requirement. Looking for a default..", ValuePropertyNames.SCALE);
        return null;
      }
    }
    final Set<ValueRequirement> requirements = super.getRequirements(context, target, desiredValue);
    if (requirements == null) {
      return null;
    }
    requirements.add(new ValueRequirement(ValueRequirementNames.POSITION_DELTA, target.toSpecification(), constraints));
    requirements.add(new ValueRequirement(ValueRequirementNames.FORWARD, target.toSpecification(), constraints.withoutAny(ValuePropertyNames.SCALE)));
    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
    // Build output specification.
    // TODO This is going to be a copy of the spec of the delta!!!
    final IRFutureOptionSecurity security = (IRFutureOptionSecurity) target.getTrade().getSecurity();
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.VALUE_DELTA, target.toSpecification(), desiredValue.getConstraints());

    // Get inputs and compute output
    final Object deltaObject = inputs.getValue(ValueRequirementNames.POSITION_DELTA);
    if (deltaObject == null) {
      throw new OpenGammaRuntimeException("Could not get PositionDelta for " + security.getUnderlyingId());
    }
    final Double positionDelta = (Double) deltaObject;

    final Object futureObject = inputs.getValue(ValueRequirementNames.FORWARD);
    if (futureObject == null) {
      throw new OpenGammaRuntimeException("Could not get Forward for " + security.getUnderlyingId());
    }
    final Double futurePrice = (Double) futureObject;

    final Double valueDelta = futurePrice * positionDelta;
    return Collections.singleton(new ComputedValue(spec, valueDelta));
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data,
      final ValueSpecification spec, final Set<ValueRequirement> desiredValues) {
    throw new OpenGammaRuntimeException("Should never get called");
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String currency) {
    return super.getResultProperties(currency)
        .withAny(ValuePropertyNames.SCALE);
  }

}
