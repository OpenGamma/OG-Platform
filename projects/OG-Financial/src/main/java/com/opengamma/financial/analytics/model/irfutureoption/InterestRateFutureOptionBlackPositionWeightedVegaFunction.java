/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingWeightedVegaIRFutureOptionFunction;
import com.opengamma.financial.security.option.IRFutureOptionSecurity;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Function computes {@link ValueRequirementNames#POSITION_WEIGHTED_VEGA}, taking as input {@link ValueRequirementNames#POSITION_VEGA},
 * for interest rate future options in the Black world.
 * @deprecated Use {@link BlackDiscountingWeightedVegaIRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackPositionWeightedVegaFunction extends InterestRateFutureOptionBlackFunction {
  /** The base number of days to use */
  private static int s_baseDays = 90; // TODO - Should be property available to the user
  /** The logger */
  private static final Logger s_logger = LoggerFactory.getLogger(InterestRateFutureOptionBlackPositionWeightedVegaFunction.class);

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#POSITION_WEIGHTED_VEGA}
   */
  public InterestRateFutureOptionBlackPositionWeightedVegaFunction() {
    super(ValueRequirementNames.POSITION_WEIGHTED_VEGA, true);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final ValueSpecification spec,
      final Set<ValueRequirement> desiredValues) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueRequirement vegaReq = new ValueRequirement(ValueRequirementNames.POSITION_VEGA, target.toSpecification(), desiredValue.getConstraints().withoutAny(
        ValuePropertyNames.FUNCTION));
    final Set<ValueRequirement> requirements = Sets.newHashSet(vegaReq);
    return requirements;
  }

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {

    // 1. Get Vega
    Double vega = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      if (input.getSpecification().getValueName().equals(ValueRequirementNames.POSITION_VEGA)) {
        final Object inputVal = input.getValue();
        if (inputVal != null) {
          vega = (Double) inputVal;
        } else {
          s_logger.error("Did not satisfy requirement," + ValueRequirementNames.POSITION_VEGA + ", for security" + target.getTrade().getSecurity().toString());
        }
      }
    }
    if (vega == null) {
      throw new OpenGammaRuntimeException("Could not get vega for " + target);
    }
    // 2. Compute Weighted Vega
    final IRFutureOptionSecurity security = (IRFutureOptionSecurity) target.getTrade().getSecurity();
    final Expiry expiry = security.getExpiry();

    if (expiry.getAccuracy().equals(ExpiryAccuracy.MONTH_YEAR) || expiry.getAccuracy().equals(ExpiryAccuracy.YEAR)) {
      throw new OpenGammaRuntimeException("Security's Expiry is not accurate to the day, which is required: " + security.toString());
    }

    final long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(executionContext.getValuationClock()), expiry.getExpiry().toLocalDate());
    final double weighting = Math.sqrt(s_baseDays / Math.max(daysToExpiry, 1.0));
    final double weightedVega = weighting * vega;

    // 3. Create specification and return
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(ValueRequirementNames.POSITION_WEIGHTED_VEGA, target.toSpecification(), desiredValue.getConstraints());
    final ComputedValue result = new ComputedValue(valueSpecification, weightedVega);
    return Sets.newHashSet(result);
  }
}
