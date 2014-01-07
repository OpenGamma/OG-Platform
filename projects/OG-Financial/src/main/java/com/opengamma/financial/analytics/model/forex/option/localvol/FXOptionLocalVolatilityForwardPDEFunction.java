/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.forex.option.localvol;

import org.threeten.bp.ZonedDateTime;

import com.opengamma.analytics.financial.model.option.pricing.analytic.formula.EuropeanVanillaOption;
import com.opengamma.analytics.util.time.TimeCalculator;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.financial.analytics.model.InstrumentTypeProperties;
import com.opengamma.financial.analytics.model.forex.FXUtils;
import com.opengamma.financial.analytics.model.volatility.local.LocalVolatilityForwardPDEFunction;
import com.opengamma.financial.security.FinancialSecurity;
import com.opengamma.financial.security.FinancialSecurityTypes;
import com.opengamma.financial.security.option.FXOptionSecurity;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 *
 */
public abstract class FXOptionLocalVolatilityForwardPDEFunction extends LocalVolatilityForwardPDEFunction {

  public FXOptionLocalVolatilityForwardPDEFunction(final String blackSmileInterpolatorName) {
    super(blackSmileInterpolatorName);
  }

  @Override
  public ComputationTargetType getTargetType() {
    return FinancialSecurityTypes.FX_OPTION_SECURITY;
  }

  @Override
  protected ComputationTargetReference getVolatilitySurfaceAndForwardCurveTarget(final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    return ComputationTargetType.UNORDERED_CURRENCY_PAIR.specification(UnorderedCurrencyPair.of(fxOption.getCallCurrency(), fxOption.getPutCurrency()));
  }

  @Override
  protected ComputationTargetReference getDiscountingCurveTarget(final ComputationTarget target) {
    final FXOptionSecurity fxOption = (FXOptionSecurity) target.getSecurity();
    return ComputationTargetType.CURRENCY.specification(fxOption.getCallCurrency());
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
