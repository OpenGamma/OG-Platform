/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_GAMMA_P;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.blackforex.ValueGammaSpotForexBlackSmileCalculator;
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
import com.opengamma.util.money.CurrencyAmount;

/**
 * Calculates the value gamma multiplied by the spot (which produces the change of delta for a relative increase of e of the spot rate (from X to X(1+e)))
 * of FX options using a Black surface and curves constructed using the discounting method.
 */
public class BlackDiscountingValueGammaSpotFXOptionFunction extends BlackDiscountingFXOptionFunction {
  /** The value gamma spot calculator */
  private static final InstrumentDerivativeVisitor<BlackForexSmileProviderInterface, CurrencyAmount> CALCULATOR =
      ValueGammaSpotForexBlackSmileCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#VALUE_GAMMA_P}
   */
  public BlackDiscountingValueGammaSpotFXOptionFunction() {
    super(VALUE_GAMMA_P);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final BlackForexSmileProvider blackData = getBlackSurface(executionContext, inputs, target, fxMatrix);
        final CurrencyAmount valueGamma = derivative.accept(CALCULATOR, blackData);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final String currency = Iterables.getOnlyElement(properties.getValues(CURRENCY));
        if (!currency.equals(valueGamma.getCurrency().getCode())) {
          throw new OpenGammaRuntimeException("Currency of result " + valueGamma.getCurrency() + " did not match" +
              " the expected currency " + currency);
        }
        final ValueSpecification spec = new ValueSpecification(VALUE_GAMMA_P, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, valueGamma.getAmount()));
      }

    };
  }
}
