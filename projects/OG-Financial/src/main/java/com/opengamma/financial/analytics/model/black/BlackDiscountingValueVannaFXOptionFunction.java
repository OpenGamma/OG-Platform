/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.black;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.VALUE_VANNA;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.blackforex.ValueVannaForexBlackSmileCalculator;
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
 * Calculates the value vanna (second order cross-derivative of the spot and implied volatility)
 * of FX options using a Black surface and curves constructed using the discounting method.
 */
public class BlackDiscountingValueVannaFXOptionFunction extends BlackDiscountingFXOptionFunction {
  /** The value vanna calculator */
  private static final InstrumentDerivativeVisitor<BlackForexSmileProviderInterface, CurrencyAmount> CALCULATOR =
      ValueVannaForexBlackSmileCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#VALUE_VANNA}
   */
  public BlackDiscountingValueVannaFXOptionFunction() {
    super(VALUE_VANNA);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new BlackDiscountingCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), true) {

      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs,
          final ComputationTarget target, final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative,
          final FXMatrix fxMatrix) {
        final BlackForexSmileProvider blackData = getBlackSurface(executionContext, inputs, target, fxMatrix);
        final CurrencyAmount valueVanna = derivative.accept(CALCULATOR, blackData);
        final ValueRequirement desiredValue = Iterables.getOnlyElement(desiredValues);
        final ValueProperties properties = desiredValue.getConstraints().copy().get();
        final String currency = Iterables.getOnlyElement(properties.getValues(CURRENCY));
        if (!currency.equals(valueVanna.getCurrency().getCode())) {
          throw new OpenGammaRuntimeException("Currency of result " + valueVanna.getCurrency() + " did not match" +
              " the expected currency " + currency);
        }
        final ValueSpecification spec = new ValueSpecification(VALUE_VANNA, target.toSpecification(), properties);
        return Collections.singleton(new ComputedValue(spec, valueVanna.getAmount()));
      }

    };
  }
}
