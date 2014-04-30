/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model;

/**
 * Contains property names and values that are used to label calculations.
 */
public final class CalculationPropertyNamesAndValues {

  /** Property name for the model type */
  public static final String PROPERTY_MODEL_TYPE = "ModelType";

  // Values for ValuePropertyNames.CALCULATION_METHOD
  /** Black calculations */
  public static final String BLACK_METHOD = "BlackMethod";
  /** Black model without an entire Volatility Surface, hence no smile */
  public static final String BLACK_BASIC_METHOD = "BlackBasicMethod";
  /** Black model without an entire Volatility Surface. Implies volatility from listed option security's market value */
  public static final String BLACK_LISTED_METHOD = "BlackListedMethod";
  /** The Barone-Adesi Whaley approximation for American options */
  public static final String BAW_METHOD = "BaroneAdesiWhaleyMethod";
  /** The Bjerksund-Stensland approximation for American options, from a vol surface */
  public static final String BJERKSUND_STENSLAND_METHOD = "BjerksundStenslandMethod";
  /** The Bjerksund-Stensland approximation for American options, when option's market price is available */
  public static final String BJERKSUND_STENSLAND_LISTED_METHOD = "BjerksundStenslandListedMethod";
  /** The Roll-Geske-Whaley model for American call options with discrete dividends, when option's market price is available */
  public static final String ROLL_GESKE_WHALEY_LISTED_METHOD = "RollGeskeWhaleyListedMethod";
  /** The so-called PDE method computes prices for American and European options under the BlackScholesMerton model*/
  public static final String PDE_METHOD = "PDE";
  /** Pricing from marked / listed price */
  public static final String MARK_TO_MARKET_METHOD = "MarkToMarket";
  /** Pricing cash-flow instruments using discounting */
  public static final String DISCOUNTING = "Discounting";
  /** Pricing FX forwards using a forward points curve */
  public static final String FORWARD_POINTS = "ForwardPoints";
  /** The name of the Black call spread calculation method */
  public static final String CALL_SPREAD_BLACK_METHOD = "CallSpreadBlackMethod";

  //Values for PROPERTY_MODEL_TYPE
  /** Analytic */
  public static final String ANALYTIC = "Analytic";
  /** PDE */
  public static final String PDE = "PDE";

  // FX option-specific properties
  /** The name of the property that sets the value of the call spread */
  public static final String PROPERTY_CALL_SPREAD_VALUE = "CallSpreadValue";
  /** The name of the property that sets whether FX option greeks are in terms of the direct or indirect quote */
  public static final String PROPERTY_DIRECT_QUOTE = "DirectQuote";

  // Properties for greeks
  /** The name of the property that sets the number of days per financial year */
  public static final String PROPERTY_DAYS_PER_YEAR = "DaysInYear";

  // Properties for bond pricing
  /** The property value indicating that bond analytics are produced from the clean price */
  public static final String CLEAN_PRICE_METHOD = "CleanPrice";
  /** The property value indicating that bond analytics are produced from the yield */
  public static final String YIELD_METHOD = "Yield";
  /** The property value indicating that bond analytics are produced from curves */
  public static final String CURVES_METHOD = "Curves";

  // Properties for variance and volatility swap pricing
  /** The name of the property that determines how realized variance is calculated */
  public static final String PROPERTY_REALIZED_VARIANCE_METHOD = "RealizedVarianceMethod";
  /** The property value indicating that the realized variance is supplied as market data */
  public static final String MARKET_REALIZED_VARIANCE = "MarketRealizedVariance";
  /**
   * The property value indicating that the realized variance is calculated from a
   * historical time series.
   */
  public static final String HISTORICAL_REALIZED_VARIANCE = "HistoricalRealizedVariance";
  /** The historical realized variance start date */
  public static final String HISTORICAL_VARIANCE_START = "HistoricalVarianceStartDate";
  /** The historical realized variance end date */
  public static final String HISTORICAL_VARIANCE_END = "HistoricalVarianceEndDate";

  /**
   * Private constructor.
   */
  private CalculationPropertyNamesAndValues() {
  }

}
