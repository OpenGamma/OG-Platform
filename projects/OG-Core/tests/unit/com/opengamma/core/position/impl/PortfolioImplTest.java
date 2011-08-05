/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertEquals;
import org.testng.annotations.Test;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test PortfolioImpl.
 */
@Test
public class PortfolioImplTest {

  public void test_construction_String() {
    PortfolioImpl test = new PortfolioImpl("Name");
    assertEquals(null, test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(true, PortfolioNodeImpl.class.isAssignableFrom(test.getRootNode().getClass()));
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_String_null() {
    new PortfolioImpl((String) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_PortfolioIdString() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    assertEquals(id("Scheme", "Id"), test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(true, PortfolioNodeImpl.class.isAssignableFrom(test.getRootNode().getClass()));
    assertEquals(0, test.getRootNode().size());
    assertEquals("Portfolio[Scheme~Id]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdString_nullId() {
    new PortfolioImpl(null, "Name");
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdString_nullName() {
    new PortfolioImpl(id("Scheme", "Id"), null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_PortfolioIdStringNode() {
    PortfolioNodeImpl root = new PortfolioNodeImpl();
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name", root);
    assertEquals(id("Scheme", "Id"), test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(true, test.getRootNode() == root);
    assertEquals("Portfolio[Scheme~Id]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdStringNode_nullId() {
    new PortfolioImpl(null, "Name", new PortfolioNodeImpl());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdStringNode_nullName() {
    new PortfolioImpl(id("Scheme", "Id"), null, new PortfolioNodeImpl());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_PortfolioIdStringNode_nullRoot() {
    new PortfolioImpl(id("Scheme", "Id"), "Name", null);
  }

  private UniqueIdentifier id(String scheme, String value) {
    return UniqueIdentifier.of(scheme, value);
  }

  //-------------------------------------------------------------------------
  public void test_setUniqueId() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setUniqueId(id("Scheme2", "Id2"));
    assertEquals(id("Scheme2", "Id2"), test.getUniqueId());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setUniqueId_null() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setUniqueId(null);
  }

  //-------------------------------------------------------------------------
  public void test_setName() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setName("Name2");
    assertEquals("Name2", test.getName());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setName_null() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setName(null);
  }

  //-------------------------------------------------------------------------
  public void test_setRootNode() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    PortfolioNodeImpl root = new PortfolioNodeImpl();
    test.setRootNode(root);
    assertSame(root, test.getRootNode());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setRootNode_null() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setRootNode(null);
  }

}
