/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValueRequirementNames.GAMMA;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.blackforex.SpotGammaForexBlackSmileCalculator;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProvider;
import com.opengamma.analytics.financial.provider.description.forex.BlackForexSmileProviderInterface;
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
 * Calculates the spot gamma of FX options using a Black surface and
 * curves constructed using the discounting method.
 */
public class BlackDiscountingSpotGammaFXOptionFunction extends BlackDiscountingFXOptionFunction {
  /** The spot gamma calculator */
  private static final InstrumentDerivativeVisitor<BlackForexSmileProviderInterface, Double> CALCULATOR =
      SpotGammaForexBlackSmileCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#GAMMA}
   */
  public BlackDiscountingSpotGammaFXOptionFunction() {
    super(GAMMA);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final BlackForexSmileProvider blackData = getBlackSurface(executionContext, inputs, target, fxMatrix);
        final double gamma = derivative.accept(CALCULATOR, blackData);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(GAMMA, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, gamma));
      }

    };
  }
}
