/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class FieldNameChangeTest {
  
  @Test
  public void fieldNameChange() {
    FieldNameChange nameChange = new FieldNameChange("Foo", "Bar");
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeMsg normalized = nameChange.apply(msg, "123", new FieldHistoryStore());
    assertEquals(3, normalized.getAllFields().size());
    assertNull(normalized.getByName("Foo"));
    assertEquals(2.0, (Double) normalized.getAllByName("Bar").get(0).getValue(), 0.0001);
    assertEquals("1", (String) normalized.getAllByName("Bar").get(1).getValue());
    assertEquals(500, normalized.getInt("Baz").intValue());
  }

}
