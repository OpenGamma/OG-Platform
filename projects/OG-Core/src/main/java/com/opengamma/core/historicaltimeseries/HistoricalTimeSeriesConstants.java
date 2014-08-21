/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.historicaltimeseries;


/**
 * Class for holding generic observation time name or data provider names
 */
public final class HistoricalTimeSeriesConstants {
  
  /**
   * Historical timeseries observation time names
   */
  /** Default observation time**/
  public static final String DEFAULT_OBSERVATION_TIME = "DEFAULT";
  /** London close observation time **/
  public static final String LONDON_CLOSE = "LONDON_CLOSE";
  /** Tokyo close observation time **/
  public static final String TOKYO_CLOSE = "TOKYO_CLOSE";
  /** NewYork close observation time **/
  public static final String NEWYORK_CLOSE = "NEWYORK_CLOSE";
  

  /**
   * Restricted constructor
   */
  private HistoricalTimeSeriesConstants() {
  }
  
  
}
