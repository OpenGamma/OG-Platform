/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValueRequirementNames.IMPLIED_VOLATILITY;
import static com.opengamma.engine.value.ValueRequirementNames.SECURITY_IMPLIED_VOLATILITY;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.provider.description.interestrate.BlackSwaptionFlatProvider;
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
 * Function that returns the implied volatility of a swaption. There are no volatility modelling
 * assumptions made; the implied volatility is read directly from the market data system.
 */
public class ConstantBlackDiscountingImpliedVolatilitySwaptionFunction extends ConstantBlackDiscountingSwaptionFunction {

  /**
   * Sets the value requirement to {@link ValueRequirementNames#SECURITY_IMPLIED_VOLATILITY}
   */
  public ConstantBlackDiscountingImpliedVolatilitySwaptionFunction() {
    super(SECURITY_IMPLIED_VOLATILITY);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final BlackSwaptionFlatProvider blackData = getSwaptionBlackSurface(executionContext, inputs, target, fxMatrix);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final ValueSpecification spec = new ValueSpecification(IMPLIED_VOLATILITY, target.toSpecification(), properties);
        final Double impliedVolatility = blackData.getBlackParameters().getVolatility(0, 0);
        return Collections.singleton(new ComputedValue(spec, impliedVolatility));
      }

    };
  }
}
