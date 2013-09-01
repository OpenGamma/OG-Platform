/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
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
import com.opengamma.core.position.Position;
import com.opengamma.core.security.Security;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.value.ValueRenamingFunction;

/**
 * Function computes the {@link ValueRequirementNames#POSITION_RHO},
 * first order derivative of {@link Position} price with respect to the continuously compounded discount rates of the provided {@link ValuePropertyNames#CURVE},
 * for interest rate future options in the Black world.<p>
 * This is equivalent to {@link InterestRateFutureOptionBlackPV01Function}, though Rho is intended to have the curve set by default.
 * @deprecated This function simply returns the PV01, which is calculated in {@link InterestRateFutureOptionBlackPV01Function}.
 * Use {@link ValueRenamingFunction} to rename outputs rather than repeating the calculations.
 */
@Deprecated
public class InterestRateFutureOptionBlackPositionRhoFunction extends InterestRateFutureOptionBlackCurveSpecificFunction {
  private static final PV01Calculator CALCULATOR = new PV01Calculator(PresentValueCurveSensitivityBlackCalculator.getInstance());

  public InterestRateFutureOptionBlackPositionRhoFunction() {
    super(ValueRequirementNames.POSITION_RHO);
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
