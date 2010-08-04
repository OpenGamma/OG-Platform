/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.id;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * A pure unit test for {@link IdentificationScheme}.
 */
public class IdentificationSchemeTest {

  public void test_constructor() {
    IdentificationScheme test = new IdentificationScheme("IATA");
    assertEquals("IATA", test.getName());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_noNameProvided() {
    new IdentificationScheme(null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_emptyNameProvided() {
    new IdentificationScheme("");
  }

  @Test
  public void test_compareTo() {
    IdentificationScheme d1 = new IdentificationScheme("d1");
    IdentificationScheme d2 = new IdentificationScheme("d2");
    
    assertEquals(d1.compareTo(d1) == 0, true);
    assertEquals(d1.compareTo(d2) < 0, true);
    
    assertEquals(d2.compareTo(d1) > 0, true);
    assertEquals(d2.compareTo(d2) == 0, true);
  }

  @Test
  public void test_equals() {
    IdentificationScheme d1a = new IdentificationScheme("d1");
    IdentificationScheme d1b = new IdentificationScheme("d1");
    IdentificationScheme d2 = new IdentificationScheme("d2");
    
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
    IdentificationScheme d1a = new IdentificationScheme("d1");
    IdentificationScheme d1b = new IdentificationScheme("d1");
    
    assertEquals(d1a.hashCode(), d1b.hashCode());
  }

  @Test
  public void test_toString() {
    IdentificationScheme test = new IdentificationScheme("Scheme");
    assertEquals("Scheme", test.toString());
  }

}
