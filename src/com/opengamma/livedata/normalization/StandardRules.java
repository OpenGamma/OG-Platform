/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Collections;

/**
 * 
 *
 * @author pietari
 */
public class StandardRules {
  
  private static final NormalizationRuleSet NO_NORMALIZATION = 
    new NormalizationRuleSet("No Normalization", 
        "Raw",
        Collections.<NormalizationRule>emptyList());
  
  /**
   * Will include:
   * 
   * <ul>
   * <li>IndicativeValue
   * <li>Volume (if available)
   * </ul>
   */
  public static String getOpenGammaRuleSetId() {
    return "OpenGamma";
  }
  
  public static NormalizationRuleSet getNoNormalization() {
    return NO_NORMALIZATION;
  }
  
}
