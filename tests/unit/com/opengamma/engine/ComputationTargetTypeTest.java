/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import org.junit.Test;

import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionBean;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.id.DomainSpecificIdentifiers;

/**
 * 
 *
 * @author kirk
 */
public class ComputationTargetTypeTest {
  private static final Position POSITION = new PositionBean(new BigDecimal(1), new DomainSpecificIdentifiers());
  private static final Security SECURITY = new DefaultSecurity();

  @Test
  public void determine() {
    assertEquals(ComputationTargetType.POSITION, ComputationTargetType.determineFromTarget(POSITION));
    assertEquals(ComputationTargetType.SECURITY, ComputationTargetType.determineFromTarget(SECURITY));
    assertEquals(ComputationTargetType.PRIMITIVE, ComputationTargetType.determineFromTarget(null));
    assertEquals(ComputationTargetType.MULTIPLE_POSITIONS, ComputationTargetType.determineFromTarget(new PortfolioNodeImpl()));
    assertNull(ComputationTargetType.determineFromTarget("Kirk Wylie"));
  }
  
  @Test
  public void compatible() {
    assertTrue(ComputationTargetType.isCompatible(ComputationTargetType.POSITION, POSITION));
    assertTrue(ComputationTargetType.isCompatible(ComputationTargetType.SECURITY, SECURITY));
    assertTrue(ComputationTargetType.isCompatible(ComputationTargetType.PRIMITIVE, null));
    assertTrue(ComputationTargetType.isCompatible(ComputationTargetType.MULTIPLE_POSITIONS, new PortfolioNodeImpl()));
  }
  
  @Test
  public void notCompatible() {
    assertFalse(ComputationTargetType.isCompatible(ComputationTargetType.POSITION, SECURITY));
    assertFalse(ComputationTargetType.isCompatible(ComputationTargetType.SECURITY, POSITION));
    assertFalse(ComputationTargetType.isCompatible(ComputationTargetType.PRIMITIVE, SECURITY));
    assertFalse(ComputationTargetType.isCompatible(ComputationTargetType.MULTIPLE_POSITIONS, POSITION));
  }
}
