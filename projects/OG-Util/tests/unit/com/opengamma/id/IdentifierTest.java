/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.Test;

/**
 * Test Identifier. 
 */
@Test
public class IdentifierTest {

  private static final IdentificationScheme SCHEME = IdentificationScheme.of("Scheme");
  private static final IdentificationScheme OTHER_SCHEME = IdentificationScheme.of("Other");

  public void test_factory_IdentificationScheme_String() {
    Identifier test = Identifier.of(SCHEME, "value");
    assertEquals("Scheme", test.getScheme().getName());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_IdentificationScheme_String_nullScheme() {
    Identifier.of((IdentificationScheme) null, "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_IdentificationScheme_String_nullValue() {
    Identifier.of(SCHEME, (String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_IdentificationScheme_String_emptyValue() {
    Identifier.of(SCHEME, "");
  }

  public void test_factory_String_String() {
    Identifier test = Identifier.of("Scheme", "value");
    assertEquals("Scheme", test.getScheme().getName());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullScheme() {
    Identifier.of((String) null, "value");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_nullValue() {
    Identifier.of(SCHEME, (String) null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_factory_String_String_emptyValue() {
    Identifier.of(SCHEME, "");
  }

  //-------------------------------------------------------------------------
  public void test_parse() {
    Identifier test = Identifier.parse("Scheme~value");
    assertEquals(SCHEME, test.getScheme());
    assertEquals("value", test.getValue());
    assertEquals("Scheme~value", test.toString());
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_parse_invalidFormat() {
    Identifier.parse("Scheme:value");
  }

  //-------------------------------------------------------------------------
  public void test_isScheme_IdentificationScheme() {
    Identifier test = Identifier.of(SCHEME, "value");
    assertEquals(true, test.isScheme(SCHEME));
    assertEquals(false, test.isScheme(OTHER_SCHEME));
    assertEquals(false, test.isScheme((IdentificationScheme) null));
  }

  public void test_isNotScheme_IdentificationScheme() {
    Identifier test = Identifier.of(SCHEME, "value");
    assertEquals(false, test.isNotScheme(SCHEME));
    assertEquals(true, test.isNotScheme(OTHER_SCHEME));
    assertEquals(true, test.isNotScheme((IdentificationScheme) null));
  }

  public void test_isScheme_String() {
    Identifier test = Identifier.of(SCHEME, "value");
    assertEquals(true, test.isScheme("Scheme"));
    assertEquals(false, test.isScheme("Other"));
    assertEquals(false, test.isScheme((String) null));
  }

  public void test_isNotScheme_String() {
    Identifier test = Identifier.of(SCHEME, "value");
    assertEquals(false, test.isNotScheme("Scheme"));
    assertEquals(true, test.isNotScheme("Other"));
    assertEquals(true, test.isNotScheme((String) null));
  }

  //-------------------------------------------------------------------------
  public void test_getIdentityKey() {
    Identifier test = Identifier.of(SCHEME, "value");
    assertEquals(test, test.getIdentityKey());
  }

  //-------------------------------------------------------------------------
  public void test_toBundle() {
    Identifier test = Identifier.of(SCHEME, "value");
    assertEquals(IdentifierBundle.of(test), test.toBundle());
  }

  //-------------------------------------------------------------------------
  public void test_equals() {
    Identifier d1a = Identifier.of(SCHEME, "d1");
    Identifier d1b = Identifier.of(SCHEME, "d1");
    Identifier d2 = Identifier.of(SCHEME, "d2");
    
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
    Identifier d1a = Identifier.of(SCHEME, "d1");
    Identifier d1b = Identifier.of(SCHEME, "d1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  //-------------------------------------------------------------------------
  public void test_fudgeEncoding() {
    Identifier test = Identifier.of("id1", "value1");
    FudgeMsg msg = test.toFudgeMsg(new FudgeContext());
    assertNotNull(msg);
    assertEquals(2, msg.getNumFields());
    
    Identifier decoded = Identifier.fromFudgeMsg(msg);
    assertEquals(test, decoded);
  }

}
