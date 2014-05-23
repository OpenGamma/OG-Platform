/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity.option;

import static com.opengamma.engine.value.ValueRequirementNames.VEGA;
import static com.opengamma.engine.value.ValueRequirementNames.WEIGHTED_VEGA;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;
import org.threeten.bp.temporal.ChronoUnit;

import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.security.Security;
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
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.OptionSecurityVisitors;
import com.opengamma.util.async.AsynchronousExecution;
import com.opengamma.util.time.Expiry;
import com.opengamma.util.time.ExpiryAccuracy;

/**
 * Calculates the weighed vega for equity options, equity index options and equity index future options.
 */
public class WeightedVegaFunction extends AbstractFunction.NonCompiledInvoker {
  /** The security types that this function can handle */
  private static final ComputationTargetType TYPE = FinancialSecurityTypes.EQUITY_OPTION_SECURITY.or(FinancialSecurityTypes.EQUITY_INDEX_OPTION_SECURITY).or(
      FinancialSecurityTypes.EQUITY_INDEX_FUTURE_OPTION_SECURITY);
  /** The number of base days */
  private static int s_baseDays = 60; // TODO - Should be property available to the user [PLAT-5521]

  @Override
  public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {

    // 1. Get Vega
    Double vega = null;
    for (final ComputedValue input : inputs.getAllValues()) {
      if (input.getSpecification().getValueName().equals(VEGA)) {
        final Object inputVal = input.getValue();
        if (inputVal != null) {
          vega = (Double) inputVal;
        } else {
          throw new OpenGammaRuntimeException("Did not satisfy requirement," + VEGA + ", for security" + target.getSecurity().toString());
        }
      }
    }
    if (vega == null) {
      throw new OpenGammaRuntimeException("Could not get vega for " + target.getSecurity());
    }

    // 2. Compute Weighted Vega

    final Security security = target.getSecurity();
    final Expiry expiry = OptionSecurityVisitors.getExpiry(security);
    if (expiry.getAccuracy().equals(ExpiryAccuracy.MONTH_YEAR) || expiry.getAccuracy().equals(ExpiryAccuracy.YEAR)) {
      throw new OpenGammaRuntimeException("Security's Expiry is not accurate to the day, which is required: " + security.toString());
    }

    final long daysToExpiry = ChronoUnit.DAYS.between(LocalDate.now(executionContext.getValuationClock()), expiry.getExpiry().toLocalDate());
    final double weighting = Math.sqrt(s_baseDays / (daysToExpiry + 1.));
    final double weightedVega = weighting * vega;

    // 3. Create specification and return
    final ValueRequirement desiredValue = desiredValues.iterator().next();
    final ValueSpecification valueSpecification = new ValueSpecification(WEIGHTED_VEGA, target.toSpecification(), desiredValue.getConstraints());
    final ComputedValue result = new ComputedValue(valueSpecification, weightedVega);
    return Sets.newHashSet(result);

  }

  @Override
  public ComputationTargetType getTargetType() {
    return TYPE;
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
    return Collections.singleton(new ValueSpecification(WEIGHTED_VEGA, target.toSpecification(), ValueProperties.all()));
  }

  @Override
  public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target, final Map<ValueSpecification, ValueRequirement> inputs) {
    final ValueSpecification vega = inputs.keySet().iterator().next();
    final ValueProperties properties = vega.getProperties().copy().withoutAny(ValuePropertyNames.FUNCTION).with(ValuePropertyNames.FUNCTION, getUniqueId()).get();
    return Collections.singleton(new ValueSpecification(WEIGHTED_VEGA, target.toSpecification(), properties));
  }

  @Override
  public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
    final ValueRequirement vegaReq = new ValueRequirement(VEGA, target.toSpecification(), desiredValue.getConstraints().withoutAny(
        ValuePropertyNames.FUNCTION));
    return Sets.newHashSet(vegaReq);
  }

}
