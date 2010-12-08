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

import javax.time.calendar.OffsetDateTime;

import org.junit.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.CounterpartyImpl;
import com.opengamma.core.position.impl.PortfolioImpl;
import com.opengamma.core.position.impl.PortfolioNodeImpl;
import com.opengamma.core.position.impl.PositionImpl;
import com.opengamma.core.position.impl.TradeImpl;
import com.opengamma.core.security.Security;
import com.opengamma.engine.security.MockSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test ComputationTargetType.
 */
public class ComputationTargetTypeTest {

  private static final Portfolio PORTFOLIO = new PortfolioImpl(UniqueIdentifier.of("Test", "1"), "Name");
  private static final PortfolioNodeImpl NODE = new PortfolioNodeImpl();
  private static final Position POSITION = new PositionImpl(UniqueIdentifier.of("Test", "1"), new BigDecimal(1), IdentifierBundle.EMPTY);
  private static final Security SECURITY = new MockSecurity("");
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.nowSystemClock();
  private static final Trade TRADE = new TradeImpl(POSITION.getUniqueIdentifier(), SECURITY, new BigDecimal(1), 
      new CounterpartyImpl(Identifier.of("CPARTY", "C100")), TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());

  @Test
  public void determine() {
    
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.determineFromTarget(NODE));
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.determineFromTarget(PORTFOLIO));
    
    assertEquals(ComputationTargetType.POSITION, ComputationTargetType.determineFromTarget(POSITION));
    
    assertEquals(ComputationTargetType.TRADE, ComputationTargetType.determineFromTarget(TRADE));
    
    assertEquals(ComputationTargetType.SECURITY, ComputationTargetType.determineFromTarget(SECURITY));
    
    assertEquals(ComputationTargetType.PRIMITIVE, ComputationTargetType.determineFromTarget(null));
    assertEquals(ComputationTargetType.PRIMITIVE, ComputationTargetType.determineFromTarget("Kirk Wylie"));
  }

  @Test
  public void compatible() {
    assertTrue(ComputationTargetType.PORTFOLIO_NODE.isCompatible(NODE));
    assertTrue(ComputationTargetType.PORTFOLIO_NODE.isCompatible(PORTFOLIO));
    
    assertTrue(ComputationTargetType.POSITION.isCompatible(POSITION));
    
    assertTrue(ComputationTargetType.TRADE.isCompatible(TRADE));
    
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
    
    assertFalse(ComputationTargetType.TRADE.isCompatible(PORTFOLIO));
    assertFalse(ComputationTargetType.TRADE.isCompatible(NODE));
    assertFalse(ComputationTargetType.TRADE.isCompatible(SECURITY));
    
    assertFalse(ComputationTargetType.SECURITY.isCompatible(PORTFOLIO));
    assertFalse(ComputationTargetType.SECURITY.isCompatible(NODE));
    assertFalse(ComputationTargetType.SECURITY.isCompatible(POSITION));
    
    assertFalse(ComputationTargetType.PRIMITIVE.isCompatible(PORTFOLIO));
    assertFalse(ComputationTargetType.PRIMITIVE.isCompatible(NODE));
    assertFalse(ComputationTargetType.PRIMITIVE.isCompatible(POSITION));
    assertFalse(ComputationTargetType.PRIMITIVE.isCompatible(SECURITY));
  }

}
