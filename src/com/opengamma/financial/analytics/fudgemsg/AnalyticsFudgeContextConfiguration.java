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
 * 
 * @author Andrew Griffin
 */
public class AnalyticsFudgeContextConfiguration extends FudgeContextConfiguration {
  
  public static final FudgeContextConfiguration INSTANCE = new AnalyticsFudgeContextConfiguration ();
  
  public void configureFudgeObjectDictionary (final FudgeObjectDictionary dictionary) {
    ModelInterestRateCurve.addBuilders (dictionary);
  }
  
}