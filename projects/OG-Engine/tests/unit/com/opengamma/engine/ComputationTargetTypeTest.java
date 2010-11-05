/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test ComputationTargetType.
 */
public class ComputationTargetTypeTest {

  private static final Portfolio PORTFOLIO = new PortfolioImpl(UniqueIdentifier.of("Test", "1"), "Name");
  private static final PortfolioNodeImpl NODE = new PortfolioNodeImpl();
  private static final Position POSITION = new PositionImpl(UniqueIdentifier.of("Test", "1"), new BigDecimal(1), IdentifierBundle.EMPTY);
  private static final Security SECURITY = new DefaultSecurity("");

  @Test
  public void determine() {
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.determineFromTarget(NODE));
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.determineFromTarget(PORTFOLIO));
    
    assertEquals(ComputationTargetType.POSITION, ComputationTargetType.determineFromTarget(POSITION));
    
    assertEquals(ComputationTargetType.SECURITY, ComputationTargetType.determineFromTarget(SECURITY));
    
    assertEquals(ComputationTargetType.PRIMITIVE, ComputationTargetType.determineFromTarget(null));
    assertEquals(ComputationTargetType.PRIMITIVE, ComputationTargetType.determineFromTarget("Kirk Wylie"));
  }

  @Test
  public void compatible() {
    assertTrue(ComputationTargetType.PORTFOLIO_NODE.isCompatible(NODE));
    assertTrue(ComputationTargetType.PORTFOLIO_NODE.isCompatible(PORTFOLIO));
    
    assertTrue(ComputationTargetType.POSITION.isCompatible(POSITION));
    
    assertTrue(ComputationTargetType.SECURITY.isCompatible(SECURITY));
    
    assertTrue(ComputationTargetType.PRIMITIVE.isCompatible(null));
  }

  @Test
  public void notCompatible() {
    assertFalse(ComputationTargetType.PORTFOLIO_NODE.isCompatible(POSITION));
    assertFalse(ComputationTargetType.PORTFOLIO_NODE.isCompatible(SECURITY));
    
    assertFalse(ComputationTargetType.POSITION.isCompatible(PORTFOLIO));
    assertFalse(ComputationTargetType.POSITION.isCompatible(NODE));
    assertFalse(ComputationTargetType.POSITION.isCompatible(SECURITY));
    
    assertFalse(ComputationTargetType.SECURITY.isCompatible(PORTFOLIO));
    assertFalse(ComputationTargetType.SECURITY.isCompatible(NODE));
    assertFalse(ComputationTargetType.SECURITY.isCompatible(POSITION));
    
    assertFalse(ComputationTargetType.PRIMITIVE.isCompatible(PORTFOLIO));
    assertFalse(ComputationTargetType.PRIMITIVE.isCompatible(NODE));
    assertFalse(ComputationTargetType.PRIMITIVE.isCompatible(POSITION));
    assertFalse(ComputationTargetType.PRIMITIVE.isCompatible(SECURITY));
  }

}
