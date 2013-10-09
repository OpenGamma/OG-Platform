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
 * Test {@link UniqueId}. 
 */
@Test(groups = TestGroup.UNIT)
public class UniqueIdTest {

  public void test_factory_String_String() {
    UniqueId test = UniqueId.of("Scheme", "value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullScheme() {
    UniqueId.of((String) null, "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_emptyScheme() {
    UniqueId.of("", "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullValue() {
    UniqueId.of("Scheme", (String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_emptyValue() {
    UniqueId.of("Scheme", "");
  }

  // [PLAT-1543] Fix UniqueId and enable the test
  @Test(enabled = false)
  public void testStringEscaping() {
    final String[] strs = new String[] {"Foo", "~Foo", "Foo~", "~Foo~", "~", "~~", "~~~" };
    for (String scheme : strs) {
      for (String value : strs) {
        UniqueId testUID = UniqueId.of(scheme, value);
        String testStr = testUID.toString();
        // System.out.println("scheme = " + scheme + ", value = " + value + ", version = NULL, uid = " + testUID.toString());
        UniqueId uid = UniqueId.parse(testStr);
        assertEquals(testUID, uid);
        for (String version : strs) {
          testUID = UniqueId.of(scheme, value, version);
          testStr = testUID.toString();
          // System.out.println("scheme = " + scheme + ", value = " + value + ", version = " + version + ", uid = " + testUID.toString());
          uid = UniqueId.parse(testStr);
          assertEquals(testUID, uid);
        }
      }
    }
  }

  //-------------------------------------------------------------------------
  public void test_factory_String_String_String() {
    UniqueId test = UniqueId.of("Scheme", "value", "version");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("version", test.getVersion());
    assertEquals("Scheme~value~version", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_String_nullScheme() {
    UniqueId.of((String) null, "value", "version");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_String_emptyScheme() {
    UniqueId.of("", "value", "version");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_String_nullValue() {
    UniqueId.of("Scheme", (String) null, "version");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_String_emptyValue() {
    UniqueId.of("Scheme", "", "version");
  }

  public void test_factory_String_String_String_nullVersion() {
    UniqueId test = UniqueId.of("Scheme", "value", null);
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  public void test_factory_String_String_String_emptyVersion() {
    UniqueId test = UniqueId.of("Scheme", "value", "");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  //-------------------------------------------------------------------------
  public void test_factory_ObjectId_String() {
    UniqueId test = UniqueId.of(ObjectId.of("Scheme", "value"), "version");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("version", test.getVersion());
    assertEquals("Scheme~value~version", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_ObjectId_String_nullObjectId() {
    UniqueId.of((ObjectId) null, "version");
  }

  public void test_factory_ObjectId_String_nullVersion() {
    UniqueId test = UniqueId.of(ObjectId.of("Scheme", "value"), null);
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  public void test_factory_ObjectId_String_emptyVersion() {
    UniqueId test = UniqueId.of(ObjectId.of("Scheme", "value"), "");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  //-------------------------------------------------------------------------
  public void test_parse_version() {
    UniqueId test = UniqueId.parse("Scheme~value~version");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("version", test.getVersion());
    assertEquals("Scheme~value~version", test.toString());
  }

  public void test_parse_noVersion() {
    UniqueId test = UniqueId.parse("Scheme~value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat1() {
    UniqueId.parse("Scheme");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat2() {
    UniqueId.parse("Scheme:value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat3() {
    UniqueId.parse("Scheme~value~version~other");
  }

  //-------------------------------------------------------------------------
  public void test_withScheme() {
    UniqueId test = UniqueId.of("id1", "value1", "32");
    assertEquals(UniqueId.of("scheme", "value1", "32"), test.withScheme("scheme"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withScheme_null() {
    UniqueId test = UniqueId.of("id1", "value1", "32");
    test.withScheme(null);
  }

  //-------------------------------------------------------------------------
  public void test_withValue() {
    UniqueId test = UniqueId.of("id1", "value1", "32");
    assertEquals(UniqueId.of("id1", "newValue", "32"), test.withValue("newValue"));
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_withValue_null() {
    UniqueId test = UniqueId.of("id1", "value1", "32");
    test.withValue(null);
  }

  //-------------------------------------------------------------------------
  public void test_withVersion_added() {
    UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1", "32"), test.withVersion("32"));
  }

  public void test_withVersion_replaced() {
    UniqueId test = UniqueId.of("id1", "value1", "12");
    assertEquals(UniqueId.of("id1", "value1", "32"), test.withVersion("32"));
  }

  public void test_withVersion_replacedToNull() {
    UniqueId test = UniqueId.of("id1", "value1", "32");
    assertEquals(UniqueId.of("id1", "value1"), test.withVersion(null));
  }

  public void test_withVersion_replacedToSame() {
    UniqueId test = UniqueId.of("id1", "value1", "32");
    assertSame(test, test.withVersion("32"));
  }

  //-------------------------------------------------------------------------
  public void test_getObjectId() {
    UniqueId test = UniqueId.of("id1", "value1", "version");
    assertEquals(ObjectId.of("id1", "value1"), test.getObjectId());
  }

  //-------------------------------------------------------------------------
  public void test_getUniqueId() {
    UniqueId test = UniqueId.of("id1", "value1");
    assertSame(test, test.getUniqueId());
  }

  //-------------------------------------------------------------------------
  public void test_isLatest_noVersion() {
    UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(true, test.isLatest());
  }

  public void test_isLatest_version() {
    UniqueId test = UniqueId.of("id1", "value1", "1");
    assertEquals(false, test.isLatest());
  }

  public void test_isVersioned_noVersion() {
    UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(false, test.isVersioned());
  }

  public void test_isVersioned_version() {
    UniqueId test = UniqueId.of("id1", "value1", "1");
    assertEquals(true, test.isVersioned());
  }

  public void test_toLatest_noVersion() {
    UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(UniqueId.of("id1", "value1"), test.toLatest());
  }

  public void test_toLatest_version() {
    UniqueId test = UniqueId.of("id1", "value1", "1");
    assertEquals(UniqueId.of("id1", "value1"), test.toLatest());
  }

  //-------------------------------------------------------------------------
  public void test_toExternalId() {
    UniqueId test = UniqueId.of("id1", "value1");
    assertEquals(ExternalId.of(UniqueId.EXTERNAL_SCHEME, "id1~value1"), test.toExternalId());
  }

  //-------------------------------------------------------------------------
  public void test_equalObjectId_noVersion() {
    UniqueId d1a = UniqueId.of("Scheme", "d1");
    UniqueId d1b = UniqueId.of("Scheme", "d1");
    UniqueId d2 = UniqueId.of("Scheme", "d2");
    
    assertEquals(true, d1a.equalObjectId(d1a));
    assertEquals(true, d1a.equalObjectId(d1b));
    assertEquals(false, d1a.equalObjectId(d2));
    
    assertEquals(true, d1b.equalObjectId(d1a));
    assertEquals(true, d1b.equalObjectId(d1b));
    assertEquals(false, d1b.equalObjectId(d2));
    
    assertEquals(false, d2.equalObjectId(d1a));
    assertEquals(false, d2.equalObjectId(d1b));
    assertEquals(true, d2.equalObjectId(d2));
  }

  public void test_equalObjectId_version() {
    UniqueId d1 = UniqueId.of("Scheme", "d1", "1");
    UniqueId d2 = UniqueId.of("Scheme", "d1", "2");
    
    assertEquals(true, d1.equalObjectId(d2));
  }

  public void test_equalObjectId_scheme() {
    UniqueId d1 = UniqueId.of("Scheme", "d1", "1");
    UniqueId d2 = UniqueId.of("Other", "d1", "2");
    
    assertEquals(false, d1.equalObjectId(d2));
  }

  public void test_equalObjectId_null() {
    UniqueId d1 = UniqueId.of("Scheme", "d1", "1");
    
    assertEquals(false, d1.equalObjectId(null));
  }

  //-------------------------------------------------------------------------
  public void test_compareTo_noVersion() {
    UniqueId a = UniqueId.of("A", "1");
    UniqueId b = UniqueId.of("A", "2");
    UniqueId c = UniqueId.of("B", "2");
    
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

  public void test_compareTo_versionOnly() {
    UniqueId a = UniqueId.of("A", "1", null);
    UniqueId b = UniqueId.of("A", "1", "4");
    UniqueId c = UniqueId.of("A", "1", "5");
    
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

  public void test_compareTo_valueBeatsVersion() {
    UniqueId a = UniqueId.of("A", "1", "5");
    UniqueId b = UniqueId.of("A", "2", "4");
    
    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);
    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  public void test_compareTo_schemeBeatsValue() {
    UniqueId a = UniqueId.of("A", "2", "1");
    UniqueId b = UniqueId.of("B", "1", "1");
    
    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);
    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_compareTo_null() {
    UniqueId test = UniqueId.of("A", "1");
    test.compareTo(null);
  }

  //-------------------------------------------------------------------------
  public void test_equals_noVersion() {
    UniqueId d1a = UniqueId.of("Scheme", "d1");
    UniqueId d1b = UniqueId.of("Scheme", "d1");
    UniqueId d2 = UniqueId.of("Scheme", "d2");
    
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

  public void test_equals_version() {
    UniqueId d1a = UniqueId.of("Scheme", "d1", "1");
    UniqueId d1b = UniqueId.of("Scheme", "d1", "1");
    UniqueId d2 = UniqueId.of("Scheme", "d2", null);
    
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

  public void test_equals_differentScheme() {
    UniqueId d1 = UniqueId.of("Scheme", "d1", "1");
    UniqueId d2 = UniqueId.of("Other", "d1", "1");
    
    assertEquals(true, d1.equals(d1));
    assertEquals(false, d1.equals(d2));
    assertEquals(false, d2.equals(d1));
    assertEquals(true, d2.equals(d2));
  }

  public void test_hashCode_noVersion() {
    UniqueId d1a = UniqueId.of("Scheme", "d1");
    UniqueId d1b = UniqueId.of("Scheme", "d1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  public void test_hashCode_version() {
    UniqueId d1a = UniqueId.of("Scheme", "d1", "1");
    UniqueId d1b = UniqueId.of("Scheme", "d1", "1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

}
