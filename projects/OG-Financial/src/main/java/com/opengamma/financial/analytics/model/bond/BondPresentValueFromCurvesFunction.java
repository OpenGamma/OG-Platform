/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bond;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitorAdapter;
import com.opengamma.analytics.financial.interestrate.PresentValueCalculator;
import com.opengamma.analytics.financial.interestrate.YieldCurveBundle;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * Bond present value from curves.
 */
public class BondPresentValueFromCurvesFunction extends BondFromCurvesFunction {

  @Override
  protected InstrumentDerivativeVisitorAdapter<YieldCurveBundle, Double> getCalculator() {
    return PresentValueCalculator.getInstance();
  }

  @Override
  protected String getValueRequirementName() {
    return ValueRequirementNames.PRESENT_VALUE;
  }

  @Override
  protected ValueProperties.Builder getResultProperties() {
    return super.getResultProperties()
        .withAny(ValuePropertyNames.CURRENCY);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final String riskFreeCurveName, final String creditCurveName, final String riskFreeCurveConfig,
      final String creditCurveConfig, final ComputationTarget target) {
    return super.getResultProperties(riskFreeCurveName, creditCurveName, riskFreeCurveConfig, creditCurveConfig, target)
        .with(ValuePropertyNames.CURRENCY, FinancialSecurityUtils.getCurrency(target.getSecurity()).getCode());
  }
}
