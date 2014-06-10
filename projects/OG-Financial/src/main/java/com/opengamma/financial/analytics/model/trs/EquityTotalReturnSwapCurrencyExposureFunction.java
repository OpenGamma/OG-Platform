/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.trs;

import static com.opengamma.engine.value.ValueRequirementNames.FX_CURRENCY_EXPOSURE;

import java.util.Set;

import org.threeten.bp.Instant;

import com.google.common.collect.Sets;
import com.opengamma.analytics.financial.equity.EquityTrsDataBundle;
import com.opengamma.analytics.financial.equity.trs.EqyTrsCurrencyExposureCalculator;
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * Calculates the currency exposure of an equity total return swap security.
 */
public class EquityTotalReturnSwapCurrencyExposureFunction extends EquityTotalReturnSwapFunction  {

  private static final InstrumentDerivativeVisitor<EquityTrsDataBundle, MultipleCurrencyAmount> CALCULATOR =
      EqyTrsCurrencyExposureCalculator.getInstance();

  /**
   * Sets the value requirement to {@link ValueRequirementNames#FX_CURRENCY_EXPOSURE}.
   */
  public EquityTotalReturnSwapCurrencyExposureFunction() {
    super(FX_CURRENCY_EXPOSURE);
  }

  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant) {

    return new EquityTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context),
                                                     getDefinitionToDerivativeConverter(context),
                                                     false) {

      @Override
      protected Set<ComputedValue> getValues(FunctionExecutionContext executionContext,
                                             FunctionInputs inputs,
                                             ComputationTarget target,
                                             Set<ValueRequirement> desiredValues,
                                             InstrumentDerivative derivative,
                                             FXMatrix fxMatrix) {
        Set<ComputedValue> results = Sets.newHashSet();
        for (ValueRequirement desiredValue : desiredValues) {
          EquityTrsDataBundle data = getDataBundle(inputs, fxMatrix);
          MultipleCurrencyAmount exposure = derivative.accept(CALCULATOR, data);
          ComputedValue result = new ComputedValue(ValueSpecification.of(FX_CURRENCY_EXPOSURE,
                                                                         target.toSpecification(),
                                                                         desiredValue.getConstraints()), exposure);
          results.add(result);
        }
        return results;
      }
    };
  }
}
