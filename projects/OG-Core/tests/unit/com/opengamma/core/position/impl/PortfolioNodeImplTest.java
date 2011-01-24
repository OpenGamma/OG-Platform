/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.math.BigDecimal;
import java.util.Arrays;

import org.junit.Test;

import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test PortfolioNodeImpl.
 */
public class PortfolioNodeImplTest {

  @Test
  public void test_construction() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    assertEquals(null, test.getUniqueId());
    assertEquals("", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[, 0 child-nodes, 0 positions]", test.toString());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_String() {
    PortfolioNodeImpl test = new PortfolioNodeImpl(UniqueIdentifier.of("A", "B"), "Name");
    assertEquals(UniqueIdentifier.of("A", "B"), test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[A::B, 0 child-nodes, 0 positions]", test.toString());
  }

  @Test
  public void test_construction_String_null() {
    PortfolioNodeImpl test = new PortfolioNodeImpl(UniqueIdentifier.of("A", "B"), null);
    assertEquals(UniqueIdentifier.of("A", "B"), test.getUniqueId());
    assertEquals("", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[A::B, 0 child-nodes, 0 positions]", test.toString());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setUniqueId() {
    PortfolioImpl test = new PortfolioImpl(UniqueIdentifier.of("Scheme", "Id"), "Name");
    test.setUniqueId(UniqueIdentifier.of("Scheme2", "Id2"));
    assertEquals(UniqueIdentifier.of("Scheme2", "Id2"), test.getUniqueId());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_setUniqueId_null() {
    PortfolioImpl test = new PortfolioImpl(UniqueIdentifier.of("Scheme", "Id"), "Name");
    test.setUniqueId(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setName() {
    PortfolioImpl test = new PortfolioImpl(UniqueIdentifier.of("Scheme", "Id"), "Name");
    test.setName("Name2");
    assertEquals("Name2", test.getName());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_setName_null() {
    PortfolioImpl test = new PortfolioImpl(UniqueIdentifier.of("Scheme", "Id"), "Name");
    test.setName(null);
  }

  //-------------------------------------------------------------------------
  @Test(expected=UnsupportedOperationException.class)
  public void test_getChildNodes_immutable() {
    PortfolioNodeImpl test = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "test"), "test");
    PortfolioNodeImpl child = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "child"), "child");
    test.getChildNodes().add(child);
  }

  @Test
  public void test_addChildNode() {
    PortfolioNodeImpl test = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "test"), "test");
    PortfolioNodeImpl child = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "child"), "child");
    child.setParentNodeId(test.getUniqueId());
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(child, test.getChildNodes().get(0));
    assertEquals(0, test.getPositions().size());
    assertEquals(1, test.size());
  }

  @Test
  public void test_addChildNodes() {
    PortfolioNodeImpl test = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "test"), "test");
    PortfolioNodeImpl child0 = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "child0"), "child0");
    PortfolioNodeImpl child1 = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "child1"), "child1");
    child0.setParentNodeId(test.getUniqueId());
    child1.setParentNodeId(test.getUniqueId());
    test.addChildNodes(Arrays.asList(child0, child1));
    assertEquals(2, test.getChildNodes().size());
    assertEquals(child0, test.getChildNodes().get(0));
    assertEquals(child1, test.getChildNodes().get(1));
    assertEquals(0, test.getPositions().size());
    assertEquals(2, test.size());
  }

  @Test
  public void test_removeChildNode_match() {
    PortfolioNodeImpl test = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "test"), "test");
    PortfolioNodeImpl child = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "child"), "child");
    child.setParentNodeId(test.getUniqueId ());
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    test.removeChildNode(child);
    assertEquals(0, test.getChildNodes().size());
  }

  @Test
  public void test_removeChildNode_noMatch() {
    PortfolioNodeImpl test = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "test"), "test");
    PortfolioNodeImpl child = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "child"), "child");
    PortfolioNodeImpl removing = new PortfolioNodeImpl(UniqueIdentifier.of ("A", "removing"), "removing");
    child.setParentNodeId (test.getUniqueId ());
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    test.removeChildNode(removing);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(child, test.getChildNodes().get(0));
  }

  //-------------------------------------------------------------------------
  @Test(expected=UnsupportedOperationException.class)
  public void test_getPositions_immutable() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionImpl child = new PositionImpl(BigDecimal.ONE, Identifier.of("K", "V"));
    test.getPositions().add(child);
  }

  @Test
  public void test_addPosition() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionImpl child = new PositionImpl(BigDecimal.ONE, Identifier.of("K", "V"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    assertEquals(child, test.getPositions().get(0));
    assertEquals(0, test.getChildNodes().size());
    assertEquals(1, test.size());
  }

  @Test
  public void test_addPositions() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionImpl child0 = new PositionImpl(BigDecimal.ONE, Identifier.of("K", "V"));
    PositionImpl child1 = new PositionImpl(BigDecimal.ONE, Identifier.of("K", "V"));
    test.addPositions(Arrays.asList(child0, child1));
    assertEquals(2, test.getPositions().size());
    assertEquals(child0, test.getPositions().get(0));
    assertEquals(child1, test.getPositions().get(1));
    assertEquals(0, test.getChildNodes().size());
    assertEquals(2, test.size());
  }

  @Test
  public void test_removePosition_match() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionImpl child = new PositionImpl(BigDecimal.ONE, Identifier.of("K", "V"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    test.removePosition(child);
    assertEquals(0, test.getPositions().size());
  }

  @Test
  public void test_removePosition_noMatch() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PositionImpl child = new PositionImpl(BigDecimal.ONE, Identifier.of("K", "V"));
    PositionImpl removing = new PositionImpl(BigDecimal.ONE, Identifier.of("K", "OTHER"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    test.removePosition(removing);
    assertEquals(1, test.getPositions().size());
    assertEquals(child, test.getPositions().get(0));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_size() {
    PortfolioNodeImpl test = new PortfolioNodeImpl();
    PortfolioNodeImpl child1 = new PortfolioNodeImpl();
    PositionImpl child2 = new PositionImpl(BigDecimal.ONE, Identifier.of("K", "V"));
    test.addChildNode(child1);
    test.addPosition(child2);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(1, test.getPositions().size());
    assertEquals(2, test.size());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getNode_Identifier() {
    PortfolioNodeImpl root = new PortfolioNodeImpl(UniqueIdentifier.of("Root", "A"), "Name");
    PortfolioNodeImpl child1 = new PortfolioNodeImpl(UniqueIdentifier.of("Child", "A"), "Name");
    root.addChildNode(child1);
    PortfolioNodeImpl child2 = new PortfolioNodeImpl(UniqueIdentifier.of("Child", "B"), "Name");
    child2.setParentNodeId (root.getUniqueId ());
    root.addChildNode(child2);
    assertSame(root, root.getNode(UniqueIdentifier.of("Root", "A")));
    assertNotSame(child1, root.getNode(UniqueIdentifier.of("Child", "A")));
    // equal except for the parent link
    assertFalse(child1.equals (root.getNode(UniqueIdentifier.of("Child", "A"))));
    child1.setParentNodeId (root.getUniqueId ());
    assertEquals(child1, root.getNode(UniqueIdentifier.of("Child", "A")));
    assertSame(child2, root.getNode(UniqueIdentifier.of("Child", "B")));
    assertEquals(null, root.getNode(UniqueIdentifier.of("NotFound", "A")));
  }

  @Test
  public void test_getPosition_Identifier() {
    PortfolioNodeImpl root = new PortfolioNodeImpl(UniqueIdentifier.of("Root", "A"), "Name");
    PortfolioNodeImpl child = new PortfolioNodeImpl(UniqueIdentifier.of("Child", "A"), "Name");
    PositionImpl position1 = new PositionImpl(UniqueIdentifier.of("Child", "A"), BigDecimal.ZERO, Identifier.of("A", "B"));
    PositionImpl position2 = new PositionImpl(UniqueIdentifier.of("Child", "B"), BigDecimal.ZERO, Identifier.of("A", "B"));
    child.addPosition(position1);
    position2.setParentNodeId(child.getUniqueId ());
    child.addPosition(position2);
    root.addChildNode(child);
    assertNotSame(position1, root.getPosition(UniqueIdentifier.of("Child", "A")));
    // equal except for the parent link
    assertFalse(position1.equals (root.getPosition(UniqueIdentifier.of("Child", "A"))));
    position1.setParentNodeId(child.getUniqueId ());
    assertEquals(position1, root.getPosition(UniqueIdentifier.of("Child", "A")));
    assertSame(position2, root.getPosition(UniqueIdentifier.of("Child", "B")));
    assertEquals(null, root.getPosition(UniqueIdentifier.of("NotFound", "A")));
  }

}
