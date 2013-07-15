/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondfutureoption;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackCubeBundle;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.ircurve.calcconfig.MultiCurveCalculationConfig;
import com.opengamma.financial.security.FinancialSecurityUtils;
import com.opengamma.financial.security.option.BondFutureOptionSecurity;

/**
 *
 */
public class BondFutureOptionBlackPV01Function extends BondFutureOptionBlackCurveSpecificFunction {
  private static final PV01Calculator CALCULATOR = new PV01Calculator(PresentValueCurveSensitivityBlackCalculator.getInstance());

  public BondFutureOptionBlackPV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative bondFutureOption, final YieldCurveWithBlackCubeBundle data, final MultiCurveCalculationConfig curveCalculationConfig,
      final ValueSpecification spec, final FunctionInputs inputs, final Set<ValueRequirement> desiredValues, final BondFutureOptionSecurity security) {
    final String curveName = Iterables.getOnlyElement(desiredValues).getConstraint(ValuePropertyNames.CURVE) + "_" + FinancialSecurityUtils.getCurrency(security);
    final Map<String, Double> pv01 = CALCULATOR.visit(bondFutureOption, data);
    if (!pv01.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for " + curveName);
    }
    return Collections.singleton(new ComputedValue(spec, pv01.get(curveName)));
  }

}
