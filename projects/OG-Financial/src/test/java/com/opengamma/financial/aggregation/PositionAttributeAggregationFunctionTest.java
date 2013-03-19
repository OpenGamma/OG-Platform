/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class PositionAttributeAggregationFunctionTest {

  public static final String ATTR_NAME = "ATTR_NAME";
  public static final String POS_VALUE = "POS_VALUE";
  public static final String TRADE_VALUE = "TRADE_VALUE";
  private final PositionAttributeAggregationFunction _aggFn = new PositionAttributeAggregationFunction(ATTR_NAME);

  @Test
  public void positionAttribute() {
    SimplePosition position = new SimplePosition();
    position.addAttribute(ATTR_NAME, POS_VALUE);
    assertEquals(POS_VALUE, _aggFn.classifyPosition(position));
  }

  @Test
  public void tradeAttribute() {
    SimplePosition position = new SimplePosition();
    SimpleTrade trade = new SimpleTrade();
    trade.addAttribute(ATTR_NAME, TRADE_VALUE);
    position.addTrade(trade);
    assertEquals(TRADE_VALUE, _aggFn.classifyPosition(position));
  }

  /**
   * Tests the position's value is used when both the position and one of its trades have values for the same attribute.
   */
  @Test
  public void positionAndTradeAttributes() {
    SimplePosition position = new SimplePosition();
    position.addAttribute(ATTR_NAME, POS_VALUE);
    SimpleTrade trade = new SimpleTrade();
    trade.addAttribute(ATTR_NAME, TRADE_VALUE);
    position.addTrade(trade);
    assertEquals(POS_VALUE, _aggFn.classifyPosition(position));
  }
}
