/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.GammaSpotBlackForexCalculator;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionDigital;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionSingleBarrier;
import com.opengamma.analytics.financial.forex.derivative.ForexOptionVanilla;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.model.option.definition.ForexOptionDataBundle;
import com.opengamma.analytics.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionExecutionContext;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingValueGammaSpotFXOptionFunction;
import com.opengamma.util.money.CurrencyAmount;

/**
 * The function to compute the Gamma Spot of Forex options in the Black model.
 * @deprecated Use {@link BlackDiscountingValueGammaSpotFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackValueGammaSpotFunction extends FXOptionBlackSingleValuedFunction {

  /**
   * The calculator to compute the gamma value.
   */
  private static final GammaSpotBlackForexCalculator CALCULATOR = GammaSpotBlackForexCalculator.getInstance();

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#VALUE_GAMMA_P}
   */
  public FXOptionBlackValueGammaSpotFunction() {
    super(ValueRequirementNames.VALUE_GAMMA_P);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final CurrencyAmount result = forex.accept(CALCULATOR, data);
      final double gammaValue = result.getAmount() / 100.0; // FIXME: the 100 should be removed when the scaling is available
      // for PLAT-4626
      double spot = 1;
      if (forex instanceof ForexOptionVanilla) {
        final ForexOptionVanilla fxDerivative = (ForexOptionVanilla) forex;
        spot = data.getFxRates().getFxRate(fxDerivative.getCurrency1(), fxDerivative.getCurrency2());
      } else if (forex instanceof ForexOptionDigital) {
        final ForexOptionDigital fxDerivative = (ForexOptionDigital) forex;
        if (fxDerivative.payDomestic()) {
          spot = data.getFxRates().getFxRate(fxDerivative.getCurrency1(), fxDerivative.getCurrency2());     
        } else {
          spot = data.getFxRates().getFxRate(fxDerivative.getCurrency2(), fxDerivative.getCurrency1());       
        }
      } else if (forex instanceof ForexOptionSingleBarrier) {
        final ForexOptionSingleBarrier fxDerivative = (ForexOptionSingleBarrier) forex;
        spot = data.getFxRates().getFxRate(fxDerivative.getCurrency1(), fxDerivative.getCurrency2());
        return Collections.singleton(new ComputedValue(spec, gammaValue * spot * spot));
      }
      return Collections.singleton(new ComputedValue(spec, gammaValue * spot));
    }
    throw new OpenGammaRuntimeException("Can only calculate gamma spot for surfaces with smiles");
  }
}
