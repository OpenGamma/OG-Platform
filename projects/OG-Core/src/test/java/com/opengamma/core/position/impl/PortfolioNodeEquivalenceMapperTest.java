/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.Assert.assertEquals;

import java.math.BigDecimal;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link PortfolioNodeEquivalenceMapper} class.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioNodeEquivalenceMapperTest {

  private PortfolioNode createNodeA(final UniqueIdSupplier uidSupplier, final boolean swapBC, final boolean swapB, final int sizeB) {
    final SimplePortfolioNode node = new SimplePortfolioNode(uidSupplier.get(), "A");
    if (swapBC) {
      node.addChildNode(createNodeC(uidSupplier));
      node.addChildNode(createNodeB(uidSupplier, swapB, sizeB));
    } else {
      node.addChildNode(createNodeB(uidSupplier, swapB, sizeB));
      node.addChildNode(createNodeC(uidSupplier));
    }
    return node;
  }

  private PortfolioNode createNodeAUnbalanced(final UniqueIdSupplier uidSupplier, final boolean omitB, final boolean omitC) {
    final SimplePortfolioNode node = new SimplePortfolioNode(uidSupplier.get(), "A");
    if (!omitB) {
      node.addChildNode(createNodeB(uidSupplier, false, 2));
    }
    if (!omitC) {
      node.addChildNode(createNodeC(uidSupplier));
    }
    return node;
  }

  private PortfolioNode createNodeB(final UniqueIdSupplier uidSupplier, final boolean swap, final int size) {
    final SimplePortfolioNode node = new SimplePortfolioNode(uidSupplier.get(), "B");
    if (swap) {
      for (int i = size; i > 0; i--) {
        node.addPosition(createPosition(i));
      }
    } else {
      for (int i = 1; i <= size; i++) {
        node.addPosition(createPosition(i));
      }
    }
    return node;
  }

  private PortfolioNode createNodeC(final UniqueIdSupplier uidSupplier) {
    final SimplePortfolioNode node = new SimplePortfolioNode(uidSupplier.get(), "C");
    node.addChildNode(createNodeD(uidSupplier));
    node.addPosition(createPosition(3));
    return node;
  }

  private PortfolioNode createNodeD(final UniqueIdSupplier uidSupplier) {
    final SimplePortfolioNode node = new SimplePortfolioNode(uidSupplier.get(), "D");
    node.addPosition(createPosition(4));
    return node;
  }

  private Position createPosition(final int i) {
    return new SimplePosition(UniqueId.of("Test", Integer.toString(i)), BigDecimal.ONE, ExternalId.of("Test", Integer.toString(i)));
  }

  public void testIdentical() {
    final UniqueIdSupplier ids = new UniqueIdSupplier("Node");
    final PortfolioNodeEquivalenceMapper mapper = new PortfolioNodeEquivalenceMapper();
    final PortfolioNode a = createNodeA(ids, false, false, 2);
    final Map<UniqueId, UniqueId> map = mapper.getEquivalentNodes(a, a);
    assertEquals(map.size(), 4);
    assertEquals(map.get(UniqueId.of("Node", "1")), UniqueId.of("Node", "1"));
    assertEquals(map.get(UniqueId.of("Node", "2")), UniqueId.of("Node", "2"));
    assertEquals(map.get(UniqueId.of("Node", "3")), UniqueId.of("Node", "3"));
    assertEquals(map.get(UniqueId.of("Node", "4")), UniqueId.of("Node", "4"));
  }

  public void testEqualNodes() {
    final UniqueIdSupplier ids = new UniqueIdSupplier("Node");
    final PortfolioNodeEquivalenceMapper mapper = new PortfolioNodeEquivalenceMapper();
    final PortfolioNode a = createNodeA(ids, false, false, 2);
    final PortfolioNode b = createNodeA(ids, false, false, 2);
    final Map<UniqueId, UniqueId> map = mapper.getEquivalentNodes(a, b);
    assertEquals(map.size(), 4);
    assertEquals(map.get(UniqueId.of("Node", "1")), UniqueId.of("Node", "5"));
    assertEquals(map.get(UniqueId.of("Node", "2")), UniqueId.of("Node", "6"));
    assertEquals(map.get(UniqueId.of("Node", "3")), UniqueId.of("Node", "7"));
    assertEquals(map.get(UniqueId.of("Node", "4")), UniqueId.of("Node", "8"));
  }

  public void testEqualNodesSubset() {
    final UniqueIdSupplier ids = new UniqueIdSupplier("Node");
    final PortfolioNodeEquivalenceMapper mapper = new PortfolioNodeEquivalenceMapper();
    final PortfolioNode a = createNodeAUnbalanced(ids, false, false); // 1-4
    final PortfolioNode b = createNodeAUnbalanced(ids, false, true); // 5-6
    final PortfolioNode c = createNodeAUnbalanced(ids, true, false); // 7-9
    Map<UniqueId, UniqueId> map = mapper.getEquivalentNodes(a, b);
    assertEquals(map.size(), 1);
    assertEquals(map.get(UniqueId.of("Node", "2")), UniqueId.of("Node", "6"));
    map = mapper.getEquivalentNodes(b, a);
    assertEquals(map.size(), 1);
    assertEquals(map.get(UniqueId.of("Node", "6")), UniqueId.of("Node", "2"));
    map = mapper.getEquivalentNodes(a, c);
    assertEquals(map.size(), 2);
    assertEquals(map.get(UniqueId.of("Node", "3")), UniqueId.of("Node", "8"));
    assertEquals(map.get(UniqueId.of("Node", "4")), UniqueId.of("Node", "9"));
    map = mapper.getEquivalentNodes(c, a);
    assertEquals(map.size(), 2);
    assertEquals(map.get(UniqueId.of("Node", "8")), UniqueId.of("Node", "3"));
    assertEquals(map.get(UniqueId.of("Node", "9")), UniqueId.of("Node", "4"));
  }

  public void testEqualNodesReordered() {
    final UniqueIdSupplier ids = new UniqueIdSupplier("Node");
    final PortfolioNodeEquivalenceMapper mapper = new PortfolioNodeEquivalenceMapper();
    final PortfolioNode a = createNodeA(ids, false, false, 2);
    final PortfolioNode b = createNodeA(ids, true, false, 2);
    final Map<UniqueId, UniqueId> map = mapper.getEquivalentNodes(a, b);
    assertEquals(map.size(), 4);
    assertEquals(map.get(UniqueId.of("Node", "1")), UniqueId.of("Node", "5"));
    assertEquals(map.get(UniqueId.of("Node", "2")), UniqueId.of("Node", "8"));
    assertEquals(map.get(UniqueId.of("Node", "3")), UniqueId.of("Node", "6"));
    assertEquals(map.get(UniqueId.of("Node", "4")), UniqueId.of("Node", "7"));
  }

  public void testPositionsReordered() {
    final UniqueIdSupplier ids = new UniqueIdSupplier("Node");
    final PortfolioNodeEquivalenceMapper mapper = new PortfolioNodeEquivalenceMapper();
    final PortfolioNode a = createNodeA(ids, false, false, 2);
    final PortfolioNode b = createNodeA(ids, false, true, 2);
    final Map<UniqueId, UniqueId> map = mapper.getEquivalentNodes(a, b);
    assertEquals(map.size(), 2);
    assertEquals(map.get(UniqueId.of("Node", "3")), UniqueId.of("Node", "7"));
    assertEquals(map.get(UniqueId.of("Node", "4")), UniqueId.of("Node", "8"));
  }

  public void testPositionsChanged() {
    final UniqueIdSupplier ids = new UniqueIdSupplier("Node");
    final PortfolioNodeEquivalenceMapper mapper = new PortfolioNodeEquivalenceMapper();
    final SimplePortfolioNode x = new SimplePortfolioNode(UniqueId.of("Test", "X"), "X");
    x.addChildNode(createNodeA(ids, false, false, 2));
    final SimplePortfolioNode y = new SimplePortfolioNode(UniqueId.of("Test", "Y"), "Y");
    y.addChildNode(createNodeA(ids, false, false, 3));
    final Map<UniqueId, UniqueId> map = mapper.getEquivalentNodes(x, y);
    assertEquals(map.size(), 2);
    assertEquals(map.get(UniqueId.of("Node", "3")), UniqueId.of("Node", "7"));
    assertEquals(map.get(UniqueId.of("Node", "4")), UniqueId.of("Node", "8"));
  }

  public void testNoMatch() {
    final UniqueIdSupplier ids = new UniqueIdSupplier("Node");
    final PortfolioNodeEquivalenceMapper mapper = new PortfolioNodeEquivalenceMapper();
    PortfolioNode a = createNodeB(ids, false, 2);
    PortfolioNode b = createNodeC(ids);
    assertEquals(mapper.getEquivalentNodes(a, b).size(), 0);
    a = createNodeB(ids, false, 2);
    b = createNodeB(ids, false, 3);
    assertEquals(mapper.getEquivalentNodes(a, b).size(), 0);
  }

}
