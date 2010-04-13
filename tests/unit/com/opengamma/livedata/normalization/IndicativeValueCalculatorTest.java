/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.junit.Assert.assertEquals;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.Test;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * 
 *
 * @author pietari
 */
public class IndicativeValueCalculatorTest {
  
  @Test
  public void bidAskLast() {
    IndicativeValueCalculator calculator = new IndicativeValueCalculator();
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataFieldNames.BID_FIELD, 50.80);
    msg.add(MarketDataFieldNames.ASK_FIELD, 50.90);
    msg.add(MarketDataFieldNames.LAST_FIELD, 50.89);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = calculator.apply(msg, store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.85, normalized.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD), 0.0001);
  }
  
  @Test
  public void bidAskOnly() {
    IndicativeValueCalculator calculator = new IndicativeValueCalculator();
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataFieldNames.BID_FIELD, 50.80);
    msg.add(MarketDataFieldNames.ASK_FIELD, 50.90);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = calculator.apply(msg, store);
    assertEquals(3, normalized.getAllFields().size());
    assertEquals(50.85, normalized.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD), 0.0001);
  }
  
  @Test
  public void lastOnly() {
    IndicativeValueCalculator calculator = new IndicativeValueCalculator();
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataFieldNames.LAST_FIELD, 50.89);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = calculator.apply(msg, store);
    assertEquals(2, normalized.getAllFields().size());
    assertEquals(50.89, normalized.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD), 0.0001);
  }
  
  @Test
  public void bigSpread() {
    IndicativeValueCalculator calculator = new IndicativeValueCalculator();
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataFieldNames.BID_FIELD, 50.0);
    msg.add(MarketDataFieldNames.ASK_FIELD, 100.0);
    msg.add(MarketDataFieldNames.LAST_FIELD, 55.12);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = calculator.apply(msg, store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(55.12, normalized.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD), 0.0001);
  }
  
  @Test
  public void bigSpreadLowLast() {
    IndicativeValueCalculator calculator = new IndicativeValueCalculator();
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataFieldNames.BID_FIELD, 50.0);
    msg.add(MarketDataFieldNames.ASK_FIELD, 100.0);
    msg.add(MarketDataFieldNames.LAST_FIELD, 44.50);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = calculator.apply(msg, store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.0, normalized.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD), 0.0001);
  }
  
  @Test
  public void bigSpreadHighLast() {
    IndicativeValueCalculator calculator = new IndicativeValueCalculator();
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataFieldNames.BID_FIELD, 50.0);
    msg.add(MarketDataFieldNames.ASK_FIELD, 100.0);
    msg.add(MarketDataFieldNames.LAST_FIELD, 120.0);
    
    FieldHistoryStore store = new FieldHistoryStore();
    store.liveDataReceived(msg);
    
    MutableFudgeFieldContainer normalized = calculator.apply(msg, store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(100.0, normalized.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD), 0.0001);
  }

}
