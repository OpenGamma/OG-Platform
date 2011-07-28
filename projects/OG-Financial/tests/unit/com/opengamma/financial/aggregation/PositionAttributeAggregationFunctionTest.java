/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.aggregation;

import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import org.testng.annotations.Test;

import static org.testng.AssertJUnit.assertEquals;

/**
 *
 */
public class PositionAttributeAggregationFunctionTest {

  public static final String ATTR_NAME = "ATTR_NAME";
  public static final String POS_VALUE = "POS_VALUE";
  public static final String TRADE_VALUE = "TRADE_VALUE";
  private final PositionAttributeAggregationFunction _aggFn = new PositionAttributeAggregationFunction(ATTR_NAME);

  @Test
  public void positionAttribute() {
    PositionImpl position = new PositionImpl();
    position.addAttribute(ATTR_NAME, POS_VALUE);
    assertEquals(POS_VALUE, _aggFn.classifyPosition(position));
  }

  @Test
  public void tradeAttribute() {
    PositionImpl position = new PositionImpl();
    TradeImpl trade = new TradeImpl();
    trade.addAttribute(ATTR_NAME, TRADE_VALUE);
    position.addTrade(trade);
    assertEquals(TRADE_VALUE, _aggFn.classifyPosition(position));
  }

  /**
   * Tests the position's value is used when both the position and one of its trades have values for the same attribute.
   */
  @Test
  public void positionAndTradeAttributes() {
    PositionImpl position = new PositionImpl();
    position.addAttribute(ATTR_NAME, POS_VALUE);
    TradeImpl trade = new TradeImpl();
    trade.addAttribute(ATTR_NAME, TRADE_VALUE);
    position.addTrade(trade);
    assertEquals(POS_VALUE, _aggFn.classifyPosition(position));
  }
}
