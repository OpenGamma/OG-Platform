/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.MutableFudgeMsg;
import org.testng.annotations.Test;

import com.opengamma.livedata.server.FieldHistoryStore;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class FieldFilterTest {

  public void normalCase() {
    Set<String> fieldsToAccept = new HashSet<String>();
    fieldsToAccept.add("Foo");    
    fieldsToAccept.add("Bar");
    FieldFilter filter = new FieldFilter(fieldsToAccept);
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertEquals("1", normalized.getString("Foo"));
    assertEquals(2.0, normalized.getDouble("Bar"), 0.0001);
    assertNull(normalized.getByName("Baz"));
  }

  public void extinguishmentWithNonEmptyFieldsToAccept() {
    Set<String> fieldsToAccept = new HashSet<String>();
    fieldsToAccept.add("Foo");    
    fieldsToAccept.add("Bar");
    FieldFilter filter = new FieldFilter(fieldsToAccept);
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo2", "1");
    
    MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNull(normalized);
  }

  public void extinguishmentWithEmptyFieldsToAccept() {
    Set<String> fieldsToAccept = new HashSet<String>();
    FieldFilter filter = new FieldFilter(fieldsToAccept);
    
    MutableFudgeMsg msg = OpenGammaFudgeContext.getInstance().newMessage();
    msg.add("Foo", "1");
    
    MutableFudgeMsg normalized = filter.apply(msg, "123", new FieldHistoryStore());
    assertNull(normalized);
  }

}
