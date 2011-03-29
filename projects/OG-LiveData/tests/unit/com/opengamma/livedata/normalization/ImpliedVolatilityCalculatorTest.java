/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * 
 */
public class ImpliedVolatilityCalculatorTest {
  
  ImpliedVolatilityCalculator _calculator = new ImpliedVolatilityCalculator();
  
  @Test
  public void best() {
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataRequirementNames.BEST_IMPLIED_VOLATILITY, 50.80);
    msg.add(MarketDataRequirementNames.MID_IMPLIED_VOLATILITY, 50.81);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = _calculator.apply(msg, store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }
  
  @Test
  public void mid() {
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataRequirementNames.MID_IMPLIED_VOLATILITY, 50.80);
    msg.add(MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY, 50.81);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = _calculator.apply(msg, store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }
  
  @Test
  public void last() {
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataRequirementNames.LAST_IMPLIED_VOLATILITY, 50.80);
    msg.add(MarketDataRequirementNames.BID_IMPLIED_VOLATILITY, 50.81);
    msg.add(MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY, 50.82);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = _calculator.apply(msg, store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }
  
  @Test
  public void bidAsk() {
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataRequirementNames.BID_IMPLIED_VOLATILITY, 50.81);
    msg.add(MarketDataRequirementNames.ASK_IMPLIED_VOLATILITY, 50.82);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = _calculator.apply(msg, store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.815, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }
  
  @Test
  public void history() {
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataRequirementNames.IMPLIED_VOLATILITY, 50.80);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = _calculator.apply(FudgeContext.GLOBAL_DEFAULT.newMessage(), store);
    assertEquals(1, normalized.getAllFields().size());
    assertEquals(50.80, normalized.getDouble(MarketDataRequirementNames.IMPLIED_VOLATILITY), 0.0001);
  }

}
