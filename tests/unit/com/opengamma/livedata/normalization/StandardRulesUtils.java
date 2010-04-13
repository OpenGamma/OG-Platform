/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.fudgemsg.FudgeFieldContainer;

/**
 * 
 *
 * @author pietari
 */
public class StandardRulesUtils {
  
  public static void validateOpenGammaMsg(FudgeFieldContainer msg) {
    assertNotNull(msg);
    assertTrue(msg.getAllFields().size() >= 1 && msg.getAllFields().size() <= 2);
    assertNotNull(msg.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD));
    assertTrue(msg.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD) > 0.0);    
  }

}
