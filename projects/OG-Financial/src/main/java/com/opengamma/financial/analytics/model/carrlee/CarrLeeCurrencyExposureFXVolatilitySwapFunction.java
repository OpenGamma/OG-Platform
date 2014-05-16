/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.carrlee;

import static com.opengamma.engine.value.ValueRequirementNames.FX_CURRENCY_EXPOSURE;

import java.util.Collections;
import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Iterables;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.description.volatilityswap.CarrLeeFXData;
import com.opengamma.analytics.financial.volatilityswap.CarrLeeFXVolatilitySwapCalculator;
import com.opengamma.analytics.financial.volatilityswap.CarrLeeFXVolatilitySwapDeltaCalculator;
import com.opengamma.analytics.financial.volatilityswap.FXVolatilitySwap;
import com.opengamma.analytics.financial.volatilityswap.VolatilitySwapCalculatorResult;
import com.opengamma.analytics.financial.volatilityswap.VolatilitySwapCalculatorResultWithStrikes;
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
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class CarrLeeCurrencyExposureFXVolatilitySwapFunction extends CarrLeeFXVolatilitySwapFunction {

  /** The fair value calculator */
  private static final InstrumentDerivativeVisitor<CarrLeeFXData, VolatilitySwapCalculatorResult> FV_CALCULATOR = new CarrLeeFXVolatilitySwapCalculator();
  /** The delta calculator */
  private static final CarrLeeFXVolatilitySwapDeltaCalculator DELTA_CALCULATOR = new CarrLeeFXVolatilitySwapDeltaCalculator();
  
  /**
   * Sets the value requirement to {@link ValueRequirementNames#FX_CURRENCY_EXPOSURE}.
   */
  public CarrLeeCurrencyExposureFXVolatilitySwapFunction() {
    super(FX_CURRENCY_EXPOSURE);
  }

  @Override
  public CompiledFunctionDefinition compile(final FunctionCompilationContext context, final Instant atInstant) {
    return new CarrLeeFXVolatilitySwapCompiledFunction(getTargetToDefinitionConverter(context), getDefinitionToDerivativeConverter(context), false) {

      @SuppressWarnings("synthetic-access")
      @Override
      protected Set<ComputedValue> getValues(final FunctionExecutionContext executionContext, final FunctionInputs inputs, final ComputationTarget target,
          final Set<ValueRequirement> desiredValues, final InstrumentDerivative derivative, final FXMatrix fxMatrix) {
        final CarrLeeFXData data = getCarrLeeData(executionContext, inputs, target, fxMatrix);
        
        if (derivative instanceof FXVolatilitySwap) {
          final FXVolatilitySwap swap = (FXVolatilitySwap) derivative;
          final VolatilitySwapCalculatorResultWithStrikes res = (VolatilitySwapCalculatorResultWithStrikes) FV_CALCULATOR.visitFXVolatilitySwap(swap, data);
          final double strike = swap.getVolatilityStrike();
          final double notional = swap.getVolatilityNotional();
          final double spot = data.getSpot();
          final double pv = notional * (res.getFairValue() - strike) * spot;
          
          final double delta = DELTA_CALCULATOR.getFXVolatilitySwapDelta(res, swap, data);
          final double pvDelta = notional * delta * spot;

          final CurrencyAmount[] currencyExposure = new CurrencyAmount[2];
          currencyExposure[0] = CurrencyAmount.of(data.getCurrencyPair().getFirst(), pvDelta);
          currencyExposure[1] = CurrencyAmount.of(data.getCurrencyPair().getSecond(), pv - pvDelta * spot);
          final MultipleCurrencyAmount result = MultipleCurrencyAmount.of(currencyExposure);
          
          final ValueProperties properties = Iterables.getOnlyElement(desiredValues).getConstraints().copy().get();
          final ValueSpecification spec = new ValueSpecification(FX_CURRENCY_EXPOSURE, target.toSpecification(), properties);
          return Collections.singleton(new ComputedValue(spec, result));
        }
        
        throw new IllegalArgumentException("Derivative instrument should be FX volatility swap");
      }

    };
  }

}
