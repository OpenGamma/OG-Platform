/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import org.junit.Test;

/**
 * Test IdentificationScheme.
 */
public class IdentificationSchemeTest {

  public void test_factory() {
    IdentificationScheme test = IdentificationScheme.of("IATA");
    assertEquals("IATA", test.getName());
  }

  public void test_factory_cached() {
    assertSame(IdentificationScheme.of("ISO"), IdentificationScheme.of("ISO"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_factory_null() {
    IdentificationScheme.of(null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_factory_emptyName() {
    IdentificationScheme.of("");
  }

  @Test
  public void test_compareTo() {
    IdentificationScheme d1 = IdentificationScheme.of("d1");
    IdentificationScheme d2 = IdentificationScheme.of("d2");
    
    assertEquals(d1.compareTo(d1) == 0, true);
    assertEquals(d1.compareTo(d2) < 0, true);
    
    assertEquals(d2.compareTo(d1) > 0, true);
    assertEquals(d2.compareTo(d2) == 0, true);
  }

  @Test
  public void test_equals() {
    IdentificationScheme d1a = IdentificationScheme.of("d1");
    IdentificationScheme d1b = IdentificationScheme.of("d1");
    IdentificationScheme d2 = IdentificationScheme.of("d2");
    
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
    IdentificationScheme d1a = IdentificationScheme.of("d1");
    IdentificationScheme d1b = IdentificationScheme.of("d1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  @Test
  public void test_toString() {
    IdentificationScheme test = IdentificationScheme.of("Scheme");
    assertEquals("Scheme", test.toString());
  }

}
