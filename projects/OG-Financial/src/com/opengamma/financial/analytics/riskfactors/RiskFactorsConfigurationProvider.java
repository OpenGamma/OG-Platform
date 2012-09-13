/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.riskfactors;

import com.opengamma.util.money.Currency;

/**
 * Provides configuration to a {@link RiskFactorsGatherer}.
 */
public interface RiskFactorsConfigurationProvider {

  /**
   * Gets the output currency override which should be applied to the risk factors.
   * <p>
   * If this is not set then risk factors should be generated in their default currency which could be different for
   * each position.
   * 
   * @return the output currency override, or null if not set
   */
  Currency getCurrencyOverride();
  
  /**
   * Gets the name of the funding curve to use where required.
   * 
   * @return the name of the funding curve, not null
   */
  String getFundingCurve();
  
  /**
   * Gets the name of the forward curve to use where required.
   * 
   * @param currency  the currency for which the forward curve is required, not null
   * @return the name of the forward curve, not null
   */
  String getForwardCurve(Currency currency);
  
  /**
   * Gets the name of the FX Option Volatility Surface
   * @param ccy1 numerator currency
   * @param ccy2 denominator currency
   */
  String getFXVanillaOptionSurfaceName(Currency ccy1, Currency ccy2);
  
  /**
   * Gets the name of the Commodity Future Option Volatility Surface
   * @param futureCode typically two character future code prefix, e.g. ED.
   */
  String getCommodityFutureOptionVolatilitySurfaceName(String futureCode);
  
  /**
   * Gets the name of the IR Future Option Volatility Surface
   * @param futureCode typically two character future code prefix, e.g. ED.
   */
  String getIRFutureOptionVolatilitySurfaceName(String futureCode);
  
  /**
   * Gets the name of the Equity Index Option Volatility Surface
   * @param tickerPlusMarketSector bloomberg code for underlying index e.g. DJX Index
   */
  String getEquityIndexOptionVolatilitySurfaceName(String tickerPlusMarketSector);
  
  /**
   * Gets the name of the ATM Swaption Volatility Surface
   * @param ccy Currency of the swaption
   */
  String getSwaptionVolatilitySurfaceName(Currency ccy);
  
  /**
   * Gets the name of the Swaption Volatility Cube
   * @param ccy Currency of the swaption
   */
  String getSwaptionVolatilityCubeName(Currency ccy);
}
