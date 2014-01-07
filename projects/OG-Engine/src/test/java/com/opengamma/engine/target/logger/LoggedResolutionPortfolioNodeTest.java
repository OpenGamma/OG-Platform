/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.logger;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.mockito.Mockito;
import org.testng.annotations.Test;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LoggedResolutionPortfolioNode} class.
 */
@Test(groups = TestGroup.UNIT)
public class LoggedResolutionPortfolioNodeTest {

  public void getParentNodeId() {
    final PortfolioNode node = Mockito.mock(PortfolioNode.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final PortfolioNode logged = new LoggedResolutionPortfolioNode(node, logger);
    Mockito.when(node.getParentNodeId()).thenReturn(UniqueId.of("Foo", "Bar"));
    assertEquals(logged.getParentNodeId(), UniqueId.of("Foo", "Bar"));
    Mockito.verifyZeroInteractions(logger);
  }

  public void size() {
    final PortfolioNode node = Mockito.mock(PortfolioNode.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final PortfolioNode logged = new LoggedResolutionPortfolioNode(node, logger);
    Mockito.when(node.size()).thenReturn(42);
    assertEquals(logged.size(), 42);
    Mockito.verifyZeroInteractions(logger);
  }

  public void getChildNodes() {
    final PortfolioNode node = Mockito.mock(PortfolioNode.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final PortfolioNode logged = new LoggedResolutionPortfolioNode(node, logger);
    final List<PortfolioNode> childNodes = new ArrayList<PortfolioNode>();
    for (int i = 0; i < 3; i++) {
      final PortfolioNode childNode = Mockito.mock(PortfolioNode.class);
      Mockito.when(childNode.getUniqueId()).thenReturn(UniqueId.of("Node", Integer.toString(i), "0"));
      childNodes.add(childNode);
    }
    Mockito.when(node.getChildNodes()).thenReturn(childNodes);
    final Collection<PortfolioNode> loggedChildNodes = logged.getChildNodes();
    assertEquals(loggedChildNodes.size(), 3);
    int i = 0;
    for (PortfolioNode childNode : loggedChildNodes) {
      assertTrue(childNode instanceof LoggedResolutionPortfolioNode);
      //Mockito.verify(logger).log(new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UniqueId.of("Node", Integer.toString(i))), UniqueId.of("Node", Integer.toString(i), "0"));
      i++;
    }
    Mockito.verifyNoMoreInteractions(logger);
  }

  public void getPositions() {
    final PortfolioNode node = Mockito.mock(PortfolioNode.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final PortfolioNode logged = new LoggedResolutionPortfolioNode(node, logger);
    final List<Position> positions = new ArrayList<Position>();
    for (int i = 0; i < 3; i++) {
      final Position position = Mockito.mock(Position.class);
      Mockito.when(position.getUniqueId()).thenReturn(UniqueId.of("Position", Integer.toString(i), "0"));
      positions.add(position);
    }
    Mockito.when(node.getPositions()).thenReturn(positions);
    final Collection<Position> loggedPositions = logged.getPositions();
    assertEquals(loggedPositions.size(), 3);
    int i = 0;
    for (Position position : loggedPositions) {
      assertTrue(position instanceof LoggedResolutionPosition);
      Mockito.verify(logger).log(new ComputationTargetSpecification(ComputationTargetType.POSITION, UniqueId.of("Position", Integer.toString(i))), UniqueId.of("Position", Integer.toString(i), "0"));
      i++;
    }
    Mockito.verifyNoMoreInteractions(logger);
  }

  public void getName() {
    final PortfolioNode node = Mockito.mock(PortfolioNode.class);
    final ResolutionLogger logger = Mockito.mock(ResolutionLogger.class);
    final PortfolioNode logged = new LoggedResolutionPortfolioNode(node, logger);
    Mockito.when(node.getName()).thenReturn("Foo");
    assertEquals(logged.getName(), "Foo");
    Mockito.verifyZeroInteractions(logger);
  }

}
