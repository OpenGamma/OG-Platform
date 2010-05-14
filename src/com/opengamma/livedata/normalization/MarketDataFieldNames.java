/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;



/**
 * 
 *
 * @author kirk
 */
public interface MarketDataFieldNames {
  
  /** 
   * Sent in the OpenGamma normalization schema for all asset types. 
   */
  public static final String INDICATIVE_VALUE_FIELD = "IndicativeValue";
  
  /** 
   * Sent in the OpenGamma normalization schema for all options for which 
   * it is available from the underlying market data API. 
   */
  public static final String IMPLIED_VOLATILITY_FIELD = "ImpliedVolatility";
  
  /** 
   * Sent in the OpenGamma normalization schema for all asset types for which available (e.g., equities). 
   */
  public static final String VOLUME = "Volume";
  
  public static final String LAST_FIELD = "LAST";
  public static final String MID_FIELD = "MID";
  public static final String BID_FIELD = "BID";
  public static final String ASK_FIELD = "ASK";
  
  public static final String BEST_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_BEST";
  public static final String BID_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_BID";
  public static final String ASK_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_ASK";
  public static final String LAST_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_LAST";
  public static final String MID_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_MID";
  
}
