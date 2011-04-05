/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertSame;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;

/**
 * Test ObjectIdentifier. 
 */
@Test
public class ObjectIdentifierTest {

  public void test_factory_String_String() {
    ObjectIdentifier test = ObjectIdentifier.of("Scheme", "value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme::value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullScheme() {
    ObjectIdentifier.of((String) null, "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_emptyScheme() {
    ObjectIdentifier.of("", "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullValue() {
    ObjectIdentifier.of("Scheme", (String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_emptyValue() {
    ObjectIdentifier.of("Scheme", "");
  }

  //-------------------------------------------------------------------------
  public void test_parse() {
    ObjectIdentifier test = ObjectIdentifier.parse("Scheme::value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme::value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat1() {
    ObjectIdentifier.parse("Scheme");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat2() {
    ObjectIdentifier.parse("Scheme:value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat3() {
    ObjectIdentifier.parse("Scheme::value::other");
  }

  //-------------------------------------------------------------------------
  public void test_withScheme() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    assertEquals(ObjectIdentifier.of("newScheme", "value1"), test.withScheme("newScheme"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withScheme_empty() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    test.withScheme("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withScheme_null() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    test.withScheme(null);
  }

  //-------------------------------------------------------------------------
  public void test_withValue() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    assertEquals(ObjectIdentifier.of("id1", "newValue"), test.withValue("newValue"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withValue_empty() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    test.withValue("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withValue_null() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    test.withValue(null);
  }

  //-------------------------------------------------------------------------
  public void test_atLatestVersion() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    assertEquals(UniqueIdentifier.of("id1", "value1", null), test.atLatestVersion());
  }

  //-------------------------------------------------------------------------
  public void test_atVersion() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    assertEquals(UniqueIdentifier.of("id1", "value1", "32"), test.atVersion("32"));
  }

  public void test_atVersion_null() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    assertEquals(UniqueIdentifier.of("id1", "value1", null), test.atVersion(null));
  }

  //-------------------------------------------------------------------------
  public void test_getObjectId() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    assertSame(test, test.getObjectId());
  }

  //-------------------------------------------------------------------------
  public void test_compareTo() {
    ObjectIdentifier a = ObjectIdentifier.of("A", "1");
    ObjectIdentifier b = ObjectIdentifier.of("A", "2");
    ObjectIdentifier c = ObjectIdentifier.of("B", "2");
    
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

  @Test(expectedExceptions = NullPointerException.class)
  public void test_compareTo_null() {
    ObjectIdentifier test = ObjectIdentifier.of("A", "1");
    test.compareTo(null);
  }

  //-------------------------------------------------------------------------
  public void test_equals() {
    ObjectIdentifier d1a = ObjectIdentifier.of("Scheme", "d1");
    ObjectIdentifier d1b = ObjectIdentifier.of("Scheme", "d1");
    ObjectIdentifier d2 = ObjectIdentifier.of("Scheme", "d2");
    
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

  public void test_hashCode() {
    ObjectIdentifier d1a = ObjectIdentifier.of("Scheme", "d1");
    ObjectIdentifier d1b = ObjectIdentifier.of("Scheme", "d1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  //-------------------------------------------------------------------------
  public void test_fudgeEncoding() {
    ObjectIdentifier test = ObjectIdentifier.of("id1", "value1");
    FudgeMsg msg = test.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    ObjectIdentifier decoded = ObjectIdentifier.fromFudgeMsg(msg);
    assertEquals(test, decoded);
  }

}
