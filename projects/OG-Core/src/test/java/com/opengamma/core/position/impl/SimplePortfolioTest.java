/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertSame;

import org.testng.annotations.Test;

import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimplePortfolio}.
 */
@Test(groups = TestGroup.UNIT)
public class SimplePortfolioTest {

  public void test_construction_String() {
    SimplePortfolio test = new SimplePortfolio("Name");
    assertEquals(null, test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(true, SimplePortfolioNode.class.isAssignableFrom(test.getRootNode().getClass()));
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_String_null() {
    new SimplePortfolio((String) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_PortfolioIdString() {
    SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    assertEquals(id("Scheme", "Id"), test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(true, SimplePortfolioNode.class.isAssignableFrom(test.getRootNode().getClass()));
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[Scheme~Id]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdString_nullId() {
    new SimplePortfolio(null, "Name");
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdString_nullName() {
    new SimplePortfolio(id("Scheme", "Id"), null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_PortfolioIdStringNode() {
    SimplePortfolioNode root = new SimplePortfolioNode();
    SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name", root);
    assertEquals(id("Scheme", "Id"), test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(true, test.getRootNode() == root);
    assertEquals("Portfolio[Scheme~Id]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdStringNode_nullId() {
    new SimplePortfolio(null, "Name", new SimplePortfolioNode());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdStringNode_nullName() {
    new SimplePortfolio(id("Scheme", "Id"), null, new SimplePortfolioNode());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdStringNode_nullRoot() {
    new SimplePortfolio(id("Scheme", "Id"), "Name", null);
  }

  private UniqueId id(String scheme, String value) {
    return UniqueId.of(scheme, value);
  }

  //-------------------------------------------------------------------------
  public void test_setUniqueId() {
    SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setUniqueId(id("Scheme2", "Id2"));
    assertEquals(id("Scheme2", "Id2"), test.getUniqueId());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setUniqueId_null() {
    SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setUniqueId(null);
  }

  //-------------------------------------------------------------------------
  public void test_setName() {
    SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setName("Name2");
    assertEquals("Name2", test.getName());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setName_null() {
    SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setName(null);
  }

  //-------------------------------------------------------------------------
  public void test_setRootNode() {
    SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    SimplePortfolioNode root = new SimplePortfolioNode();
    test.setRootNode(root);
    assertSame(root, test.getRootNode());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setRootNode_null() {
    SimplePortfolio test = new SimplePortfolio(id("Scheme", "Id"), "Name");
    test.setRootNode(null);
  }

}
