/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * 
 */
public class PositionMasterTestUtil {

  public static Portfolio constructTestPortfolio(UniqueIdentifier portfolioUid, String name) {
    PortfolioImpl portfolio = new PortfolioImpl(name);
    if (portfolioUid != null) {
      portfolio.setUniqueIdentifier(portfolioUid);
    }
    PortfolioNodeImpl rootNode = new PortfolioNodeImpl();
    
    portfolio.setRootNode(rootNode);
    populateNode(rootNode, "0", 3);
    
    return portfolio;
  }

  private static void populateNode(PortfolioNodeImpl node, String discriminator, int depth) {   
    for (int i=0; i<10; i++) {
      PositionImpl pos = new PositionImpl(new BigDecimal(i * 10), Identifier.of("S", discriminator + "-" + i));
      node.addPosition(pos);
    }
    
    if (depth == 0) {
      return;
    }
    
    // Add some child nodes
    for (int i=0; i<2; i++) {
      PortfolioNodeImpl childNode = new PortfolioNodeImpl();
      node.addChildNode(childNode);
      populateNode(node, discriminator + "-" + i, depth - 1);
    }
  }
  
  public static void testNodeLookup(PositionMaster pm, Portfolio portfolio, boolean isPresent) {
    Portfolio expectedPortfolio = isPresent ? portfolio : null;
    assertEquals(expectedPortfolio, pm.getPortfolio(portfolio.getUniqueIdentifier()));
    
    testNodeLookup(pm, portfolio.getRootNode(), isPresent);
  }
    
  private static void testNodeLookup(PositionMaster pm, PortfolioNode node, boolean isPresent) {
    PortfolioNode expectedNode = isPresent ? node : null;
    assertEquals(expectedNode, pm.getPortfolioNode(node.getUniqueIdentifier()));
    
    for (Position position : node.getPositions()) {
      Position expectedPosition = isPresent ? position : null;
      assertEquals(expectedPosition, pm.getPosition(position.getUniqueIdentifier()));
    }
    for (PortfolioNode childNode : node.getChildNodes()) {
      testNodeLookup(pm, childNode, isPresent);
    }
  }
  
  /**
   * Tests whether two node structures are equivalent, ignoring IDs. Node at each level must have unique names, and 
   * positions at each level must have unique security keys.
   * 
   * @param expected  the expected root portfolio node
   * @param actual  the actual root portfolio node
   */
  public static void equivalentNodeStructures(PortfolioNode expected, PortfolioNode actual) {
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getChildNodes().size(), actual.getChildNodes().size());
    assertEquals(expected.getPositions().size(), actual.getPositions().size());
    
    Map<String, PortfolioNode> expectedChildNodeMap = new HashMap<String, PortfolioNode>();
    for (PortfolioNode expectedChildNode : expected.getChildNodes()) {
      expectedChildNodeMap.put(expectedChildNode.getName(), expectedChildNode);
    }
    for (PortfolioNode actualChildNode : actual.getChildNodes()) {
      PortfolioNode expectedChildNode = expectedChildNodeMap.get(actualChildNode.getName());
      assertNotNull(expectedChildNode);
      equivalentNodeStructures(expectedChildNode, actualChildNode);
    }
    
    Map<IdentifierBundle, Position> expectedPositionMap = new HashMap<IdentifierBundle, Position>();
    for (Position expectedPosition : expected.getPositions()) {
      expectedPositionMap.put(expectedPosition.getSecurityKey(), expectedPosition);
    }
    for (Position actualPosition : actual.getPositions()) {
      Position expectedPosition = expectedPositionMap.get(actualPosition.getSecurityKey());
      assertNotNull(expectedPosition);
      assertEquals(expectedPosition.getQuantity(), actualPosition.getQuantity());
    }
  }
  
}
