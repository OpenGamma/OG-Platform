/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VEGA;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.blackstirfutures.PresentValueBlackSensitivityBlackSTIRFutureOptionCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSTIRFuturesProviderInterface;
import com.opengamma.analytics.util.amount.SurfaceValue;
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
 * Calculates the value vega of interest rate future options using a Black surface and
 * curves constructed using the discounting method.
 */
public class BlackDiscountingValueVegaIRFutureOptionFunction extends BlackDiscountingIRFutureOptionFunction {
  /** The value vega calculator */
  private static final InstrumentDerivativeVisitor<BlackSTIRFuturesProviderInterface, SurfaceValue> CALCULATOR =
      PresentValueBlackSensitivityBlackSTIRFutureOptionCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#VALUE_VEGA}
   */
  public BlackDiscountingValueVegaIRFutureOptionFunction() {
    super(VALUE_VEGA);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final BlackSTIRFuturesProviderInterface blackData = getBlackSurface(executionContext, inputs, target, fxMatrix);
        final SurfaceValue sensitivities = derivative.accept(CALCULATOR, blackData);
        final double vega = sensitivities.toSingleValue();
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(VALUE_VEGA, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, vega));
      }

    };
  }

}
