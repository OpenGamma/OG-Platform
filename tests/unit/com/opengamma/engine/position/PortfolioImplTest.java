/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;

import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test PortfolioImpl.
 */
public class PortfolioImplTest {

  @Test
  public void test_construction_String() {
    PortfolioImpl test = new PortfolioImpl("Basic::Id");
    assertEquals(id("Basic", "Id"), test.getUniqueIdentifier());
    assertEquals("Basic::Id", test.getName());
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
    assertEquals(id("Scheme", "Id"), test.getUniqueIdentifier());
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
    PortfolioNodeImpl root = new PortfolioNodeImpl();
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name", root);
    assertEquals(id("Scheme", "Id"), test.getUniqueIdentifier());
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

  private UniqueIdentifier id(String scheme, String value) {
    return UniqueIdentifier.of(scheme, value);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setUniqueIdentifier() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setUniqueIdentifier(id("Scheme2", "Id2"));
    assertEquals(id("Scheme2", "Id2"), test.getUniqueIdentifier());
  }

  @Test(expected=NullPointerException.class)
  public void test_setUniqueIdentifier_null() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setUniqueIdentifier(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setName() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setName("Name2");
    assertEquals("Name2", test.getName());
  }

  @Test(expected=NullPointerException.class)
  public void test_setName_null() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setName(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setRootNode() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    PortfolioNodeImpl root = new PortfolioNodeImpl();
    test.setRootNode(root);
    assertSame(root, test.getRootNode());
  }

  @Test(expected=NullPointerException.class)
  public void test_setRootNode_null() {
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name");
    test.setRootNode(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getNode_Identifier() {
    PortfolioNodeImpl root = new PortfolioNodeImpl(UniqueIdentifier.of("Root", "A"), "Name");
    PortfolioNodeImpl child = new PortfolioNodeImpl(UniqueIdentifier.of("Child", "A"), "Name");
    root.addChildNode(child);
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name", root);
    assertSame(root, test.getNode(UniqueIdentifier.of("Root", "A")));
    assertSame(child, test.getNode(UniqueIdentifier.of("Child", "A")));
    assertEquals(null, test.getNode(UniqueIdentifier.of("NotFound", "A")));
  }

  @Test
  public void test_getPosition_Identifier() {
    PortfolioNodeImpl root = new PortfolioNodeImpl(UniqueIdentifier.of("Root", "A"), "Name");
    Position position = new PositionImpl(UniqueIdentifier.of("Child", "A"), BigDecimal.ZERO, Identifier.of("A", "B"));
    root.addPosition(position);
    PortfolioImpl test = new PortfolioImpl(id("Scheme", "Id"), "Name", root);
    assertSame(position, test.getPosition(UniqueIdentifier.of("Child", "A")));
    assertEquals(null, test.getPosition(UniqueIdentifier.of("NotFound", "A")));
  }

}
