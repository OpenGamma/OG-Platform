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
 * Test UniqueIdentifier. 
 */
@Test
public class UniqueIdentifierTest {

  public void test_factory_String_String() {
    UniqueIdentifier test = UniqueIdentifier.of("Scheme", "value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullScheme() {
    UniqueIdentifier.of((String) null, "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_emptyScheme() {
    UniqueIdentifier.of("", "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullValue() {
    UniqueIdentifier.of("Scheme", (String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_emptyValue() {
    UniqueIdentifier.of("Scheme", "");
  }

  //-------------------------------------------------------------------------
  public void test_factory_String_String_String() {
    UniqueIdentifier test = UniqueIdentifier.of("Scheme", "value", "version");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("version", test.getVersion());
    assertEquals("Scheme~value~version", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_String_nullScheme() {
    UniqueIdentifier.of((String) null, "value", "version");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_String_emptyScheme() {
    UniqueIdentifier.of("", "value", "version");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_String_nullValue() {
    UniqueIdentifier.of("Scheme", (String) null, "version");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_String_emptyValue() {
    UniqueIdentifier.of("Scheme", "", "version");
  }

  public void test_factory_String_String_String_nullVersion() {
    UniqueIdentifier test = UniqueIdentifier.of("Scheme", "value", null);
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  public void test_factory_String_String_String_emptyVersion() {
    UniqueIdentifier test = UniqueIdentifier.of("Scheme", "value", "");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals(null, test.getVersion());
    assertEquals("Scheme~value", test.toString());
  }

  //-------------------------------------------------------------------------
  public void test_parse_version() {
    UniqueIdentifier test = UniqueIdentifier.parse("Scheme~value~version");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("version", test.getVersion());
    assertEquals("Scheme~value~version", test.toString());
  }

  public void test_parse_noVersion() {
    UniqueIdentifier test = UniqueIdentifier.parse("Scheme~value");
    assertEquals("Scheme", test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat1() {
    UniqueIdentifier.parse("Scheme");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat2() {
    UniqueIdentifier.parse("Scheme:value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat3() {
    UniqueIdentifier.parse("Scheme~value~version~other");
  }

  //-------------------------------------------------------------------------
  public void test_withVersion_added() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1");
    assertEquals(UniqueIdentifier.of("id1", "value1", "32"), test.withVersion("32"));
  }

  public void test_withVersion_replaced() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1", "12");
    assertEquals(UniqueIdentifier.of("id1", "value1", "32"), test.withVersion("32"));
  }

  public void test_withVersion_replacedToNull() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1", "32");
    assertEquals(UniqueIdentifier.of("id1", "value1"), test.withVersion(null));
  }

  public void test_withVersion_replacedToSame() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1", "32");
    assertSame(test, test.withVersion("32"));
  }

  //-------------------------------------------------------------------------
  public void test_getObjectId() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1", "version");
    assertEquals(ObjectIdentifier.of("id1", "value1"), test.getObjectId());
  }

  //-------------------------------------------------------------------------
  public void test_getUniqueId() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1");
    assertSame(test, test.getUniqueId());
  }

  //-------------------------------------------------------------------------
  public void test_isLatest_noVersion() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1");
    assertEquals(true, test.isLatest());
  }

  public void test_isLatest_version() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1", "1");
    assertEquals(false, test.isLatest());
  }

  public void test_isVersioned_noVersion() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1");
    assertEquals(false, test.isVersioned());
  }

  public void test_isVersioned_version() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1", "1");
    assertEquals(true, test.isVersioned());
  }

  public void test_toLatest_noVersion() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1");
    assertEquals(UniqueIdentifier.of("id1", "value1"), test.toLatest());
  }

  public void test_toLatest_version() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1", "1");
    assertEquals(UniqueIdentifier.of("id1", "value1"), test.toLatest());
  }

  //-------------------------------------------------------------------------
  public void test_toIdentifier() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1");
    assertEquals(Identifier.of(UniqueIdentifier.UID, "id1~value1"), test.toIdentifier());
  }

  //-------------------------------------------------------------------------
  public void test_equalObjectIdentifier_noVersion() {
    UniqueIdentifier d1a = UniqueIdentifier.of("Scheme", "d1");
    UniqueIdentifier d1b = UniqueIdentifier.of("Scheme", "d1");
    UniqueIdentifier d2 = UniqueIdentifier.of("Scheme", "d2");
    
    assertEquals(true, d1a.equalObjectIdentifier(d1a));
    assertEquals(true, d1a.equalObjectIdentifier(d1b));
    assertEquals(false, d1a.equalObjectIdentifier(d2));
    
    assertEquals(true, d1b.equalObjectIdentifier(d1a));
    assertEquals(true, d1b.equalObjectIdentifier(d1b));
    assertEquals(false, d1b.equalObjectIdentifier(d2));
    
    assertEquals(false, d2.equalObjectIdentifier(d1a));
    assertEquals(false, d2.equalObjectIdentifier(d1b));
    assertEquals(true, d2.equalObjectIdentifier(d2));
  }

  public void test_equalObjectIdentifier_version() {
    UniqueIdentifier d1 = UniqueIdentifier.of("Scheme", "d1", "1");
    UniqueIdentifier d2 = UniqueIdentifier.of("Scheme", "d1", "2");
    
    assertEquals(true, d1.equalObjectIdentifier(d2));
  }

  public void test_equalObjectIdentifier_null() {
    UniqueIdentifier d1 = UniqueIdentifier.of("Scheme", "d1", "1");
    
    assertEquals(false, d1.equalObjectIdentifier(null));
  }

  //-------------------------------------------------------------------------
  public void test_compareTo_noVersion() {
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

  public void test_compareTo_versionOnly() {
    UniqueIdentifier a = UniqueIdentifier.of("A", "1", null);
    UniqueIdentifier b = UniqueIdentifier.of("A", "1", "4");
    UniqueIdentifier c = UniqueIdentifier.of("A", "1", "5");
    
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
    UniqueIdentifier a = UniqueIdentifier.of("A", "1", "5");
    UniqueIdentifier b = UniqueIdentifier.of("A", "2", "4");
    
    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);
    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  public void test_compareTo_schemeBeatsValue() {
    UniqueIdentifier a = UniqueIdentifier.of("A", "2", "1");
    UniqueIdentifier b = UniqueIdentifier.of("B", "1", "1");
    
    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);
    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_compareTo_null() {
    UniqueIdentifier test = UniqueIdentifier.of("A", "1");
    test.compareTo(null);
  }

  //-------------------------------------------------------------------------
  public void test_equals_noVersion() {
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

  public void test_equals_version() {
    UniqueIdentifier d1a = UniqueIdentifier.of("Scheme", "d1", "1");
    UniqueIdentifier d1b = UniqueIdentifier.of("Scheme", "d1", "1");
    UniqueIdentifier d2 = UniqueIdentifier.of("Scheme", "d2", null);
    
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

  public void test_hashCode_noVersion() {
    UniqueIdentifier d1a = UniqueIdentifier.of("Scheme", "d1");
    UniqueIdentifier d1b = UniqueIdentifier.of("Scheme", "d1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  public void test_hashCode_version() {
    UniqueIdentifier d1a = UniqueIdentifier.of("Scheme", "d1", "1");
    UniqueIdentifier d1b = UniqueIdentifier.of("Scheme", "d1", "1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  //-------------------------------------------------------------------------
  public void test_fudgeEncoding_noVersion() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1");
    FudgeMsg msg = test.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    UniqueIdentifier decoded = UniqueIdentifier.fromFudgeMsg(msg);
    assertEquals(test, decoded);
  }

  public void test_fudgeEncoding_version() {
    UniqueIdentifier test = UniqueIdentifier.of("id1", "value1", "1");
    FudgeMsg msg = test.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(3, msg.getNumFields());
    
    UniqueIdentifier decoded = UniqueIdentifier.fromFudgeMsg(msg);
    assertEquals(test, decoded);
  }

}
