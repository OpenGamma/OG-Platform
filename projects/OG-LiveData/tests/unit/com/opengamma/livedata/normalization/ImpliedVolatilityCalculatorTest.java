/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class ImpliedVolatilityCalculatorTest {
  
  ImpliedVolatilityCalculator _calculator = new ImpliedVolatilityCalculator();
  
  @Test
  public void best() {
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BEST_IMPLIED_VOLATILITY, 50.80);
    msg.add(MarketDataRequirementNames.MID_IMPLIED_VOLATILITY, 50.81);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = _calculator.apply(msg, "123", store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }
  
  @Test
  public void mid() {
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.MID_IMPLIED_VOLATILITY, 50.80);
    msg.add(MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY, 50.81);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = _calculator.apply(msg, "123", store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }
  
  @Test
  public void last() {
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY, 50.80);
    msg.add(MarketDataRequirementNames.BID_IMPLIED_VOLATILITY, 50.81);
    msg.add(MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY, 50.82);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = _calculator.apply(msg, "123", store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }
  
  @Test
  public void bidAsk() {
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.BID_IMPLIED_VOLATILITY, 50.81);
    msg.add(MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY, 50.82);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = _calculator.apply(msg, "123", store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.815, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }
  
  @Test
  public void history() {
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, 50.80);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeMsg normalized = _calculator.apply(OpenGammaFudgeContext.getInstance().newMessage(), "123", store);
    assertEquals(1, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }

}
