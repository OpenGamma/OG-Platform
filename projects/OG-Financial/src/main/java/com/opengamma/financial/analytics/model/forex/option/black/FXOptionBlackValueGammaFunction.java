/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.GammaValueBlackForexCalculator;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingValueGammaFXOptionFunction;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the Gamma of Forex options in the Black model.
 * @deprecated Use {@link BlackDiscountingValueGammaFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackValueGammaFunction extends FXOptionBlackSingleValuedFunction {

  /**
   * The calculator to compute the gamma value.
   */
  private static final GammaValueBlackForexCalculator CALCULATOR = GammaValueBlackForexCalculator.getInstance();

  public FXOptionBlackValueGammaFunction() {
    super(ValueRequirementNames.VALUE_GAMMA);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final CurrencyAmount result = forex.accept(CALCULATOR, data);
      final String resultCurrency = result.getCurrency().getCode();
      final String expectedCurrency = spec.getProperty(ValuePropertyNames.CURRENCY);
      if (!expectedCurrency.equals(resultCurrency)) {
        throw new OpenGammaRuntimeException("Expected currency " + expectedCurrency + " does not equal result currency " + resultCurrency);
      }
      final double gammaValue = result.getAmount();
      return Collections.singleton(new ComputedValue(spec, gammaValue));
    }
    throw new OpenGammaRuntimeException("Can only calculate gamma for surfaces with smiles");
  }

}
