/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.exposure;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.position.Trade;
import com.opengamma.financial.security.fra.FRASecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;

/**
 * Unit test for TradeAttributeExposureFunction.
 */
@Test(groups = TestGroup.UNIT)
public class TradeAttributeExposureFunctionTest {

  private static final ExposureFunction EXPOSURE_FUNCTION = new TradeAttributeExposureFunction();
  
  @Test
  public void testSingleAttribute() {
    FRASecurity security = ExposureFunctionTestHelper.getFRASecurity();
    Trade trade = ExposureFunctionTestHelper.getTrade(security);
    Map<String, String> attributes = new HashMap<>();
    String key = "Attribute1";
    String value = "Value1";
    attributes.put(key, value);
    trade.setAttributes(attributes);
    
    List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals("Expected one ExternalId", 1, ids.size());
    assertEquals("Expected trade attribute with key " + key + ", value " + value, ExternalId.of(TradeAttributeExposureFunction.TRADE_ATTRIBUTE_IDENTIFIER, key + "=" + value), ids.get(0));
  }
  
  @Test
  public void testMultipleAttributes() {
    FRASecurity security = ExposureFunctionTestHelper.getFRASecurity();
    Trade trade = ExposureFunctionTestHelper.getTrade(security);
    Map<String, String> attributes = new HashMap<>();
    String key1 = "Attribute1";
    String value1 = "Value1";
    attributes.put(key1, value1);
    String key2 = "Attribute2";
    String value2 = "Value2";
    attributes.put(key2, value2);
    trade.setAttributes(attributes);
    
    List<ExternalId> ids = EXPOSURE_FUNCTION.getIds(trade);
    assertEquals("Expected two ExternalIds", 2, ids.size());
    assertTrue("Expected trade attribute with key " + key1 + ", value " + value1, ids.contains(ExternalId.of(TradeAttributeExposureFunction.TRADE_ATTRIBUTE_IDENTIFIER, key1 + "=" + value1)));
    assertTrue("Expected trade attribute with key " + key2 + ", value " + value2, ids.contains(ExternalId.of(TradeAttributeExposureFunction.TRADE_ATTRIBUTE_IDENTIFIER, key2 + "=" + value2)));
  }
}
