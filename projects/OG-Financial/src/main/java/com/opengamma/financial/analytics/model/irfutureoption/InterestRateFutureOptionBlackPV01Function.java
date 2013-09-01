/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.irfutureoption;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.core.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.BlackDiscountingPV01IRFutureOptionFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * Calculates the PV01 for interest rate future options.
 * @deprecated Use {@link BlackDiscountingPV01IRFutureOptionFunction}
 */
@Deprecated
public class InterestRateFutureOptionBlackPV01Function extends InterestRateFutureOptionBlackCurveSpecificFunction {
  private static final PV01Calculator CALCULATOR = new PV01Calculator(PresentValueCurveSensitivityBlackCalculator.getInstance());

  public InterestRateFutureOptionBlackPV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative irFutureOption, final YieldCurveWithBlackCubeBundle data, final String curveName, final ValueSpecification spec,
      final Security security) {
    final Map<String, Double> pv01 = CALCULATOR.visit(irFutureOption, data);
    final String fullCurveName = curveName + "_" + FinancialSecurityUtils.getCurrency(security).getCode();
    if (!pv01.containsKey(fullCurveName)) {
      throw new OpenGammaRuntimeException("Could not get sensitivity for " + curveName);
    }
    return Collections.singleton(new ComputedValue(spec, pv01.get(fullCurveName)));
  }

}
