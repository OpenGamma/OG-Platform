/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import java.util.Collection;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.FXOptionSecurity;

/**
 *
 */
public class FXForwardCurveFromYieldCurvesSecurityDefaults extends FXForwardCurveFromYieldCurvesDefaults {

  public FXForwardCurveFromYieldCurvesSecurityDefaults(final String... currencyCurveConfigAndDiscountingCurveNames) {
    super(FinancialSecurityTypes.FX_OPTION_SECURITY, currencyCurveConfigAndDiscountingCurveNames);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final String firstCurrency = fxOption.getPutCurrency().getCode();
    final String secondCurrency = fxOption.getCallCurrency().getCode();
    final Collection<String> currencies = getTargets();
    return currencies.contains(firstCurrency) && currencies.contains(secondCurrency);
  }

  @Override
  protected String getFirstCurrency(final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    return fxOption.getPutCurrency().getCode();
  }

  @Override
  protected String getSecondCurrency(final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    return fxOption.getCallCurrency().getCode();
  }
}
