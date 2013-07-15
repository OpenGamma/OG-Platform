/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.curve.forward;

import com.opengamma.core.security.Security;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.forex.ForexVisitors;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public class FXForwardCurveSecurityDefaults extends FXForwardCurveDefaults {

  public FXForwardCurveSecurityDefaults(final String... defaultsPerCurrencyPair) {
    super(FinancialSecurityTypes.FX_OPTION_SECURITY, defaultsPerCurrencyPair);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    final Security security = target.getSecurity();
    final FXOptionSecurity fxOption = (FXOptionSecurity) security;
    final Currency putCurrency = fxOption.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = fxOption.accept(ForexVisitors.getCallCurrencyVisitor());
    final String currencyPair = UnorderedCurrencyPair.of(putCurrency, callCurrency).getObjectId().getValue();
    if (getAllCurrencyPairs().contains(currencyPair)) {
      return true;
    }
    final String firstCcy = currencyPair.substring(0, 3);
    final String secondCcy = currencyPair.substring(3, 6);
    final String reversedCcys = secondCcy + firstCcy;
    return getAllCurrencyPairs().contains(reversedCcys);
  }

  @Override
  protected String getCurrencyPair(final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    final Currency putCurrency = fxOption.accept(ForexVisitors.getPutCurrencyVisitor());
    final Currency callCurrency = fxOption.accept(ForexVisitors.getCallCurrencyVisitor());
    return UnorderedCurrencyPair.of(putCurrency, callCurrency).getObjectId().getValue();
  }

}
