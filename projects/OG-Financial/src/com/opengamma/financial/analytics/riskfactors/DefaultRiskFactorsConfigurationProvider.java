/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.util.money.Currency;

/**
 * Default implementation of {@link RiskFactorsConfigurationProvider}.
 */
public class DefaultRiskFactorsConfigurationProvider implements RiskFactorsConfigurationProvider {

  private final Currency _currencyOverride;
  
  public DefaultRiskFactorsConfigurationProvider() {
    _currencyOverride = null;
  }
  
  public DefaultRiskFactorsConfigurationProvider(Currency currencyOverride) {
    _currencyOverride = currencyOverride;
  }

  @Override
  public ValueProperties processConstraints(ValueProperties constraints) {
    if (_currencyOverride == null) {
      return constraints;
    }
    return constraints.copy().with(ValuePropertyNames.CURRENCY, _currencyOverride.getCode()).get();
  }

  @Override
  public String getFundingCurve() {
    return "FUNDING";
  }

  @Override
  public String getForwardCurve(Currency currency) {
    String suffix;
    if (currency.equals(Currency.USD)) { 
      suffix = "3M";
    } else {
      suffix = "6M";
    }
    return "FORWARD_" + suffix;
  }
  
}
