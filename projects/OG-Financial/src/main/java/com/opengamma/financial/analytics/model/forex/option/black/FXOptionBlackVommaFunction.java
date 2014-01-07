/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.black;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.forex.calculator.VommaValueBlackForexCalculator;
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
import com.opengamma.financial.analytics.model.black.BlackDiscountingValueVommaFXOptionFunction;
import com.opengamma.util.money.CurrencyAmount;

/**
 * Produces Vomma (aka Volga) for FXOption's in the Black (Garman-Kohlhagen) world.
 * This is the spot vomma, the 2nd order sensitivity of the present value to the implied vol,
 *          $\frac{\partial^2}{\partial \sigma^2} (PV) $
 * @deprecated Use {@link BlackDiscountingValueVommaFXOptionFunction}
 */
@Deprecated
public class FXOptionBlackVommaFunction extends FXOptionBlackSingleValuedFunction {

  public FXOptionBlackVommaFunction() {
    super(ValueRequirementNames.VALUE_VOMMA);
  }

  private static final VommaValueBlackForexCalculator CALCULATOR = VommaValueBlackForexCalculator.getInstance();

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative forex, final ForexOptionDataBundle<?> data, final ComputationTarget target,
      final Set<ValueRequirement> desiredValues, final FunctionInputs inputs, final ValueSpecification spec, final FunctionExecutionContext executionContext) {
    if (data instanceof SmileDeltaTermStructureDataBundle) {
      final CurrencyAmount result = forex.accept(CALCULATOR, data);
      final double vommaValue = result.getAmount();
      return Collections.singleton(new ComputedValue(spec, vommaValue));
    }
    throw new OpenGammaRuntimeException("Can only calculate vomma for surfaces with smiles");
  }

}
