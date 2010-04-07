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
   * This is the only field that is actually distributed from LiveData to the Engine 
   * in the OpenGamma normalization schema. 
   */
  public static final String INDICATIVE_VALUE_FIELD = "IndicativeValue";
  
  public static final String LAST_FIELD = "LAST";
  public static final String MID_FIELD = "MID";
  public static final String BID_FIELD = "BID";
  public static final String ASK_FIELD = "ASK";
  
}
