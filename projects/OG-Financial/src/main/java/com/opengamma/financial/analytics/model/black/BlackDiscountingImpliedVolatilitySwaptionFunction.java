/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.interestrate.SwaptionBlackImpliedVolatilityCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProvider;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProviderInterface;
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
 * Calculates the implied volatility of swaptions using a Black surface and
 * curves constructed using the discounting method.
 */
public class BlackDiscountingImpliedVolatilitySwaptionFunction extends BlackDiscountingSwaptionFunction {
  /** The present value calculator */
  private static final InstrumentDerivativeVisitor<BlackSwaptionFlatProviderInterface, Double> CALCULATOR =
      SwaptionBlackImpliedVolatilityCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#SECURITY_IMPLIED_VOLATILITY}
   */
  public BlackDiscountingImpliedVolatilitySwaptionFunction() {
    super(SECURITY_IMPLIED_VOLATILITY);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final BlackSwaptionFlatProvider blackData = getBlackSurface(executionContext, inputs, target, fxMatrix);
        final double impliedVol = derivative.accept(CALCULATOR, blackData);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(SECURITY_IMPLIED_VOLATILITY, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, impliedVol));
      }

    };
  }

}
