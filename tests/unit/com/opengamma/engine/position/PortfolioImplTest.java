/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test PortfolioImpl.
 */
public class PortfolioImplTest {

  @Test
  public void test_construction_String() {
    PortfolioImpl test = new PortfolioImpl("NameAndId");
    assertEquals(PortfolioId.of("NameAndId"), test.getId());
    assertEquals("NameAndId", test.getName());
    assertEquals(true, test.getRootNode() instanceof PortfolioNodeImpl);
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[NameAndId]", test.toString());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_String_null() {
    new PortfolioImpl(null);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_String_empty() {
    new PortfolioImpl("");
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_PortfolioIdString() {
    PortfolioImpl test = new PortfolioImpl(PortfolioId.of("Id"), "Name");
    assertEquals(PortfolioId.of("Id"), test.getId());
    assertEquals("Name", test.getName());
    assertEquals(true, test.getRootNode() instanceof PortfolioNodeImpl);
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[Id]", test.toString());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdString_nullId() {
    new PortfolioImpl(null, "Name");
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdString_nullName() {
    new PortfolioImpl(PortfolioId.of("Id"), null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_PortfolioIdStringNode() {
    PortfolioNodeImpl root = new PortfolioNodeImpl("Foo");
    PortfolioImpl test = new PortfolioImpl(PortfolioId.of("Id"), "Name", root);
    assertEquals(PortfolioId.of("Id"), test.getId());
    assertEquals("Name", test.getName());
    assertEquals(true, test.getRootNode() == root);
    assertEquals("Portfolio[Id]", test.toString());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdStringNode_nullId() {
    new PortfolioImpl(null, "Name", new PortfolioNodeImpl());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdStringNode_nullName() {
    new PortfolioImpl(PortfolioId.of("Id"), null, new PortfolioNodeImpl());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdStringNode_nullRoot() {
    new PortfolioImpl(PortfolioId.of("Id"), "Name", null);
  }

}
