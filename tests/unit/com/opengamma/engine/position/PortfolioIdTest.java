/**
 * Copyright (C) 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test PortfolioId.
 */
public class PortfolioIdTest {

  @Test
  public void test_construction() {
    PortfolioId test = PortfolioId.of("Id");
    assertEquals("Id", test.getValue());
    assertEquals("Id", test.toString());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_nullId() {
    PortfolioId.of(null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_emptyId() {
    PortfolioId.of("");
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_compareTo() {
    PortfolioId a = PortfolioId.of("A");
    PortfolioId b = PortfolioId.of("B");
    
    assertEquals(true, a.compareTo(a) == 0);
    assertEquals(true, a.compareTo(b) < 0);
    assertEquals(true, b.compareTo(a) > 0);
    assertEquals(true, b.compareTo(b) == 0);
  }

  @Test
  public void test_equals() {
    PortfolioId a1 = PortfolioId.of("A");
    PortfolioId a2 = PortfolioId.of("A");
    PortfolioId b = PortfolioId.of("B");
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(false, a1.equals(b));
    
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
    assertEquals(false, a2.equals(b));
    
    assertEquals(false, b.equals(a1));
    assertEquals(false, b.equals(a2));
    assertEquals(true, b.equals(b));
  }

  @Test
  public void test_hashCode() {
    PortfolioId a1 = PortfolioId.of("A");
    PortfolioId a2 = PortfolioId.of("A");
    
    assertEquals(a2.hashCode(), a1.hashCode());
  }

}
