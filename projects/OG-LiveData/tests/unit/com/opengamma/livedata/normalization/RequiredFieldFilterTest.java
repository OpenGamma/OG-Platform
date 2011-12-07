/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;

/**
 * 
 */
public class RequiredFieldFilterTest {

  @Test
  public void noRequiredFields() {
    RequiredFieldFilter filter = new RequiredFieldFilter();
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNotNull(normalized);
    assertSame(normalized, msg);
  }

  @Test
  public void requiredFieldsNotSatisfied() {
    RequiredFieldFilter filter = new RequiredFieldFilter("Foo", "Fibble");
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNull(normalized);
  }

  @Test
  public void requiredFieldsSatisfied() {
    RequiredFieldFilter filter = new RequiredFieldFilter("Foo");
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNotNull(normalized);
    assertSame(normalized, msg);
  }
}
