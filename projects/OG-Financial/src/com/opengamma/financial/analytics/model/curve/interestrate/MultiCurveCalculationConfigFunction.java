/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.interestrate;

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
import com.opengamma.financial.OpenGammaExecutionContext;
import com.opengamma.financial.analytics.ircurve.calcconfig.CurveCalculationConfigSource;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;

/**
 * 
 */
public class MultiCurveCalculationConfigFunction extends AbstractFunction {

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final InstantProvider atInstantProvider) {
    final ZonedDateTime atInstant = ZonedDateTime.ofInstant(atInstantProvider, TimeZone.UTC);
    return new AbstractInvokingCompiledFunction(atInstant.withTime(0, 0), atInstant.plusDays(1).withTime(0, 0).minusNanos(1000000)) {

      @Override
      public ComputationTargetType getTargetType() {
        return ComputationTargetType.PRIMITIVE;
      }

      @Override
      public Set<ValueSpecification> getResults(final FunctionCompilationContext context, final ComputationTarget target) {
        final ValueProperties properties = createValueProperties()
            .withAny(ValuePropertyNames.CURVE_CALCULATION_CONFIG).get();
        final ValueSpecification spec = new ValueSpecification(ValueRequirementNames.CURVE_CALCULATION_CONFIG, target.toSpecification(), properties);
        return Collections.singleton(spec);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext context, final ComputationTarget target, final ValueRequirement desiredValue) {
        return Collections.emptySet();
      }

      @Override
      public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
        return true;
      }

      @Override
      public Set<ComputedValue> execute(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target, final Set<ValueRequirement> desiredValues) {
        final CurveCalculationConfigSource source = OpenGammaExecutionContext.getCurveCalculationConfigSource(executionContext);
        final ValueRequirement desiredValue = desiredValues.iterator().next();
        final String curveConfigName = desiredValue.getConstraint(ValuePropertyNames.CURVE_CALCULATION_CONFIG);
        final MultiCurveCalculationConfig config = source.getConfig(curveConfigName, atInstant);
        final ValueProperties properties = createValueProperties()
            .with(ValuePropertyNames.CURVE_CALCULATION_CONFIG, curveConfigName).get();
        return Collections.singleton(new ComputedValue(new ValueSpecification(ValueRequirementNames.CURVE_CALCULATION_CONFIG, target.toSpecification(), properties), config));
      }
    };
  }

}
