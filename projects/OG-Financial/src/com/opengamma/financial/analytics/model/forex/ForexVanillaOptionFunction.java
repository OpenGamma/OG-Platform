/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex;

import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.forex.calculator.ForexConverter;
import com.opengamma.financial.forex.calculator.ForexDerivative;
import com.opengamma.financial.model.option.definition.SmileDeltaTermStructureDataBundle;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public abstract class ForexVanillaOptionFunction extends ForexOptionFunction {

  public ForexVanillaOptionFunction(final String putCurveName, final String callCurveName, final String surfaceName, final String valueRequirementName) {
    super(putCurveName, callCurveName, surfaceName, valueRequirementName);
  }

  @Override
  protected ForexConverter<?> getDefinition(final FinancialSecurity target) {
    final FXOptionSecurity security = (FXOptionSecurity) target;
    return getVisitor().visitFXOptionSecurity(security);
  }

  @Override
  protected Currency getPutCurrency(final FinancialSecurity target) {
    final FXOptionSecurity security = (FXOptionSecurity) target;
    return security.getPutCurrency();
  }

  @Override
  protected Currency getCallCurrency(final FinancialSecurity target) {
    final FXOptionSecurity security = (FXOptionSecurity) target;
    return security.getCallCurrency();
  }

  @Override
  protected Identifier getSpotIdentifier(final FinancialSecurity target) {
    final FXOptionSecurity security = (FXOptionSecurity) target;
    return FXUtils.getSpotIdentifier(security, true);
  }

  @Override
  protected abstract Object getResult(ForexDerivative fxOption, SmileDeltaTermStructureDataBundle data);

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    if (target.getType() != ComputationTargetType.SECURITY) {
      return false;
    }
    return target.getSecurity() instanceof FXOptionSecurity;
  }

}
