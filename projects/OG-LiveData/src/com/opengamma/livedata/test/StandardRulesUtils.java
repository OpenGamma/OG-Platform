/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.test;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Set;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;

import com.google.common.collect.Sets;
import com.opengamma.core.value.MarketDataRequirementNames;

/**
 * Utility methods to test the conformity of messages to standard
 * normalization rules.
 */
public class StandardRulesUtils {
  
  public static void validateOpenGammaMsg(FudgeMsg msg) {
    assertNotNull(msg);
    
    Set<String> acceptableFields = Sets.newHashSet(
        MarketDataRequirementNames.MARKET_VALUE,
        MarketDataRequirementNames.VOLUME,
        MarketDataRequirementNames.IMPLIED_VOLATILITY,
        MarketDataRequirementNames.YIELD_CONVENTION_MID,
        MarketDataRequirementNames.YIELD_YIELD_TO_MATURITY_MID,
        MarketDataRequirementNames.DIRTY_PRICE_MID);
    for (FudgeField field : msg.getAllFields()) {
      assertTrue(acceptableFields + " does not contain " + field.getName(), acceptableFields.contains(field.getName()));
    }
    
    assertNotNull(msg.getDouble(MarketDataRequirementNames.MARKET_VALUE));
    assertTrue(msg.getDouble(MarketDataRequirementNames.MARKET_VALUE) >= 0.0);
    
    if (msg.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY) != null) {
      assertTrue(msg.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY) >= 0.0);
    }
  }

}
