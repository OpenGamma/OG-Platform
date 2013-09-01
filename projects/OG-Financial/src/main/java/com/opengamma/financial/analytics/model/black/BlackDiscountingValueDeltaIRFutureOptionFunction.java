/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValueRequirementNames.FORWARD;
import static com.opengamma.engine.value.ValueRequirementNames.POSITION_GAMMA;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_GAMMA;

import java.util.HashSet;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Calculates the value gamma of interest rate future options using a Black surface and
 * curves constructed using the discounting method.
 */
public class BlackDiscountingValueDeltaIRFutureOptionFunction extends BlackDiscountingIRFutureOptionFunction {

  /**
   * Sets the value requirement to {@link ValueRequirementNames#VALUE_GAMMA}
   */
  public BlackDiscountingValueDeltaIRFutureOptionFunction() {
    super(VALUE_GAMMA);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties constraints = desiredValue.getConstraints();
        final double positionGamma = (Double) inputs.getValue(POSITION_GAMMA);
        final double futurePrice = (Double) inputs.getValue(FORWARD);
        final double valueGamma = futurePrice * futurePrice * positionGamma / 2;
        final ValueSpecification valueSpecification = new ValueSpecification(VALUE_GAMMA, target.toSpecification(), constraints.copy().get());
        final ComputedValue result = new ComputedValue(valueSpecification, valueGamma);
        return Sets.newHashSet(result);
      }

      @Override
      public Set<ValueRequirement> getRequirements(final FunctionCompilationContext compilationContext, final ComputationTarget target,
          final ValueRequirement desiredValue) {
        if (super.getRequirements(compilationContext, target, desiredValue) == null) {
          return null;
        }
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final Set<ValueRequirement> requirements = new HashSet<>();
        requirements.add(new ValueRequirement(POSITION_GAMMA, target.toSpecification(), properties));
        requirements.add(new ValueRequirement(FORWARD, target.toSpecification(), properties));
        return requirements;
      }

    };
  }
}
