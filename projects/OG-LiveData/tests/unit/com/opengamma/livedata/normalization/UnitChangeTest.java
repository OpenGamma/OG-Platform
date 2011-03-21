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
public class UnitChangeTest {
  
  @Test
  public void unitChange() {
    UnitChange unitChange = new UnitChange("Foo", 10);
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Foo", 2.0);
    msg.add("Bar", "2");
    
    MutableFudgeFieldContainer normalized = unitChange.apply(msg, new FieldHistoryStore());
    assertEquals(2, normalized.getAllFields().size());
    assertEquals(20.0, normalized.getDouble("Foo"), 0.0001);
  }

}
