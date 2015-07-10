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
import com.opengamma.analytics.financial.forex.method.FXMatrix;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.issuer.CurrencyExposureIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.CompiledFunctionDefinition;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.swap.BillTotalReturnSwapSecurity;
import com.opengamma.util.money.MultipleCurrencyAmount;

/**
 * 
 */
public class BillTotalReturnSwapCurrencyExposureFunction extends BillTotalReturnSwapFunction {
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, MultipleCurrencyAmount> CALCULATOR =
      CurrencyExposureIssuerCalculator.getInstance();
  /**
   * 
   */
  public BillTotalReturnSwapCurrencyExposureFunction() {
    super(FX_CURRENCY_EXPOSURE);
  }
  
  @Override
  public CompiledFunctionDefinition compile(FunctionCompilationContext context, Instant atInstant) {
    return new BillTotalReturnSwapCompiledFunction(getTargetToDefinitionConverter(context),
        getDefinitionToDerivativeConverter(context), false) {

      @Override
      protected String getCurrencyOfResult(final BillTotalReturnSwapSecurity security) {
        throw new IllegalStateException("BillTotalReturnSwapCurrencyExposureFunction does not set the Currency property in this method");
      }

      @Override
      protected Set<ComputedValue> getValues(FunctionExecutionContext executionContext, FunctionInputs inputs, ComputationTarget target, Set<ValueRequirement> desiredValues,
          InstrumentDerivative derivative, FXMatrix fxMatrix) {
        
        Set<ComputedValue> results = Sets.newHashSet();
        for (ValueRequirement desiredValue : desiredValues) {
          ParameterIssuerProviderInterface issuerCurves = getMergedWithIssuerProviders(inputs, fxMatrix);
          
          MultipleCurrencyAmount exposure = derivative.accept(CALCULATOR, issuerCurves);
          ComputedValue result = new ComputedValue(ValueSpecification.of(FX_CURRENCY_EXPOSURE, target.toSpecification(), desiredValue.getConstraints()), exposure);
          results.add(result);
        }
        
        return results;
      }

    };
  }

}
