/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * 
 */
public class RequiredFieldFilterTest {

  @Test
  public void noRequiredFields() {
    RequiredFieldFilter filter = new RequiredFieldFilter();
    
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
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
    
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNull(normalized);
  }

  @Test
  public void requiredFieldsSatisfied() {
    RequiredFieldFilter filter = new RequiredFieldFilter("Foo");
    
    MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNotNull(normalized);
    assertSame(normalized, msg);
  }
}
