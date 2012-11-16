/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.credit;

import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER;
import static com.opengamma.financial.analytics.model.credit.CreditInstrumentPropertyNamesAndValues.PROPERTY_HAZARD_RATE_CURVE_TOLERANCE;

import java.util.Collections;
import java.util.Set;

import javax.time.InstantProvider;
import javax.time.calendar.TimeZone;
import javax.time.calendar.ZonedDateTime;

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
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.cds.LegacyVanillaCDSSecurity;
import com.opengamma.util.async.AsynchronousExecution;

/**
 *
 */
public class ISDAHazardCurveFunction extends AbstractFunction {

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext compilationContext, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues) throws AsynchronousExecution {
        return null;
      }

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.SECURITY;
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        if (target.getType() != ComputationTargetType.SECURITY) {
          return false;
        }
        return target.getSecurity() instanceof LegacyVanillaCDSSecurity;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        @SuppressWarnings("synthetic-access")
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE)
            .with(PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS)
            .with(PROPERTY_HAZARD_RATE_CURVE_TOLERANCE)
            .with(PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER).get();
        return Collections.singleton(new ValueSpecification(ValueRequirementNames.HAZARD_RATE_CURVE, target.toSpecification(), properties));
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        final ValueProperties constraints = desiredValue.getConstraints();
        final Set<String> curveNames = constraints.getValues(ValuePropertyNames.CURVE);
        if (curveNames == null || curveNames.size() != 1) {
          return null;
        }
        final Set<String> nIterationsName = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_N_ITERATIONS);
        if (nIterationsName == null || nIterationsName.size() != 1) {
          return null;
        }
        final Set<String> tolerances = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_TOLERANCE);
        if (tolerances == null || tolerances.size() != 1) {
          return null;
        }
        final Set<String> rangeMultipliers = constraints.getValues(PROPERTY_HAZARD_RATE_CURVE_RANGE_MULTIPLIER);
        if (rangeMultipliers == null || rangeMultipliers.size() != 1) {
          return null;
        }
        return null;
      }

    };
  }

}
