/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.opengamma.id.Identifier;

/**
 * Test PortfolioImpl.
 */
public class PortfolioImplTest {

  @Test
  public void test_construction_String() {
    PortfolioImpl test = new PortfolioImpl("Id");
    assertEquals(id("Basic", "Id"), test.getIdentityKey());
    assertEquals("Id", test.getName());
    assertEquals(true, test.getRootNode() instanceof PortfolioNodeImpl);
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[Basic::Id]", test.toString());
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
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    assertEquals(id("Scheme", "Id"), test.getIdentityKey());
    assertEquals("Name", test.getName());
    assertEquals(true, test.getRootNode() instanceof PortfolioNodeImpl);
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[Scheme::Id]", test.toString());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdString_nullId() {
    new PortfolioImpl(null, "Name");
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdString_nullName() {
    new PortfolioImpl(id("Scheme", "Id"), null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_PortfolioIdStringNode() {
    PortfolioNodeImpl root = new PortfolioNodeImpl("Foo");
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name", root);
    assertEquals(id("Scheme", "Id"), test.getIdentityKey());
    assertEquals("Name", test.getName());
    assertEquals(true, test.getRootNode() == root);
    assertEquals("Portfolio[Scheme::Id]", test.toString());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdStringNode_nullId() {
    new PortfolioImpl(null, "Name", new PortfolioNodeImpl());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdStringNode_nullName() {
    new PortfolioImpl(id("Scheme", "Id"), null, new PortfolioNodeImpl());
  }

  @Test(expected=NullPointerException.class)
  public void test_construction_PortfolioIdStringNode_nullRoot() {
    new PortfolioImpl(id("Scheme", "Id"), "Name", null);
  }

  private Identifier id(String scheme, String value) {
    return new Identifier(scheme, value);
  }

}
