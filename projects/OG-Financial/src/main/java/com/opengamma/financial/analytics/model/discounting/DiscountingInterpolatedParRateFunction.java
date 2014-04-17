/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.discounting;

import static com.opengamma.engine.value.ValueRequirementNames.PAR_RATE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.ParRateDiscountingCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.MulticurveProviderInterface;
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
 *  Calculates the par rate of instruments using curves constructed using
 * the interpolated method.
 */
public class DiscountingInterpolatedParRateFunction extends DiscountingInterpolatedFunction {
  /** The par rate calculator */
  private static final InstrumentDerivativeVisitor<MulticurveProviderInterface, Double> CALCULATOR = ParRateDiscountingCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#PAR_RATE}
   */
  public DiscountingInterpolatedParRateFunction() {
    super(PAR_RATE);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new DiscountingInterpolatedCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final MulticurveProviderInterface data = getMergedProviders(inputs, fxMatrix);
        final Double amount = derivative.accept(CALCULATOR, data);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(PAR_RATE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, amount));
      }

    };
  }
}
