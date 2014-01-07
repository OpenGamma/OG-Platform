/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MarketValueCalculatorTest {

  public void bidAskLast() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.80);
    msg.add(MarketDataRequirementNames.ASK, 50.90);
    msg.add(MarketDataRequirementNames.LAST, 50.89);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.85, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  public void bidAskOnly() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.80);
    msg.add(MarketDataRequirementNames.ASK, 50.90);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.85, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  public void lastOnly() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.LAST, 50.89);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(2, normalized.getAllFields().size());
    assertEquals(50.89, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  public void bigSpread() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.0);
    msg.add(MarketDataRequirementNames.ASK, 100.0);
    msg.add(MarketDataRequirementNames.LAST, 55.12);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(55.12, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  public void bigSpreadHistory() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg historicalMsg = OpenGammaFudgeContext.getInstance().newMessage();
    historicalMsg.add(MarketDataRequirementNames.LAST, 45); // Should never use this
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(historicalMsg);
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.0);
    msg.add(MarketDataRequirementNames.ASK, 100.0);
    msg.add(MarketDataRequirementNames.LAST, 50.52);
    
    MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.52, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  public void bigSpreadLowLast() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.0);
    msg.add(MarketDataRequirementNames.ASK, 100.0);
    msg.add(MarketDataRequirementNames.LAST, 44.50);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.0, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  public void bigSpreadHighLast() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 50.0);
    msg.add(MarketDataRequirementNames.ASK, 100.0);
    msg.add(MarketDataRequirementNames.LAST, 120.0);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(100.0, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  public void useHistoricalBidAsk() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg historicalMsg = OpenGammaFudgeContext.getInstance().newMessage();
    historicalMsg.add(MarketDataRequirementNames.BID, 50.0);
    historicalMsg.add(MarketDataRequirementNames.ASK, 51.0);
    historicalMsg.add(MarketDataRequirementNames.MARKET_VALUE, 50.52);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(historicalMsg);
    
    MutableFudgeMsg newMsg = OpenGammaFudgeContext.getInstance().newMessage();
    newMsg.add(MarketDataRequirementNames.LAST, 50.89);
    
    MutableFudgeMsg normalized = calculator.apply(newMsg, "123", store);
    assertEquals(2, normalized.getAllFields().size());
    assertEquals(50.5, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  public void useHistoricalMarketValueWithEmptyMsg() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg historicalMsg = OpenGammaFudgeContext.getInstance().newMessage();
    historicalMsg.add(MarketDataRequirementNames.MARKET_VALUE, 50.52);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(historicalMsg);
    
    MutableFudgeMsg newMsg = OpenGammaFudgeContext.getInstance().newMessage();
    
    MutableFudgeMsg normalized = calculator.apply(newMsg, "123", store);
    assertEquals(1, normalized.getAllFields().size());
    assertEquals(50.52, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }

  public void noMarketValueAvailable() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    FieldHistoryStore store = new FieldHistoryStore();
    
    MutableFudgeMsg newMsg = OpenGammaFudgeContext.getInstance().newMessage();
    
    MutableFudgeMsg normalized = calculator.apply(newMsg, "123", store);
    assertEquals(0, normalized.getAllFields().size());
  }

  public void zeroBid() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID, 0.0);
    msg.add(MarketDataRequirementNames.ASK, 1.0);
    msg.add(MarketDataRequirementNames.LAST, 0.57);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(0.5, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE), 0.0001);
  }
  
  public void noBidAskLastOrFieldHistory() {
    MarketValueCalculator calculator = new MarketValueCalculator();
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.CLOSING_BID, 0.1);
    msg.add(MarketDataRequirementNames.CLOSING_ASK, 0.2);
    msg.add(MarketDataRequirementNames.CLOSE, 0.14);
    
    FieldHistoryStore store = new FieldHistoryStore();
    
    MutableFudgeMsg normalized = calculator.apply(msg, "123", store);
    assertEquals(0.14, normalized.getDouble(MarketDataRequirementNames.MARKET_VALUE));
  }

}
