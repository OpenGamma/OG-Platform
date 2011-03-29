/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import com.opengamma.livedata.server.FieldHistoryStore;

/**
 * 
 */
public class RequiredFieldFilterTest {

  @Test
  public void noRequiredFields() {
    RequiredFieldFilter filter = new RequiredFieldFilter();
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeFieldContainer normalized = filter.apply(msg, new FieldHistoryStore());
    assertNotNull(normalized);
    assertSame(normalized, msg);
  }

  @Test
  public void requiredFieldsNotSatisfied() {
    RequiredFieldFilter filter = new RequiredFieldFilter("Foo", "Fibble");
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeFieldContainer normalized = filter.apply(msg, new FieldHistoryStore());
    assertNull(normalized);
  }

  @Test
  public void requiredFieldsSatisfied() {
    RequiredFieldFilter filter = new RequiredFieldFilter("Foo");
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeFieldContainer normalized = filter.apply(msg, new FieldHistoryStore());
    assertNotNull(normalized);
    assertSame(normalized, msg);
  }
}
