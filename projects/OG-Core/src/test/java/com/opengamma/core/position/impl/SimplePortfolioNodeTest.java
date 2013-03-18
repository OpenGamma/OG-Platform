/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotSame;
import static org.testng.AssertJUnit.assertSame;

import java.math.BigDecimal;
import java.util.Arrays;

import org.testng.annotations.Test;

import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimplePortfolioNode}.
 */
@Test(groups = TestGroup.UNIT)
public class SimplePortfolioNodeTest {

  public void test_construction() {
    SimplePortfolioNode test = new SimplePortfolioNode();
    assertEquals(null, test.getUniqueId());
    assertEquals("", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[, 0 child-nodes, 0 positions]", test.toString());
  }

  //-------------------------------------------------------------------------
  public void test_construction_String() {
    SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of("A", "B"), "Name");
    assertEquals(UniqueId.of("A", "B"), test.getUniqueId());
    assertEquals("Name", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[A~B, 0 child-nodes, 0 positions]", test.toString());
  }

  public void test_construction_String_null() {
    SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of("A", "B"), null);
    assertEquals(UniqueId.of("A", "B"), test.getUniqueId());
    assertEquals("", test.getName());
    assertEquals(0, test.getChildNodes().size());
    assertEquals(0, test.getPositions().size());
    assertEquals(0, test.size());
    assertEquals("PortfolioNode[A~B, 0 child-nodes, 0 positions]", test.toString());
  }

  //-------------------------------------------------------------------------
  public void test_setUniqueId() {
    SimplePortfolio test = new SimplePortfolio(UniqueId.of("Scheme", "Id"), "Name");
    test.setUniqueId(UniqueId.of("Scheme2", "Id2"));
    assertEquals(UniqueId.of("Scheme2", "Id2"), test.getUniqueId());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setUniqueId_null() {
    SimplePortfolio test = new SimplePortfolio(UniqueId.of("Scheme", "Id"), "Name");
    test.setUniqueId(null);
  }

  //-------------------------------------------------------------------------
  public void test_setName() {
    SimplePortfolio test = new SimplePortfolio(UniqueId.of("Scheme", "Id"), "Name");
    test.setName("Name2");
    assertEquals("Name2", test.getName());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setName_null() {
    SimplePortfolio test = new SimplePortfolio(UniqueId.of("Scheme", "Id"), "Name");
    test.setName(null);
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions=UnsupportedOperationException.class)
  public void test_getChildNodes_immutable() {
    SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of ("A", "test"), "test");
    SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of ("A", "child"), "child");
    test.getChildNodes().add(child);
  }

  public void test_addChildNode() {
    SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of ("A", "test"), "test");
    SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of ("A", "child"), "child");
    child.setParentNodeId(test.getUniqueId());
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(child, test.getChildNodes().get(0));
    assertEquals(0, test.getPositions().size());
    assertEquals(1, test.size());
  }

  public void test_addChildNodes() {
    SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of ("A", "test"), "test");
    SimplePortfolioNode child0 = new SimplePortfolioNode(UniqueId.of ("A", "child0"), "child0");
    SimplePortfolioNode child1 = new SimplePortfolioNode(UniqueId.of ("A", "child1"), "child1");
    child0.setParentNodeId(test.getUniqueId());
    child1.setParentNodeId(test.getUniqueId());
    test.addChildNodes(Arrays.asList(child0, child1));
    assertEquals(2, test.getChildNodes().size());
    assertEquals(child0, test.getChildNodes().get(0));
    assertEquals(child1, test.getChildNodes().get(1));
    assertEquals(0, test.getPositions().size());
    assertEquals(2, test.size());
  }

  public void test_removeChildNode_match() {
    SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of ("A", "test"), "test");
    SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of ("A", "child"), "child");
    child.setParentNodeId(test.getUniqueId ());
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    test.removeChildNode(child);
    assertEquals(0, test.getChildNodes().size());
  }

  public void test_removeChildNode_noMatch() {
    SimplePortfolioNode test = new SimplePortfolioNode(UniqueId.of ("A", "test"), "test");
    SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of ("A", "child"), "child");
    SimplePortfolioNode removing = new SimplePortfolioNode(UniqueId.of ("A", "removing"), "removing");
    child.setParentNodeId (test.getUniqueId ());
    test.addChildNode(child);
    assertEquals(1, test.getChildNodes().size());
    test.removeChildNode(removing);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(child, test.getChildNodes().get(0));
  }

  //-------------------------------------------------------------------------
  @Test(expectedExceptions=UnsupportedOperationException.class)
  public void test_getPositions_immutable() {
    SimplePortfolioNode test = new SimplePortfolioNode();
    SimplePosition child = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.getPositions().add(child);
  }

  public void test_addPosition() {
    SimplePortfolioNode test = new SimplePortfolioNode();
    SimplePosition child = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    assertEquals(child, test.getPositions().get(0));
    assertEquals(0, test.getChildNodes().size());
    assertEquals(1, test.size());
  }

  public void test_addPositions() {
    SimplePortfolioNode test = new SimplePortfolioNode();
    SimplePosition child0 = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    SimplePosition child1 = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.addPositions(Arrays.asList(child0, child1));
    assertEquals(2, test.getPositions().size());
    assertEquals(child0, test.getPositions().get(0));
    assertEquals(child1, test.getPositions().get(1));
    assertEquals(0, test.getChildNodes().size());
    assertEquals(2, test.size());
  }

  public void test_removePosition_match() {
    SimplePortfolioNode test = new SimplePortfolioNode();
    SimplePosition child = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    test.removePosition(child);
    assertEquals(0, test.getPositions().size());
  }

  public void test_removePosition_noMatch() {
    SimplePortfolioNode test = new SimplePortfolioNode();
    SimplePosition child = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    SimplePosition removing = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "OTHER"));
    test.addPosition(child);
    assertEquals(1, test.getPositions().size());
    test.removePosition(removing);
    assertEquals(1, test.getPositions().size());
    assertEquals(child, test.getPositions().get(0));
  }

  //-------------------------------------------------------------------------
  public void test_size() {
    SimplePortfolioNode test = new SimplePortfolioNode();
    SimplePortfolioNode child1 = new SimplePortfolioNode();
    SimplePosition child2 = new SimplePosition(BigDecimal.ONE, ExternalId.of("K", "V"));
    test.addChildNode(child1);
    test.addPosition(child2);
    assertEquals(1, test.getChildNodes().size());
    assertEquals(1, test.getPositions().size());
    assertEquals(2, test.size());
  }

  //-------------------------------------------------------------------------
  public void test_getNode_UniqueId() {
    SimplePortfolioNode root = new SimplePortfolioNode(UniqueId.of("Root", "A"), "Name");
    SimplePortfolioNode child1 = new SimplePortfolioNode(UniqueId.of("Child", "A"), "Name");
    root.addChildNode(child1);
    SimplePortfolioNode child2 = new SimplePortfolioNode(UniqueId.of("Child", "B"), "Name");
    child2.setParentNodeId (root.getUniqueId ());
    root.addChildNode(child2);
    assertSame(root, root.getNode(UniqueId.of("Root", "A")));
    assertNotSame(child1, root.getNode(UniqueId.of("Child", "A")));
    // equal except for the parent link
    assertFalse(child1.equals (root.getNode(UniqueId.of("Child", "A"))));
    child1.setParentNodeId (root.getUniqueId ());
    assertEquals(child1, root.getNode(UniqueId.of("Child", "A")));
    assertSame(child2, root.getNode(UniqueId.of("Child", "B")));
    assertEquals(null, root.getNode(UniqueId.of("NotFound", "A")));
  }

  public void test_getPosition_UniqueId() {
    SimplePortfolioNode root = new SimplePortfolioNode(UniqueId.of("Root", "A"), "Name");
    SimplePortfolioNode child = new SimplePortfolioNode(UniqueId.of("Child", "A"), "Name");
    SimplePosition position1 = new SimplePosition(UniqueId.of("Child", "A"), BigDecimal.ZERO, ExternalId.of("A", "B"));
    SimplePosition position2 = new SimplePosition(UniqueId.of("Child", "B"), BigDecimal.ZERO, ExternalId.of("A", "B"));
    child.addPosition(position1);
    child.addPosition(position2);
    root.addChildNode(child);
    assertNotSame(position1, root.getPosition(UniqueId.of("Child", "A")));
    // equal except for the parent link
    assertEquals(position1, root.getPosition(UniqueId.of("Child", "A")));
    assertEquals(position2, root.getPosition(UniqueId.of("Child", "B")));
    assertEquals(null, root.getPosition(UniqueId.of("NotFound", "A")));
  }

}
