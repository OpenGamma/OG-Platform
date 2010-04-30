/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test Identifier. 
 */
public class IdentifierTest {

  private static final IdentificationScheme SCHEME = new IdentificationScheme("Scheme");
  private static final IdentificationScheme OTHER_SCHEME = new IdentificationScheme("Other");

  public void test_constructor_IdentificationScheme_String() {
    Identifier test = new Identifier(SCHEME, "foo");
    assertEquals("Scheme", test.getScheme().getName());
    assertEquals("foo", test.getValue());
  }

  @Test(expected=NullPointerException.class)
  public void test_constructor_IdentificationScheme_String_nullScheme() {
    new Identifier((IdentificationScheme) null, "foo");
  }

  @Test(expected=NullPointerException.class)
  public void test_constructor_IdentificationScheme_String_nullId() {
    new Identifier(SCHEME, (String) null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_IdentificationScheme_String_emptyId() {
    new Identifier(SCHEME, "");
  }

  public void test_constructor_String_String() {
    Identifier test = new Identifier("Scheme", "foo");
    assertEquals("Scheme", test.getScheme().getName());
    assertEquals("foo", test.getValue());
  }

  @Test(expected=NullPointerException.class)
  public void test_constructor_String_String_nullScheme() {
    new Identifier((String) null, "foo");
  }

  @Test(expected=NullPointerException.class)
  public void test_constructor_String_String_nullId() {
    new Identifier(SCHEME, (String) null);
    assertEquals(new IdentificationScheme("Bloomberg"), (new Identifier("Bloomberg", "foo")).getScheme());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_isScheme_IdentificationScheme() {
    Identifier test = new Identifier(SCHEME, "value");
    assertEquals(test.isScheme(SCHEME), true);
    assertEquals(test.isScheme(OTHER_SCHEME), false);
    assertEquals(test.isScheme((IdentificationScheme) null), false);
  }

  @Test
  public void test_isNotScheme_IdentificationScheme() {
    Identifier test = new Identifier(SCHEME, "value");
    assertEquals(test.isNotScheme(SCHEME), false);
    assertEquals(test.isNotScheme(OTHER_SCHEME), true);
    assertEquals(test.isNotScheme((IdentificationScheme) null), true);
  }

  @Test
  public void test_isScheme_String() {
    Identifier test = new Identifier(SCHEME, "value");
    assertEquals(test.isScheme("Scheme"), true);
    assertEquals(test.isScheme("Other"), false);
    assertEquals(test.isScheme((String) null), false);
  }

  @Test
  public void test_isNotScheme_String() {
    Identifier test = new Identifier(SCHEME, "value");
    assertEquals(test.isNotScheme("Scheme"), false);
    assertEquals(test.isNotScheme("Other"), true);
    assertEquals(test.isNotScheme((String) null), true);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals() {
    Identifier d1a = new Identifier(SCHEME, "d1");
    Identifier d1b = new Identifier(SCHEME, "d1");
    Identifier d2 = new Identifier(SCHEME, "d2");
    
    assertEquals(d1a.equals(d1a), true);
    assertEquals(d1a.equals(d1b), true);
    assertEquals(d1a.equals(d2), false);
    
    assertEquals(d1b.equals(d1a), true);
    assertEquals(d1b.equals(d1b), true);
    assertEquals(d1b.equals(d2), false);
    
    assertEquals(d2.equals(d1a), false);
    assertEquals(d2.equals(d1b), false);
    assertEquals(d2.equals(d2), true);
    
    assertEquals(d1b.equals("d1"), false);
    assertEquals(d1b.equals(null), false);
  }

  @Test
  public void test_hashCode() {
    Identifier d1a = new Identifier(SCHEME, "d1");
    Identifier d1b = new Identifier(SCHEME, "d1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

}
