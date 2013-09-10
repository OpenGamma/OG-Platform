/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.swaption.basicblack;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.PresentValueCurveSensitivityBlackCalculator;
import com.opengamma.analytics.financial.model.option.definition.YieldCurveWithBlackSwaptionBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.financial.analytics.model.black.ConstantBlackDiscountingPV01SwaptionFunction;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * Calculates the PV01 of a swaption using the Black method with no volatility modelling assumptions.
 * The implied volatility is read directly from the market data system. Note that this function produces
 * the PV01 with respect to a single, named curve.
 * <p>
 * Produces a result for {@link ValueRequirementNames#PV01} using {@link PV01Calculator} with
 * {@link PresentValueCurveSensitivityBlackCalculator} as the sensitivity calculator.
 * @deprecated Use {@link ConstantBlackDiscountingPV01SwaptionFunction}
 */
@Deprecated
public class SwaptionBasicBlackPV01Function extends SwaptionBasicBlackCurveSpecificFunction {
  /** The calculator */
  private static final PV01Calculator CALCULATOR = new PV01Calculator(PresentValueCurveSensitivityBlackCalculator.getInstance());

  /**
   * Sets {@link ValueRequirementNames#PV01} as the result.
   */
  public SwaptionBasicBlackPV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  protected Set<ComputedValue> getResult(final InstrumentDerivative swaption, final YieldCurveWithBlackSwaptionBundle data, final String curveName, final ValueSpecification spec,
      final String curveCalculationConfigName, final String curveCalculationMethod, final FunctionInputs inputs, final ComputationTarget target) {
    final Map<String, Double> pv01 = CALCULATOR.visit(swaption, data);
    final String fullCurveName = curveName + "_" + FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode();
    if (!pv01.containsKey(fullCurveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for " + curveName);
    }
    return Collections.singleton(new ComputedValue(spec, pv01.get(fullCurveName)));
  }
}
