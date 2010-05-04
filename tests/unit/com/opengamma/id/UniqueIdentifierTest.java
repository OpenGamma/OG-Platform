/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.junit.Test;

/**
 * Test UniqueIdentifier. 
 */
public class UniqueIdentifierTest {

  @Test
  public void test_factory_String_String() {
    UniqueIdentifier test = UniqueIdentifier.of("Scheme", "value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme::value", test.toString());
  }

  @Test(expected=NullPointerException.class)
  public void test_factory_String_String_nullScheme() {
    UniqueIdentifier.of((String) null, "value");
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_factory_String_String_emptyScheme() {
    UniqueIdentifier.of("", "value");
  }

  @Test(expected=NullPointerException.class)
  public void test_factory_String_String_nullValue() {
    UniqueIdentifier.of("Scheme", (String) null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_factory_String_String_emptyValue() {
    UniqueIdentifier.of("Scheme", "");
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_parse() {
    UniqueIdentifier test = UniqueIdentifier.parse("Scheme::value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme::value", test.toString());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_parse_invalidFormat() {
    UniqueIdentifier.parse("Scheme:value");
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_compareTo() {
    UniqueIdentifier a = UniqueIdentifier.of("A", "1");
    UniqueIdentifier b = UniqueIdentifier.of("A", "2");
    UniqueIdentifier c = UniqueIdentifier.of("B", "2");
    
    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);
    assertEquals(true, a.compareTo(c) < 0);
    
    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
    assertEquals(true, b.compareTo(c) < 0);
    
    assertEquals(true, c.compareTo(a) > 0);
    assertEquals(true, c.compareTo(b) > 0);
    assertEquals(true, c.compareTo(c) == 0);
  }

  @Test(expected=NullPointerException.class)
  public void test_compareTo_null() {
    UniqueIdentifier test = UniqueIdentifier.of("A", "1");
    test.compareTo(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals() {
    UniqueIdentifier d1a = UniqueIdentifier.of("Scheme", "d1");
    UniqueIdentifier d1b = UniqueIdentifier.of("Scheme", "d1");
    UniqueIdentifier d2 = UniqueIdentifier.of("Scheme", "d2");
    
    assertEquals(true, d1a.equals(d1a));
    assertEquals(true, d1a.equals(d1b));
    assertEquals(false, d1a.equals(d2));
    
    assertEquals(true, d1b.equals(d1a));
    assertEquals(true, d1b.equals(d1b));
    assertEquals(false, d1b.equals(d2));
    
    assertEquals(false, d2.equals(d1a));
    assertEquals(false, d2.equals(d1b));
    assertEquals(true, d2.equals(d2));
    
    assertEquals(false, d1b.equals("d1"));
    assertEquals(false, d1b.equals(null));
  }

  @Test
  public void test_hashCode() {
    UniqueIdentifier d1a = UniqueIdentifier.of("Scheme", "d1");
    UniqueIdentifier d1b = UniqueIdentifier.of("Scheme", "d1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_fudgeEncoding() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1");
    FudgeFieldContainer msg = test.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    UniqueIdentifier decoded = UniqueIdentifier.fromFudgeMsg(msg);
    assertEquals(test, decoded);
  }

}
