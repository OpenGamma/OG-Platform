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
   * Sent in the OpenGamma normalization schema for all asset types for which available (e.g., equities). 
   */
  public static final String VOLUME = "Volume";
  
  public static final String LAST_FIELD = "LAST";
  public static final String MID_FIELD = "MID";
  public static final String BID_FIELD = "BID";
  public static final String ASK_FIELD = "ASK";
  
}
