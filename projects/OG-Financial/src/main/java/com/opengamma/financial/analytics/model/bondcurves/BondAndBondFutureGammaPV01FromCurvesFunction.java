/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.bondcurves;

import static com.opengamma.engine.value.ValuePropertyNames.CURRENCY;
import static com.opengamma.engine.value.ValueRequirementNames.GAMMA_PV01;

import com.opengamma.analytics.financial.interestrate.InstrumentDerivativeVisitor;
import com.opengamma.analytics.financial.provider.calculator.discounting.GammaPV01CurveParametersCalculator;
import com.opengamma.analytics.financial.provider.calculator.issuer.PresentValueCurveSensitivityIssuerCalculator;
import com.opengamma.analytics.financial.provider.description.interestrate.ParameterIssuerProviderInterface;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValueRequirementNames;
import com.opengamma.financial.security.FinancialSecurityUtils;

/**
 * Calculates the gamma PV01 of a bond or bond future from yield curves.
 */
public class BondAndBondFutureGammaPV01FromCurvesFunction extends BondAndBondFutureFromCurvesFunction<ParameterIssuerProviderInterface, Double> {
  /** The gamma PV01 calculator */
  private static final InstrumentDerivativeVisitor<ParameterIssuerProviderInterface, Double> CALCULATOR =
      new GammaPV01CurveParametersCalculator<>(PresentValueCurveSensitivityIssuerCalculator.getInstance());

  /**
   * Sets the value requirement name to {@link ValueRequirementNames#GAMMA_PV01} and
   * the calculator to {@link GammaPV01CurveParametersCalculator}.
   */
  public BondAndBondFutureGammaPV01FromCurvesFunction() {
    super(GAMMA_PV01, CALCULATOR);
  }

  @Override
  protected ValueProperties.Builder getResultProperties(final ComputationTarget target) {
    final String currency = FinancialSecurityUtils.getCurrency(target.getTrade().getSecurity()).getCode();
    return super.getResultProperties(target)
        .with(CURRENCY, currency);
  }

}
