/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.math.BigDecimal;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.id.UniqueIdentifierSupplier;

/**
 * Tests the PortfolioNode and Position object builders.
 */
@Test
public class PortfolioNodeAndPositionBuilderTest extends AbstractBuilderTestCase {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioNodeAndPositionBuilderTest.class);

  private UniqueIdentifierSupplier _uidSupplier;

  @BeforeMethod
  public void init() {
    _uidSupplier = new UniqueIdentifierSupplier("PortfolioNodeBuilderTest");
  }

  private UniqueIdentifier nextIdentifier() {
    return _uidSupplier.get();
  }

  private void linkNodes(final PortfolioNodeImpl parent, final PortfolioNodeImpl child) {
    child.setParentNodeId(parent.getUniqueId());
    parent.addChildNode(child);
  }

  private PortfolioNodeImpl[] createPortfolioNodes() {
    final PortfolioNodeImpl nodes[] = new PortfolioNodeImpl[7];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = new PortfolioNodeImpl(nextIdentifier(), "node " + i);
    }
    linkNodes(nodes[0], nodes[1]);
    linkNodes(nodes[0], nodes[2]);
    linkNodes(nodes[1], nodes[3]);
    linkNodes(nodes[1], nodes[4]);
    linkNodes(nodes[2], nodes[5]);
    linkNodes(nodes[2], nodes[6]);
    return nodes;
  }

  private void addPositions(final PortfolioNodeImpl node, final int num) {
    for (int i = 0; i < num; i++) {
      node.addPosition(new PositionImpl(nextIdentifier(), new BigDecimal(10), Identifier.of("Security", "Foo")));
    }
  }

  private PortfolioNodeImpl createPortfolioWithPositions() {
    final PortfolioNodeImpl[] nodes = createPortfolioNodes();
    addPositions(nodes[1], 1);
    addPositions(nodes[3], 2);
    addPositions(nodes[5], 1);
    addPositions(nodes[6], 2);
    return nodes[0];
  }

  private void assertPortfolioNodeEquals(final PortfolioNode expected, final PortfolioNode actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    s_logger.debug("testing portfolio node {}", expected.getUniqueId());
    assertEquals(expected.getUniqueId(), actual.getUniqueId());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.size(), actual.size());
    assertEquals(expected.getParentNodeId(), actual.getParentNodeId());
    final List<PortfolioNode> expectedChildren = expected.getChildNodes();
    final List<PortfolioNode> actualChildren = actual.getChildNodes();
    assertNotNull(expectedChildren);
    assertNotNull(actualChildren);
    assertEquals(expectedChildren.size(), actualChildren.size());
    for (int i = 0; i < expectedChildren.size(); i++) {
      s_logger.debug("testing child {} of {}", i, actual.getUniqueId());
      assertPortfolioNodeEquals(expectedChildren.get(i), actualChildren.get(i));
    }
    final List<Position> expectedPositions = expected.getPositions();
    final List<Position> actualPositions = actual.getPositions();
    assertNotNull(expectedPositions);
    assertNotNull(actualPositions);
    assertEquals(expectedPositions.size(), actualPositions.size());
    for (int i = 0; i < expectedPositions.size(); i++) {
      s_logger.debug("testing position {} of {}", i, actual.getUniqueId());
      assertPositionEquals(expectedPositions.get(i), actualPositions.get(i));
    }
  }

  private void assertPositionEquals(final Position expected, final Position actual) {
    assertNotNull(expected);
    assertNotNull(actual);
    s_logger.debug("testing position {}", expected.getUniqueId());
    assertEquals(expected.getUniqueId(), actual.getUniqueId());
    assertEquals(expected.getQuantity(), actual.getQuantity());
    assertEquals(expected.getSecurityKey(), actual.getSecurityKey());
    assertEquals(expected.getParentNodeId(), actual.getParentNodeId());
  }

  private FudgeFieldContainer runPortfolioNodeTest(final PortfolioNode original) {
    final FudgeFieldContainer message = getFudgeSerializationContext().objectToFudgeMsg(original);
    s_logger.debug("Message = {}", message);
    final PortfolioNode portfolio = getFudgeDeserializationContext().fudgeMsgToObject(PortfolioNode.class, message);
    assertPortfolioNodeEquals(original, portfolio);
    return message;
  }

  private int countParentIdentifiers(final FudgeFieldContainer message) {
    int count = 0;
    for (FudgeField field : message) {
      if (PortfolioNodeBuilder.FIELD_PARENT.equals(field.getName()) || PositionBuilder.FIELD_PARENT.equals(field.getName())) {
        s_logger.debug("Found parent ref {}", field.getValue());
        count++;
      } else if (field.getValue() instanceof FudgeFieldContainer) {
        count += countParentIdentifiers((FudgeFieldContainer) field.getValue());
      }
    }
    return count;
  }

  public void testPortfolio() {
    final FudgeFieldContainer message = runPortfolioNodeTest(createPortfolioNodes()[0]);
    assertEquals(0, countParentIdentifiers(message));
  }

  public void testPortfolioWithPositions() {
    final FudgeFieldContainer message = runPortfolioNodeTest(createPortfolioWithPositions());
    assertEquals(0, countParentIdentifiers(message));
  }

  public void testPortfolioWithParent() {
    final PortfolioNodeImpl root = createPortfolioNodes()[0];
    root.setParentNodeId(nextIdentifier());
    final FudgeFieldContainer message = runPortfolioNodeTest(root);
    assertEquals(1, countParentIdentifiers(message));
  }

  private FudgeFieldContainer runPositionTest(final Position original) {
    final FudgeFieldContainer message = getFudgeSerializationContext().objectToFudgeMsg(original);
    s_logger.debug("Message = {}", message);
    final Position position = getFudgeDeserializationContext().fudgeMsgToObject(Position.class, message);
    assertPositionEquals(original, position);
    return message;
  }

  public void testPosition() {
    final FudgeFieldContainer message = runPositionTest(new PositionImpl(nextIdentifier(), new BigDecimal(100), IdentifierBundle.of(Identifier.of("Scheme 1", "Id 1"), Identifier
        .of("Scheme 2", "Id 2"))));
    assertEquals(0, countParentIdentifiers(message));
  }

  public void testPositionWithPortfolioNode() {
    final PositionImpl position = new PositionImpl(nextIdentifier(), new BigDecimal(100), Identifier.of("Security", "Bar"));
    position.setParentNodeId(nextIdentifier());
    final FudgeFieldContainer message = runPositionTest(position);
    assertEquals(1, countParentIdentifiers(message));
  }

}
