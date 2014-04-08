/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.carrlee;

import static com.opengamma.engine.value.ValueRequirementNames.FAIR_VALUE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.analytics.financial.volatilityswap.CarrLeeFXVolatilitySwapCalculator;
import com.opengamma.analytics.financial.volatilityswap.VolatilitySwapCalculatorResult;
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
 * Calculates the fair strike of a FX volatility swap using the Carr-Lee model.
 */
public class CarrLeeFairValueFXVolatilitySwapFunction extends CarrLeeFXVolatilitySwapFunction {
  /** The fair value calculator */
  private static final InstrumentDerivativeVisitor<CarrLeeFXData, VolatilitySwapCalculatorResult> CALCULATOR = new CarrLeeFXVolatilitySwapCalculator();
  /**
   * Sets the value requirement to {@link ValueRequirementNames#FAIR_VALUE}.
   */
  public CarrLeeFairValueFXVolatilitySwapFunction() {
    super(FAIR_VALUE);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new CarrLeeFXVolatilitySwapCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final CarrLeeFXData data = getCarrLeeData(executionContext, inputs, target, fxMatrix);
        final VolatilitySwapCalculatorResult result = derivative.accept(CALCULATOR, data);
        final double fairValue = result.getFairValue();
        final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(FAIR_VALUE, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, fairValue));
      }

    };
  }
}
