/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.interestrate.InterestRateDerivative;
import com.opengamma.financial.interestrate.PresentValueSABRCalculator;
import com.opengamma.financial.model.option.definition.SABRInterestRateDataBundle;

/**
 * 
 */
public class InterestRateFutureOptionPresentValueFunction extends InterestRateFutureOptionFunction {
  private static final PresentValueSABRCalculator CALCULATOR = PresentValueSABRCalculator.getInstance();

  public InterestRateFutureOptionPresentValueFunction(final String surfaceName) {
    super(surfaceName, ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResults(final InterestRateDerivative irFutureOption, final SABRInterestRateDataBundle data, final ValueSpecification[] specifications,
      final Set<ValueRequirement> desiredValues, final String ccy, final FunctionInputs inputs) {
    if (specifications.length != 1 || !specifications[0].getValueName().equals(ValueRequirementNames.PRESENT_VALUE)) {
      throw new OpenGammaRuntimeException("This should never happen: value specifications do not match those required");
    }
    final double presentValue = CALCULATOR.visit(irFutureOption, data);
    return Collections.singleton(new ComputedValue(specifications[0], presentValue));
  }

}
