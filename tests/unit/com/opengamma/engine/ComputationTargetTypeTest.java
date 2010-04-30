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
import com.opengamma.engine.position.PositionBean;
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
  private static final Position POSITION = new PositionBean(UniqueIdentifier.of("Test", "1"), new BigDecimal(1), new IdentifierBundle());
  private static final Security SECURITY = new DefaultSecurity();

  @Test
  public void determine() {
    assertEquals(ComputationTargetType.MULTIPLE_POSITIONS, ComputationTargetType.determineFromTarget(NODE));
    assertEquals(ComputationTargetType.MULTIPLE_POSITIONS, ComputationTargetType.determineFromTarget(PORTFOLIO));
    
    assertEquals(ComputationTargetType.POSITION, ComputationTargetType.determineFromTarget(POSITION));
    
    assertEquals(ComputationTargetType.SECURITY, ComputationTargetType.determineFromTarget(SECURITY));
    
    assertEquals(ComputationTargetType.PRIMITIVE, ComputationTargetType.determineFromTarget(null));
    assertEquals(ComputationTargetType.PRIMITIVE, ComputationTargetType.determineFromTarget("Kirk Wylie"));
  }

  @Test
  public void compatible() {
    assertTrue(ComputationTargetType.MULTIPLE_POSITIONS.isCompatible(NODE));
    assertTrue(ComputationTargetType.MULTIPLE_POSITIONS.isCompatible(PORTFOLIO));
    
    assertTrue(ComputationTargetType.POSITION.isCompatible(POSITION));
    
    assertTrue(ComputationTargetType.SECURITY.isCompatible(SECURITY));
    
    assertTrue(ComputationTargetType.PRIMITIVE.isCompatible(null));
  }

  @Test
  public void notCompatible() {
    assertFalse(ComputationTargetType.MULTIPLE_POSITIONS.isCompatible(POSITION));
    assertFalse(ComputationTargetType.MULTIPLE_POSITIONS.isCompatible(SECURITY));
    
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
