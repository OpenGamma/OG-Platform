/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.localvol;

import javax.time.calendar.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.function.FunctionCompilationContext;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityForwardPDEFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.fx.FXUtils;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * 
 */
public abstract class ForexLocalVolatilityForwardPDEFunction extends LocalVolatilityForwardPDEFunction {

  public ForexLocalVolatilityForwardPDEFunction(final String blackSmileInterpolatorName) {
    super(blackSmileInterpolatorName);
  }

  @Override
  public boolean canApplyTo(final FunctionCompilationContext context, final ComputationTarget target) {
    return target.getType() == ComputationTargetType.SECURITY && target.getSecurity() instanceof FXOptionSecurity;
  }

  @Override
  protected UniqueId getTargetUid(final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    return UnorderedCurrencyPair.of(fxOption.getCallCurrency(), fxOption.getPutCurrency()).getUniqueId();
  }

  @Override
  protected UniqueId getDiscountingCurveUid(final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    return fxOption.getCallCurrency().getUniqueId();
  }

  @Override
  protected String getInstrumentType() {
    return InstrumentTypeProperties.FOREX;
  }

  @Override
  protected EuropeanVanillaOption getOption(final FinancialSecurity security, final ZonedDateTime date) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) security;
    final Currency putCurrency = fxOption.getPutCurrency();
    final Currency callCurrency = fxOption.getCallCurrency();
    double strike;
    if (FXUtils.isInBaseQuoteOrder(putCurrency, callCurrency)) {
      strike = fxOption.getCallAmount() / fxOption.getPutAmount();
    } else {
      strike = fxOption.getPutAmount() / fxOption.getCallAmount();
    }
    final double t = TimeCalculator.getTimeBetween(date, fxOption.getExpiry().getExpiry());
    return new EuropeanVanillaOption(strike, t, true); //TODO this shouldn't be hard coded to a call
  }

}
