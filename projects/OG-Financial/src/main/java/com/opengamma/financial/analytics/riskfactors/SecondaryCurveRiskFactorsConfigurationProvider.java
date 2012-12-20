/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

import com.opengamma.util.money.Currency;

/**
 * Implementation of {@link RiskFactorsConfigurationProvider} which uses SECONDARY forward and funding curves.
 */
public class SecondaryCurveRiskFactorsConfigurationProvider extends DefaultRiskFactorsConfigurationProvider {

  private static final String SECONDARY_CURVE_NAME = "SECONDARY";
  
  public SecondaryCurveRiskFactorsConfigurationProvider() {
    super();
  }
  
  public SecondaryCurveRiskFactorsConfigurationProvider(Currency currencyOverride) {
    super(currencyOverride);
  }

  @Override
  public String getFundingCurve() {
    return SECONDARY_CURVE_NAME;
  }

  @Override
  public String getForwardCurve(Currency currency) {
    return SECONDARY_CURVE_NAME;
  }
  
}
