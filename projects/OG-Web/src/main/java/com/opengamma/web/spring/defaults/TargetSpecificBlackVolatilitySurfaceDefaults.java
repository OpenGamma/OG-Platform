/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring.defaults;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opengamma.financial.analytics.model.curve.forward.ForwardCurveValuePropertyNames;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;

/**
 * Class containing default values for Black volatility surface calculations. The default values
 * are target-specific (e.g. unordered currency pairs for FX, a ticker for equities). 
 */
public class TargetSpecificBlackVolatilitySurfaceDefaults {
  private static final Map<UnorderedCurrencyPair, List<String>> FX_BLACK_SURFACE_DEFAULTS;
  private static final Map<Currency, List<String>> COMMODITY_BLACK_SURFACE_DEFAULTS;
  
  static {
    List<String> eurusd = Arrays.asList("EURUSD", "DiscountingImplied", ForwardCurveValuePropertyNames.PROPERTY_YIELD_CURVE_IMPLIED_METHOD, "TULLETT");
    FX_BLACK_SURFACE_DEFAULTS = new HashMap<>(); 
    FX_BLACK_SURFACE_DEFAULTS.put(UnorderedCurrencyPair.of(Currency.EUR, Currency.USD), eurusd);

    List<String> usdCommodity = Arrays.asList("USD", "BBG", ForwardCurveValuePropertyNames.PROPERTY_FUTURE_PRICE_METHOD, "BBG");
    COMMODITY_BLACK_SURFACE_DEFAULTS = new HashMap<>();
    COMMODITY_BLACK_SURFACE_DEFAULTS.put(Currency.USD, usdCommodity);
  }
  
  /**
   * Gets all lists of default values for FX Black volatility surfaces
   * @return A collection of lists containing the default values
   */
  public static List<String> getAllFXDefaults() {
    final List<String> result = new ArrayList<>();
    for (final List<String> defaults : FX_BLACK_SURFACE_DEFAULTS.values()) {
      result.addAll(defaults);
    }
    return result;
  }
  
  /**
   * Gets all lists of default values for commodity Black volatility surfaces
   * @return A collection of lists containing the default values
   */
  public static List<String> getAllCommodityDefaults() {
    final List<String> result = new ArrayList<>();
    for (final List<String> defaults : COMMODITY_BLACK_SURFACE_DEFAULTS.values()) {
      result.addAll(defaults);
    }
    return result;
  }
  
  /**
   * Gets the default values for a FX Black volatility surface for a particular currency pair.
   * @param currencyPair The currency pair
   * @return A list containing the default values
   * @throws IllegalArgumentException if default values for the currency pair are not found 
   */
  public static List<String> getFXDefaults(final UnorderedCurrencyPair currencyPair) {
    final List<String> defaults = FX_BLACK_SURFACE_DEFAULTS.get(currencyPair);
    if (defaults != null) {
      return defaults;
    }
    throw new IllegalArgumentException("Could not get Black volatility surface defaults for " + currencyPair);
  }
  
  /**
   * Gets the default values for a commodity Black volatility surface for a particular currency 
   * @param currency The currency
   * @return A list containing the default values
   * @throws IllegalArgumentException if default values for the currency are not found
   */
  public static List<String> getCommodityDefaults(final Currency currency) {
    final List<String> defaults = COMMODITY_BLACK_SURFACE_DEFAULTS.get(currency);
    if (defaults != null) {
      return defaults;
    }
    throw new IllegalArgumentException("Could not get Black volatility surface defaults for " + currency);
  }
}
