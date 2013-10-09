/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.fixedincome;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.analytics.financial.interestrate.InstrumentDerivative;
import com.opengamma.analytics.financial.interestrate.PV01Calculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.core.position.Trade;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionInputs;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.engine.value.ValueSpecification;

/**
 * Computes the PV01 of bond trades.
 */
@Deprecated
public class BondTradePV01Function extends BondTradeCurveSpecificFunction {
  private static final PV01Calculator CALCULATOR = PV01Calculator.getInstance();

  public BondTradePV01Function() {
    super(ValueRequirementNames.PV01);
  }

  @Override
  public Set<ComputedValue> getResults(final InstrumentDerivative derivative, final String curveName, final YieldCurveBundle curves,
      final String curveCalculationConfigName, final String curveCalculationMethod, final FunctionInputs inputs, final ComputationTarget target,
      final ValueSpecification resultSpec) {
    final Trade trade = target.getTrade();
    final Map<String, Double> pv01 = CALCULATOR.visit(derivative, curves);
    if (!pv01.containsKey(curveName)) {
      throw new OpenGammaRuntimeException("Could not get PV01 for curve named " + curveName + "; should never happen");
    }
    return Collections.singleton(new ComputedValue(resultSpec, trade.getQuantity().doubleValue() * pv01.get(curveName)));
  }

}
