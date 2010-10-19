/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.function;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;

import java.math.BigDecimal;

import org.junit.Before;
import org.junit.Test;

import com.opengamma.engine.position.MockPositionSource;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Tests for the {@link ParentNodeResolver} class. 
 */
public class ParentNodeResolverTest {

  private FunctionCompilationContext _context;
  private PortfolioNodeImpl _root;
  private PortfolioNodeImpl _child1;
  private PortfolioNodeImpl _child2;
  private PortfolioNodeImpl _badChild;
  private PositionImpl _position;
  private PositionImpl _badPosition;

  @Before
  public void createPortfolio() {
    final MockPositionSource positionSource = new MockPositionSource();
    final ParentNodeResolver resolver = new ParentNodeResolver (positionSource);
    final PortfolioImpl portfolio = new PortfolioImpl(UniqueIdentifier.of ("Test", "-1"), "Test");
    _root = new PortfolioNodeImpl(UniqueIdentifier.of("Test", "0"), "root");
    _child1 = new PortfolioNodeImpl(UniqueIdentifier.of("Test", "1"), "child 1");
    _child1.setParentNode(_root.getUniqueIdentifier());
    _root.addChildNode(_child1);
    _child2 = new PortfolioNodeImpl(UniqueIdentifier.of("Test", "2"), "child 2");
    _child2.setParentNode(_child1.getUniqueIdentifier());
    _child1.addChildNode(_child2);
    _position = new PositionImpl(UniqueIdentifier.of("Test", "3"), new BigDecimal(10), Identifier.of("Test", "4"));
    _position.setPortfolioNode(_child2.getUniqueIdentifier());
    _child2.addPosition(_position);
    portfolio.setRootNode(_root);
    positionSource.addPortfolio(portfolio);
    _badChild = new PortfolioNodeImpl(UniqueIdentifier.of("Test", "4"), "child 3");
    _badChild.setParentNode(UniqueIdentifier.of("Fail", "0"));
    _badPosition = new PositionImpl(UniqueIdentifier.of("Test", "5"), new BigDecimal(10), Identifier.of("Test", "4"));
    _badPosition.setPortfolioNode(UniqueIdentifier.of("Fail", "1"));
    _context = new FunctionCompilationContext ();
    _context.setParentNodeResolver(resolver);
  }
  
  private ParentNodeResolver getParentNodeResolver () {
    return _context.getParentNodeResolver ();
  }

  @Test
  public void testGetParentNode_portfolioNode() {
    final ParentNodeResolver resolver = getParentNodeResolver ();
    assertNotNull(resolver);
    assertEquals(_child1, resolver.getParentNode(_child2));
    assertEquals(_root, resolver.getParentNode(_child1));
    assertEquals(null, resolver.getParentNode(_root));
    assertNull(resolver.getParentNode(_badChild));
  }

  @Test
  public void testGetParentNode_position() {
    final ParentNodeResolver resolver = getParentNodeResolver ();
    assertNotNull(resolver);
    assertEquals(_child2, resolver.getParentNode(_position));
    assertNull(resolver.getParentNode(_badPosition));
  }

  @Test
  public void testGetRootPortfolioNode_portfolioNode() {
    final ParentNodeResolver resolver = getParentNodeResolver ();
    assertNotNull(resolver);
    assertEquals(_root, resolver.getRootPortfolioNode(_child1));
    assertEquals(_root, resolver.getRootPortfolioNode(_child2));
    assertEquals(_root, resolver.getRootPortfolioNode(_root));
    assertNull(resolver.getRootPortfolioNode(_badChild));
  }

  @Test
  public void testGetRootPortfolioNode_position() {
    final ParentNodeResolver resolver = getParentNodeResolver ();
    assertNotNull(resolver);
    assertEquals(_root, resolver.getRootPortfolioNode(_position));
    assertNull(resolver.getRootPortfolioNode(_badPosition));
  }

}
