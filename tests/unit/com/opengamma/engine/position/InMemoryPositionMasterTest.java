/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test InMemoryPositionMaster
 */
public class InMemoryPositionMasterTest {
  
  private Portfolio constructTestPortfolio(String uidValue, String name) {
    PortfolioImpl portfolio = new PortfolioImpl(UniqueIdentifier.of("Test", uidValue), name);
    PortfolioNodeImpl rootNode = new PortfolioNodeImpl();
    
    portfolio.setRootNode(rootNode);
    populateNode(rootNode, uidValue, "0", 3);
    
    return portfolio;
  }

  private void populateNode(PortfolioNodeImpl node, String uidValue, String discriminator, int depth) {   
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
      populateNode(node, uidValue, discriminator + "-" + i, depth - 1);
    }
  }
  
  private void testNodeLookup(PositionMaster pm, PortfolioNode node, boolean isPresent) {
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
  
  @Test
  public void TestPositionMasterLifecycle() {   
    Portfolio portfolioA = constructTestPortfolio("portA", "Portfolio A");
    Portfolio portfolioB = constructTestPortfolio("portB", "Portfolio B");
    
    InMemoryPositionMaster pm = new InMemoryPositionMaster();
    pm.addPortfolio(portfolioA);
    pm.addPortfolio(portfolioB);
    
    assertEquals(2, pm.getPortfolioIds().size());
    assertTrue(pm.getPortfolioIds().contains(portfolioA.getUniqueIdentifier()));
    assertTrue(pm.getPortfolioIds().contains(portfolioB.getUniqueIdentifier()));
    
    assertNull(pm.getPortfolioNode(null));
    assertNull(pm.getPosition(null));
    
    testNodeLookup(pm, portfolioA.getRootNode(), true);
    testNodeLookup(pm, portfolioB.getRootNode(), true);
    
    pm.removePortfolio(portfolioA);
    assertEquals(1, pm.getPortfolioIds().size());
    testNodeLookup(pm, portfolioA.getRootNode(), false);
    testNodeLookup(pm, portfolioB.getRootNode(), true);
    
    pm.removePortfolio(portfolioB);
    assertEquals(0, pm.getPortfolioIds().size());
    testNodeLookup(pm, portfolioA.getRootNode(), false);
    testNodeLookup(pm, portfolioB.getRootNode(), false);
  }
  
}
