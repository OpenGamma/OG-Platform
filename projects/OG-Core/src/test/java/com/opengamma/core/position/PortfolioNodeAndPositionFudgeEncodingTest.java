/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;

import java.math.BigDecimal;
import java.util.List;

import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSupplier;
import com.opengamma.util.test.AbstractFudgeBuilderTestCase;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link PortfolioNodeFudgeBuilder} and {@link PositionFudgeBuilder}.
 */
@Test(groups = TestGroup.UNIT)
public class PortfolioNodeAndPositionFudgeEncodingTest extends AbstractFudgeBuilderTestCase {

  private static final Logger s_logger = LoggerFactory.getLogger(PortfolioNodeAndPositionFudgeEncodingTest.class);

  private UniqueIdSupplier _uniqueIdSupplier;

  @BeforeMethod
  public void init() {
    _uniqueIdSupplier = new UniqueIdSupplier("PortfolioNodeBuilderTest");
  }

  private UniqueId nextUniqueId() {
    return _uniqueIdSupplier.get();
  }

  private void linkNodes(final SimplePortfolioNode parent, final SimplePortfolioNode child) {
    child.setParentNodeId(parent.getUniqueId());
    parent.addChildNode(child);
  }

  private SimplePortfolioNode[] createPortfolioNodes() {
    final SimplePortfolioNode nodes[] = new SimplePortfolioNode[7];
    for (int i = 0; i < nodes.length; i++) {
      nodes[i] = new SimplePortfolioNode(nextUniqueId(), "node " + i);
    }
    linkNodes(nodes[0], nodes[1]);
    linkNodes(nodes[0], nodes[2]);
    linkNodes(nodes[1], nodes[3]);
    linkNodes(nodes[1], nodes[4]);
    linkNodes(nodes[2], nodes[5]);
    linkNodes(nodes[2], nodes[6]);
    return nodes;
  }

  private void addPositions(final SimplePortfolioNode node, final int num) {
    for (int i = 0; i < num; i++) {
      node.addPosition(new SimplePosition(nextUniqueId(), new BigDecimal(10), ExternalId.of("Security", "Foo")));
    }
  }

  private SimplePortfolioNode createPortfolioWithPositions() {
    final SimplePortfolioNode[] nodes = createPortfolioNodes();
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
    assertEquals(expected.getSecurityLink(), actual.getSecurityLink());
  }

  private FudgeMsg runPortfolioNodeTest(final PortfolioNode original) {
    final FudgeMsg message = getFudgeSerializer().objectToFudgeMsg(original);
    s_logger.debug("Message = {}", message);
    final PortfolioNode portfolio = getFudgeDeserializer().fudgeMsgToObject(PortfolioNode.class, message);
    assertPortfolioNodeEquals(original, portfolio);
    return message;
  }

  private int countParents(final FudgeMsg message) {
    int count = 0;
    for (FudgeField field : message) {
      if (PortfolioNodeFudgeBuilder.PARENT_FIELD_NAME.equals(field.getName())) {
        s_logger.debug("Found parent ref {}", field.getValue());
        count++;
      } else if (field.getValue() instanceof FudgeMsg) {
        count += countParents((FudgeMsg) field.getValue());
      }
    }
    return count;
  }

  public void testPortfolio() {
    final FudgeMsg message = runPortfolioNodeTest(createPortfolioNodes()[0]);
    assertEquals(0, countParents(message));
  }

  public void testPortfolioWithPositions() {
    final FudgeMsg message = runPortfolioNodeTest(createPortfolioWithPositions());
    assertEquals(0, countParents(message));
  }

  public void testPortfolioWithParent() {
    final SimplePortfolioNode root = createPortfolioNodes()[0];
    root.setParentNodeId(nextUniqueId());
    final FudgeMsg message = runPortfolioNodeTest(root);
    assertEquals(1, countParents(message));
  }

  private FudgeMsg runPositionTest(final Position original) {
    final FudgeMsg message = getFudgeSerializer().objectToFudgeMsg(original);
    s_logger.debug("Message = {}", message);
    final Position position = getFudgeDeserializer().fudgeMsgToObject(Position.class, message);
    assertPositionEquals(original, position);
    return message;
  }

  public void testPosition() {
    final FudgeMsg message = runPositionTest(new SimplePosition(nextUniqueId(), new BigDecimal(100), ExternalIdBundle.of(ExternalId.of("Scheme 1", "Id 1"), ExternalId
        .of("Scheme 2", "Id 2"))));
    assertEquals(0, countParents(message));
  }

}
