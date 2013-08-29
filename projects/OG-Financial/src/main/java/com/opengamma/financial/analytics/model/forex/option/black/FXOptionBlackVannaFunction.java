/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.VannaValueBlackForexCalculator;
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
import com.opengamma.financial.analytics.model.black.BlackDiscountingValueVannaFXOptionFunction;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Produces Vanna for FXOption's in the Black (Garman-Kohlhagen) world.
 * This is the spot vanna, ie the 2nd order cross-sensitivity of the present value to the spot and implied vol,
 *          $\frac{\partial^2 (PV)}{\partial spot \partial \sigma}$
 * @deprecated Use {@link BlackDiscountingValueVannaFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackVannaFunction extends FXOptionBlackSingleValuedFunction {

  public FXOptionBlackVannaFunction() {
    super(ValueRequirementNames.VALUE_VANNA);
  }

  private static final VannaValueBlackForexCalculator CALCULATOR = VannaValueBlackForexCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final CurrencyAmount result = forex.accept(CALCULATOR, data);
      final double vannaValue = result.getAmount();
      return Collections.singleton(new ComputedValue(spec, vannaValue));
    }
    throw new OpenGammaRuntimeException("Can only calculate vanna for surfaces with smiles");
  }

}
