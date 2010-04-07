/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.normalization;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.junit.Test;

/**
 * 
 *
 * @author pietari
 */
public class FieldFilterTest {
  
  @Test
  public void nonEmptyFieldsToAccept() {
    Set<String> fieldsToAccept = new HashSet<String>();
    fieldsToAccept.add("Foo");    
    fieldsToAccept.add("Bar");
    FieldFilter filter = new FieldFilter(fieldsToAccept);
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Foo", "1");
    msg.add("Bar", 2.0);
    msg.add("Baz", 500);
    
    MutableFudgeFieldContainer normalized = filter.apply(msg);
    assertEquals("1", normalized.getString("Foo"));
    assertEquals(2.0, normalized.getDouble("Bar"), 0.0001);
    assertNull(normalized.getByName("Baz"));
  }
  
  @Test
  public void emptyFieldsToAccept() {
    Set<String> fieldsToAccept = new HashSet<String>();
    FieldFilter filter = new FieldFilter(fieldsToAccept);
    
    MutableFudgeFieldContainer msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
    msg.add("Foo", "1");
    
    MutableFudgeFieldContainer normalized = filter.apply(msg);
    assertNull(normalized.getByName("Foo"));
  }

}
