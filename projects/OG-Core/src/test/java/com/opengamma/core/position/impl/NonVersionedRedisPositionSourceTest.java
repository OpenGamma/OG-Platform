/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.AbstractRedisTestCase;

/**
 * 
 */
@Test(enabled=false)
public class NonVersionedRedisPositionSourceTest extends AbstractRedisTestCase {
  
  public void empty() {
    NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());
    
    assertNull(source.getPortfolio(UniqueId.of("TEST", "NONE"), null));
    assertNull(source.getPosition(UniqueId.of("TEST", "NONE")));
  }
  
  public void positionAddGet() {
    NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());
    
    SimplePosition position = new SimplePosition();
    position.setQuantity(new BigDecimal(432));
    position.setSecurityLink(new SimpleSecurityLink(ExternalId.of("Test", "Pos-1")));
    position.addAttribute("Att-1", "Value-1");
    position.addAttribute("Att-2", "Value-2");
    
    UniqueId uniqueId = source.storePosition(position);
    
    Position result = source.getPosition(uniqueId);
    assertNotNull(result);
    assertEquals(uniqueId, result.getUniqueId());
    assertEquals(position.getQuantity(), result.getQuantity());
    assertEquals(ExternalIdBundle.of(ExternalId.of("Test", "Pos-1")), position.getSecurityLink().getExternalId());
    assertEquals("Value-1", position.getAttributes().get("Att-1"));
    assertEquals("Value-2", position.getAttributes().get("Att-2"));
  }

  public void positionStoreQuantityChange() {
    NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());
    
    SimplePosition position = new SimplePosition();
    position.setQuantity(new BigDecimal(432));
    position.setSecurityLink(new SimpleSecurityLink(ExternalId.of("Test", "Pos-1")));
    
    UniqueId uniqueId = source.storePosition(position);
    position.setQuantity(new BigDecimal(9999));
    position.setUniqueId(uniqueId);
    source.updatePositionQuantity(position);
    
    Position result = source.getPosition(uniqueId);
    assertNotNull(result);
    assertEquals(uniqueId, result.getUniqueId());
    assertEquals(position.getQuantity(), result.getQuantity());
  }

  public void portfolioAddGet() {
    NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());
    
    SimplePortfolio portfolio = new SimplePortfolio("Fibble");
    portfolio.setRootNode(new SimplePortfolioNode());
    int nPositions = 100;
    for (int i = 0; i < nPositions; i++) {
      portfolio.getRootNode().addPosition(addPosition(i));
    }
    
    UniqueId uniqueId = source.storePortfolio(portfolio);
    
    Portfolio result = source.getPortfolio(uniqueId, null);
    assertNotNull(result);
    assertEquals("Fibble", result.getName());
    assertNotNull(result.getRootNode());
    assertTrue((result.getRootNode().getChildNodes() == null) || result.getRootNode().getChildNodes().isEmpty());
    assertEquals(nPositions, result.getRootNode().getPositions().size());
  }
  
  public void portfolioAddGetByName() {
    NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());
    
    SimplePortfolio portfolio = new SimplePortfolio("Fibble");
    portfolio.setRootNode(new SimplePortfolioNode());
    int nPositions = 5;
    for (int i = 0; i < nPositions; i++) {
      portfolio.getRootNode().addPosition(addPosition(i));
    }
    
    UniqueId uniqueId = source.storePortfolio(portfolio);
    
    Portfolio result = source.getByName("Fibble");
    assertNotNull(result);
    assertEquals("Fibble", result.getName());
    assertEquals(uniqueId, result.getUniqueId());
  }
  
  protected SimplePosition addPosition(int quantity) {
    SimplePosition position = new SimplePosition();
    position.setQuantity(new BigDecimal(quantity));
    position.setSecurityLink(new SimpleSecurityLink(ExternalId.of("Test", "Pos-" + quantity)));
    return position;
  }
  
  /**
   * Test how fast we can add positions to an existing portfolio.
   * When this was run on Kirk's machine (localhost) took 9.337sec at 50k positions
   * (equivalent to 0.18674ms/position).
   */
  @Test(enabled = false)
  public void largePerformanceTest() {
    NonVersionedRedisPositionSource source = new NonVersionedRedisPositionSource(getJedisPool(), getRedisPrefix());
    SimplePortfolio portfolio = new SimplePortfolio("Fibble");
    portfolio.setRootNode(new SimplePortfolioNode());
    UniqueId uniqueId = source.storePortfolio(portfolio);
    Portfolio p = source.getPortfolio(uniqueId, null);
    
    long start = System.nanoTime();
    final int NUM_POSITIONS = 50000;
    for (int i = 0; i < NUM_POSITIONS; i++) {
      source.addPositionToPortfolio(p, addPosition(i));
    }
    long end = System.nanoTime();
    double durationInSec = ((double) (end - start)) / 1000000000.0;
    System.out.println("Adding " + NUM_POSITIONS + " took " + durationInSec + " sec");
    
  }

}
