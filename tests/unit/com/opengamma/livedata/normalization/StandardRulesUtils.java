/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;

import com.google.common.collect.Sets;

/**
 * 
 *
 * @author pietari
 */
public class StandardRulesUtils {
  
  public static void validateOpenGammaMsg(FudgeFieldContainer msg) {
    assertNotNull(msg);
    
    Set<String> acceptableFields = Sets.newHashSet(
        MarketDataFieldNames.INDICATIVE_VALUE_FIELD,
        MarketDataFieldNames.VOLUME,
        MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD
        );
    for (FudgeField field : msg.getAllFields()) {
      assertTrue(acceptableFields + " does not contain " + field.getName(), acceptableFields.contains(field.getName()));
    }
    
    assertNotNull(msg.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD));
    assertTrue(msg.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD) >= 0.0);
    
    if (msg.getDouble(MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD) != null) {
      assertTrue(msg.getDouble(MarketDataFieldNames.IMPLIED_VOLATILITY_FIELD) >= 0.0);
    }
  }

}
