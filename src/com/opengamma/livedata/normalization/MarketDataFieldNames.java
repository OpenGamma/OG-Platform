/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;



/**
 * 
 *
 */
public interface MarketDataFieldNames {
  
  /** 
   * Sent in the OpenGamma normalization schema for all asset types. 
   */
  String INDICATIVE_VALUE_FIELD = "IndicativeValue";
  
  /** 
   * Sent in the OpenGamma normalization schema for all options for which 
   * it is available from the underlying market data API. 
   */
  String IMPLIED_VOLATILITY_FIELD = "ImpliedVolatility";
  
  /** 
   * Sent in the OpenGamma normalization schema for all asset types for which available (e.g., equities). 
   */
  String VOLUME = "Volume";
  
  /**
   * Last trade value
   */
  String LAST_FIELD = "LAST";
  
  /**
   * (bid + ask) / 2
   */
  String MID_FIELD = "MID";
  
  /**
   * Highest bid
   */
  String BID_FIELD = "BID";
  
  /**
   * Lowest ask
   */
  String ASK_FIELD = "ASK";
  
  // the following fields mirror Bloomberg option data fields
  
  /**
   * Best estimate of implied volatility, might be same as mid implied vol
   */
  String BEST_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_BEST";
  
  /**
   * Implied vol if you want to sell options
   */
  String BID_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_BID";
  
  /**
   * Implied vol if you want to buy options
   */
  String ASK_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_ASK";
  
  /**
   * Implied vol based on the last trade
   */
  String LAST_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_LAST";
  
  /**
   * An average of bid and ask implied vol
   */
  String MID_IMPLIED_VOLATILITY_FIELD = "OPT_IMPLIED_VOLATILITY_MID";
  
}
