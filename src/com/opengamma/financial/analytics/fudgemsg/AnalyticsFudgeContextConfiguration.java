/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.fudgemsg;

import org.fudgemsg.FudgeContextConfiguration;
import org.fudgemsg.mapping.FudgeObjectDictionary;

/**
 * Configuration for Fudge of the OG-Analytics library.
 * <p>
 * This configures Fudge builders.
 */
public final class AnalyticsFudgeContextConfiguration extends FudgeContextConfiguration {

  /**
   * The singleton configuration.
   */
  public static final FudgeContextConfiguration INSTANCE = new AnalyticsFudgeContextConfiguration();

  /**
   * Restricted constructor.
   */
  private AnalyticsFudgeContextConfiguration() {
  }

  //-------------------------------------------------------------------------
  @Override
  public void configureFudgeObjectDictionary(final FudgeObjectDictionary dictionary) {
    MathInterpolation.addBuilders(dictionary);
    ModelInterestRateCurve.addBuilders(dictionary);
    ModelVolatilitySurface.addBuilders(dictionary);
  }

}
