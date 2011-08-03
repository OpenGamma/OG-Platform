/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertTrue;
import org.testng.annotations.Test;
import org.testng.annotations.BeforeMethod;
import java.math.BigDecimal;
import java.util.List;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdSupplier;

/**
 * Tests for the {@link PortfolioStructure} class. 
 */
@Test
public class PortfolioStructureTest {

  private FunctionCompilationContext _context;
  private PortfolioNodeImpl _root;
  private PortfolioNodeImpl _child1;
  private PortfolioNodeImpl _child2;
  private PortfolioNodeImpl _badChild;
  private PositionImpl _position1;
  private PositionImpl _position2;
  private PositionImpl _badPosition;
  
  @BeforeMethod
  public void createPortfolio() {
    final UniqueIdSupplier uid = new UniqueIdSupplier("Test");
    final MockPositionSource positionSource = new MockPositionSource();
    final PortfolioStructure resolver = new PortfolioStructure(positionSource);
    final PortfolioImpl portfolio = new PortfolioImpl(uid.get(), "Test");
    _root = new PortfolioNodeImpl(uid.get(), "root");
    _child1 = new PortfolioNodeImpl(uid.get(), "child 1");
    _child2 = new PortfolioNodeImpl(uid.get(), "child 2");
    _position1 = new PositionImpl(uid.get(), new BigDecimal(10), Identifier.of("Security", "Foo"));
    _position1.setParentNodeId(_child2.getUniqueId());
    _child2.addPosition(_position1);
    _position2 = new PositionImpl(uid.get(), new BigDecimal(20), Identifier.of("Security", "Bar"));
    _position2.setParentNodeId(_child2.getUniqueId());
    _child2.addPosition(_position2);
    _child2.setParentNodeId(_child1.getUniqueId());
    _child1.addChildNode(_child2);
    _child1.setParentNodeId(_root.getUniqueId());
    _root.addChildNode(_child1);
    portfolio.setRootNode(_root);
    positionSource.addPortfolio(portfolio);
    _badChild = new PortfolioNodeImpl(uid.get(), "child 3");
    _badChild.setParentNodeId(uid.get());
    _badPosition = new PositionImpl(uid.get(), new BigDecimal(10), Identifier.of("Security", "Cow"));
    _badPosition.setParentNodeId(uid.get());
    _context = new FunctionCompilationContext();
    _context.setPortfolioStructure(resolver);
  }

  private PortfolioStructure getPortfolioStructure() {
    return _context.getPortfolioStructure();
  }

  public void testGetParentNode_portfolioNode() {
    final PortfolioStructure resolver = getPortfolioStructure();
    assertNotNull(resolver);
    assertEquals(_child1, resolver.getParentNode(_child2));
    assertEquals(_root, resolver.getParentNode(_child1));
    assertEquals(null, resolver.getParentNode(_root));
    assertNull(resolver.getParentNode(_badChild));
  }

  public void testGetParentNode_position() {
    final PortfolioStructure resolver = getPortfolioStructure();
    assertNotNull(resolver);
    assertEquals(_child2, resolver.getParentNode(_position1));
    assertEquals(_child2, resolver.getParentNode(_position2));
    assertNull(resolver.getParentNode(_badPosition));
  }

  public void testGetRootPortfolioNode_portfolioNode() {
    final PortfolioStructure resolver = getPortfolioStructure();
    assertNotNull(resolver);
    assertEquals(_root, resolver.getRootPortfolioNode(_child1));
    assertEquals(_root, resolver.getRootPortfolioNode(_child2));
    assertEquals(_root, resolver.getRootPortfolioNode(_root));
    assertNull(resolver.getRootPortfolioNode(_badChild));
  }

  public void testGetRootPortfolioNode_position() {
    final PortfolioStructure resolver = getPortfolioStructure();
    assertNotNull(resolver);
    assertEquals(_root, resolver.getRootPortfolioNode(_position1));
    assertEquals(_root, resolver.getRootPortfolioNode(_position2));
    assertNull(resolver.getRootPortfolioNode(_badPosition));
  }
  
  public void testGetAllPositions () {
    final PortfolioStructure resolver = getPortfolioStructure ();
    assertNotNull(resolver);
    List<Position> positions = resolver.getAllPositions(_child1);
    assertNotNull (positions);
    assertEquals (2, positions.size ());
    assertTrue (positions.contains (_position1));
    assertTrue (positions.contains (_position2));
    positions = resolver.getAllPositions (_root);
    assertNotNull (positions);
    assertEquals (2, positions.size ());
    assertTrue (positions.contains (_position1));
    assertTrue (positions.contains (_position2));
    positions = resolver.getAllPositions (_child2);
    assertNotNull (positions);
    assertEquals (2, positions.size ());
    assertTrue (positions.contains (_position1));
    assertTrue (positions.contains (_position2));
    positions = resolver.getAllPositions(_badChild);
    assertNotNull (positions);
    assertTrue (positions.isEmpty ());
  }

}
