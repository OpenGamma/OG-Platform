/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

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
  public Currency getCurrencyOverride() {
    return _currencyOverride;
  }

  @Override
  public String getFundingCurve() {
    return "Discounting";
  }

  @Override
  public String getForwardCurve(Currency currency) {
    String suffix;
    if (currency.equals(Currency.USD) || currency.equals(Currency.NZD) || currency.equals(Currency.SEK)) { 
      suffix = "3M";
    } else {
      suffix = "6M";
    }
    return "Forward" + suffix;
  }

  @Override
  public String getFXVanillaOptionSurfaceName(Currency ccy1, Currency ccy2) {
    return "DEFAULT_" + ccy1.getCode() + ccy2.getCode();
  }

  @Override
  public String getIRFutureOptionVolatilitySurfaceName(String futureCode) {
    return "DEFAULT_" + futureCode;
  }
  
  @Override
  public String getCommodityFutureOptionVolatilitySurfaceName(String futureCode) {
    return "DEFAULT_" + futureCode;
  }

  @Override
  public String getEquityIndexOptionVolatilitySurfaceName(String tickerPlusMarketSector) {
    return "DEFAULT_" + tickerPlusMarketSector;
  }

  @Override
  public String getSwaptionVolatilitySurfaceName(Currency ccy) {
    return "DEFAULT_" + ccy.getCode();
  }

  @Override
  public String getSwaptionVolatilityCubeName(Currency ccy) {
    return "DEFAULT_" + ccy.getCode();
  }
  
}
