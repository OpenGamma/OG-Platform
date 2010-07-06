/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import java.util.Collections;

/**
 * Normalization rules that are known to be part of the OpenGamma standard package.
 *
 */
public class StandardRules {
  
  private static final NormalizationRuleSet NO_NORMALIZATION = 
    new NormalizationRuleSet("No Normalization", 
        "Raw",
        Collections.<NormalizationRule>emptyList());
  
  /**
   * @return Will include:
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
