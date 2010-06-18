/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Timer;
import java.util.TimerTask;

import net.sf.ehcache.CacheManager;

import org.junit.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test ExpiringInMemoryPositionMaster
 */
public class ExpiringInMemoryPositionMasterTest {

  @Test
  public void TestPortfolioExpiry() throws InterruptedException {
    final UniqueIdentifier ownerA = UniqueIdentifier.of("Owner", "A");
    final Portfolio portfolioA1 = PositionMasterTestUtil.constructTestPortfolio(null, "Portfolio A1");
    final Portfolio portfolioA2 = PositionMasterTestUtil.constructTestPortfolio(null, "Portfolio A2");
    final UniqueIdentifier ownerB = UniqueIdentifier.of("Owner", "B");
    final Portfolio portfolioB = PositionMasterTestUtil.constructTestPortfolio(null, "Portfolio B");
    
    final ExpiringInMemoryPositionMaster<UniqueIdentifier> pm = new ExpiringInMemoryPositionMaster<UniqueIdentifier>(1, 1, new CacheManager(), new Timer());
    pm.addPortfolio(ownerA, portfolioA1);
    pm.addPortfolio(ownerA, portfolioA2);
    pm.addPortfolio(ownerB, portfolioB);
    
    PositionMasterTestUtil.testNodeLookup(pm, portfolioA1, true);
    PositionMasterTestUtil.testNodeLookup(pm, portfolioA2, true);
    PositionMasterTestUtil.testNodeLookup(pm, portfolioB, true);
    
    Timer timer = new Timer();
    TimerTask heartbeater = new TimerTask() {

      @Override
      public void run() {
        // Should keep the two portfolios owned by ownerA alive
        pm.heartbeat(ownerA);
      }
      
    };
    timer.scheduleAtFixedRate(heartbeater, 500, 500);
    
    // Allow the portfolio owned by ownerB to expire
    Thread.sleep(2500);
    
    // Should now only contain the two portfolios owned by ownerA
    assertEquals(2, pm.getPortfolioIds().size());
    assertTrue(pm.getPortfolioIds().contains(portfolioA1.getUniqueIdentifier()));
    assertTrue(pm.getPortfolioIds().contains(portfolioA2.getUniqueIdentifier()));
    
    // Allow the two portfolios owned by ownerA to expire
    heartbeater.cancel();
    
    Thread.sleep(2500);
    assertEquals(0, pm.getPortfolioIds().size());
  }
  
  @Test(expected=OpenGammaRuntimeException.class)
  public void TestAddOldPortfolio() {
    ExpiringInMemoryPositionMaster<UniqueIdentifier> pm = new ExpiringInMemoryPositionMaster<UniqueIdentifier>(1, 1, new CacheManager(), new Timer());
    Portfolio portfolio = PositionMasterTestUtil.constructTestPortfolio(UniqueIdentifier.of("Test", "portA"), "Portfolio A");
    pm.addPortfolio(UniqueIdentifier.of("Owner", "A"), portfolio);
  }
  
}