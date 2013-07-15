/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;

import com.opengamma.util.test.TestGroup;

/**
 * Test {@link ObjectId}. 
 */
@Test(groups = TestGroup.UNIT)
public class ObjectIdTest {

  public void test_factory_String_String() {
    ObjectId test = ObjectId.of("Scheme", "value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullScheme() {
    ObjectId.of((String) null, "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_emptyScheme() {
    ObjectId.of("", "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullValue() {
    ObjectId.of("Scheme", (String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_emptyValue() {
    ObjectId.of("Scheme", "");
  }

  // [PLAT-1543] Fix ObjectId and enable the test
  @Test(enabled = false)
  public void testStringEscaping() {
    final String[] strs = new String[] {"Foo", "~Foo", "Foo~", "~Foo~", "~", "~~", "~~~" };
    for (String scheme : strs) {
      for (String value : strs) {
        final ObjectId testOID = ObjectId.of(scheme, value);
        final String testStr = testOID.toString();
        // System.out.println("scheme = " + scheme + ", value = " + value + ", oid = " + testOID.toString());
        final ObjectId oid = ObjectId.parse(testStr);
        assertEquals(testOID, oid);
      }
    }
  }

  //-------------------------------------------------------------------------
  public void test_parse() {
    ObjectId test = ObjectId.parse("Scheme~value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat1() {
    ObjectId.parse("Scheme");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat2() {
    ObjectId.parse("Scheme:value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat3() {
    ObjectId.parse("Scheme~value~other");
  }

  //-------------------------------------------------------------------------
  public void test_withScheme() {
    ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(ObjectId.of("newScheme", "value1"), test.withScheme("newScheme"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withScheme_empty() {
    ObjectId test = ObjectId.of("id1", "value1");
    test.withScheme("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withScheme_null() {
    ObjectId test = ObjectId.of("id1", "value1");
    test.withScheme(null);
  }

  //-------------------------------------------------------------------------
  public void test_withValue() {
    ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(ObjectId.of("id1", "newValue"), test.withValue("newValue"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withValue_empty() {
    ObjectId test = ObjectId.of("id1", "value1");
    test.withValue("");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withValue_null() {
    ObjectId test = ObjectId.of("id1", "value1");
    test.withValue(null);
  }

  //-------------------------------------------------------------------------
  public void test_atLatestVersion() {
    ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1", null), test.atLatestVersion());
  }

  //-------------------------------------------------------------------------
  public void test_atVersion() {
    ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1", "32"), test.atVersion("32"));
  }

  public void test_atVersion_null() {
    ObjectId test = ObjectId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1", null), test.atVersion(null));
  }

  //-------------------------------------------------------------------------
  public void test_getObjectId() {
    ObjectId test = ObjectId.of("id1", "value1");
    assertSame(test, test.getObjectId());
  }

  //-------------------------------------------------------------------------
  public void test_compareTo() {
    ObjectId a = ObjectId.of("A", "1");
    ObjectId b = ObjectId.of("A", "2");
    ObjectId c = ObjectId.of("B", "2");
    
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
    ObjectId test = ObjectId.of("A", "1");
    test.compareTo(null);
  }

  //-------------------------------------------------------------------------
  public void test_equals() {
    ObjectId d1a = ObjectId.of("Scheme", "d1");
    ObjectId d1b = ObjectId.of("Scheme", "d1");
    ObjectId d2 = ObjectId.of("Scheme", "d2");
    
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
    ObjectId d1a = ObjectId.of("Scheme", "d1");
    ObjectId d1b = ObjectId.of("Scheme", "d1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

}
