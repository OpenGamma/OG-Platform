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
  public void calculate() {
    IndicativeValueCalculator calculator = new IndicativeValueCalculator();
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add(MarketDataFieldNames.ASK_FIELD, 50.90);
    msg.add(MarketDataFieldNames.BID_FIELD, 50.80);
    msg.add(MarketDataFieldNames.LAST_FIELD, 50.89);
    
    FieldHistoryStore store = new FieldHistoryStore();
    MutableFudgeFieldContainer normalized = calculator.apply(msg, store);
    assertEquals(4, normalized.getAllFields().size());
    assertEquals(50.85, normalized.getDouble(MarketDataFieldNames.INDICATIVE_VALUE_FIELD), 0.0001);
  }

}
