/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;

import javax.time.calendar.OffsetDateTime;

import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Test ComputationTargetType.
 */
@Test
public class ComputationTargetTypeTest {

  private static final Portfolio PORTFOLIO = new SimplePortfolio(UniqueId.of("Test", "1"), "Name");
  private static final SimplePortfolioNode NODE = new SimplePortfolioNode();
  private static final Position POSITION = new SimplePosition(UniqueId.of("Test", "1"), new BigDecimal(1), ExternalIdBundle.EMPTY);
  private static final Security SECURITY = new SimpleSecurity("");
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.now();
  private static final Trade TRADE = new SimpleTrade(POSITION.getUniqueId(), SECURITY, new BigDecimal(1), 
      new SimpleCounterparty(ExternalId.of("CPARTY", "C100")), TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());

  public void determine() {
    
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.determineFromTarget(NODE));
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, ComputationTargetType.determineFromTarget(PORTFOLIO));
    
    assertEquals(ComputationTargetType.POSITION, ComputationTargetType.determineFromTarget(POSITION));
    
    assertEquals(ComputationTargetType.TRADE, ComputationTargetType.determineFromTarget(TRADE));
    
    assertEquals(ComputationTargetType.SECURITY, ComputationTargetType.determineFromTarget(SECURITY));
    
    assertEquals(ComputationTargetType.PRIMITIVE, ComputationTargetType.determineFromTarget(null));
    assertEquals(ComputationTargetType.PRIMITIVE, ComputationTargetType.determineFromTarget("Kirk Wylie"));
  }

  public void compatible() {
    assertTrue(ComputationTargetType.PORTFOLIO_NODE.isCompatible(NODE));
    assertTrue(ComputationTargetType.PORTFOLIO_NODE.isCompatible(PORTFOLIO));
    
    assertTrue(ComputationTargetType.POSITION.isCompatible(POSITION));
    
    assertTrue(ComputationTargetType.TRADE.isCompatible(TRADE));
    
    assertTrue(ComputationTargetType.SECURITY.isCompatible(SECURITY));
    
    assertTrue(ComputationTargetType.PRIMITIVE.isCompatible(null));
  }

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
