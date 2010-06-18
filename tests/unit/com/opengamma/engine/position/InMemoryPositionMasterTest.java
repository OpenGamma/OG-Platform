/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.opengamma.id.UniqueIdentifier;

/**
 * Test InMemoryPositionMaster
 */
public class InMemoryPositionMasterTest {
   
  @Test
  public void TestPositionMasterLifecycle() {   
    Portfolio portfolioA = PositionMasterTestUtil.constructTestPortfolio(UniqueIdentifier.of("Test", "portA"), "Portfolio A");
    Portfolio portfolioB = PositionMasterTestUtil.constructTestPortfolio(UniqueIdentifier.of("Test", "portB"), "Portfolio B");
    
    InMemoryPositionMaster pm = new InMemoryPositionMaster();
    pm.addPortfolio(portfolioA);
    pm.addPortfolio(portfolioB);
    
    assertEquals(2, pm.getPortfolioIds().size());
    assertTrue(pm.getPortfolioIds().contains(portfolioA.getUniqueIdentifier()));
    assertTrue(pm.getPortfolioIds().contains(portfolioB.getUniqueIdentifier()));
    
    assertNull(pm.getPortfolioNode(null));
    assertNull(pm.getPosition(null));
    
    PositionMasterTestUtil.testNodeLookup(pm, portfolioA, true);
    PositionMasterTestUtil.testNodeLookup(pm, portfolioB, true);
    
    pm.removePortfolio(portfolioA);
    assertEquals(1, pm.getPortfolioIds().size());
    PositionMasterTestUtil.testNodeLookup(pm, portfolioA, false);
    PositionMasterTestUtil.testNodeLookup(pm, portfolioB, true);
    
    pm.removePortfolio(portfolioB);
    assertEquals(0, pm.getPortfolioIds().size());
    PositionMasterTestUtil.testNodeLookup(pm, portfolioA, false);
    PositionMasterTestUtil.testNodeLookup(pm, portfolioB, false);
  }
  
}
