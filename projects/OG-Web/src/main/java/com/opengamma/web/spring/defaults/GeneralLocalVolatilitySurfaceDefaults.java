/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.web.spring.defaults;

import java.util.ArrayList;
import java.util.List;

/**
 * Class containing default values for local volatility surface construction. 
 * At the moment, these defaults are the same for all underlyings and security types,
 * but could be changed to be more flexible.
 */
public class GeneralLocalVolatilitySurfaceDefaults {
  private static final List<String> LOCAL_VOLATILITY_DEFAULTS;

  static {
    LOCAL_VOLATILITY_DEFAULTS = new ArrayList<String>();
    LOCAL_VOLATILITY_DEFAULTS.add("0.001");
  }
  
  /**
   * Gets the default values for local volatility surface construction
   * @return A list containing the default values
   */
  public static List<String> getLocalVolatilitySurfaceDefaults() {
    return LOCAL_VOLATILITY_DEFAULTS;
  }
}
