/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeContextConfiguration;
import org.fudgemsg.mapping.FudgeObjectDictionary;

/**
 * Registers custom builders for the OG-Analytics library.
 */
public class AnalyticsFudgeContextConfiguration extends FudgeContextConfiguration {

  /**
   * A pre-constructed instance.
   */
  public static final FudgeContextConfiguration INSTANCE = new AnalyticsFudgeContextConfiguration();
  
  @Override
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    MathInterpolation.addBuilders(dictionary);
    ModelInterestRateCurve.addBuilders(dictionary);
    ModelVolatilitySurface.addBuilders(dictionary);
  }
  
}
