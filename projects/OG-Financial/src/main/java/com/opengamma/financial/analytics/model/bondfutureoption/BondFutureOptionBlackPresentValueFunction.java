/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondfutureoption;

import java.util.Collections;
import java.util.Set;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PresentValueBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;

/**
 *
 */
public class BondFutureOptionBlackPresentValueFunction extends BondFutureOptionBlackFunction {
  private static final PresentValueBlackCalculator CALCULATOR = PresentValueBlackCalculator.getInstance();

  public BondFutureOptionBlackPresentValueFunction() {
    super(ValueRequirementNames.PRESENT_VALUE);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative bondFutureOption, final YieldCurveWithBlackCubeBundle data, final MultiCurveCalculationConfig curveCalculationConfig,
      final ValueSpecification spec, final FunctionInputs inputs, final Set<ValueRequirement> desiredValue, final BondFutureOptionSecurity security) {
    final double pv = bondFutureOption.accept(CALCULATOR, data);
    return Collections.singleton(new ComputedValue(spec, pv));
  }

}
