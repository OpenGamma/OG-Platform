/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.model.equity;

/**
 * Property and corresponding value names used in the various ScenarioPnLFunction's
 */
public class ScenarioPnLPropertyNamesAndValues {

  /** Property name for the size of the shift to the volatility surface */
  public static final String PROPERTY_VOL_SHIFT = "VolShift";
  
  /** 
   * Property name for the type of the shift to the volatility surface. 
   *  Valid values are: ADDITIVE and MULTIPLICATIVE
   */
  public static final String PROPERTY_VOL_SHIFT_TYPE = "VolShiftType";
  
  
  /** Property name for the size of the shift to the spot or forward price */
  public static final String PROPERTY_PRICE_SHIFT = "PriceShift";
  
  /** 
   * Property name for the type of the shift to the spot or forward price. 
   *  Valid values are: ADDITIVE and MULTIPLICATIVE
   */
  public static final String PROPERTY_PRICE_SHIFT_TYPE = "PriceShiftType";
  
  /** Apply shift in an absolute, additive fashion */
  public static final String ADDITIVE = "Additive";
  
  /** Apply shift in an relative, multiplicative fashion */
  public static final String MULTIPLICATIVE = "Multiplicative";
  
}
