package com.opengamma.core.position.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import javax.time.Instant;

import org.junit.Test;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.test.MockSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test TradeImpl.
 */
public class TradeImplTest {
  
  private static final Counterparty COUNTERPARTY = new CounterpartyImpl(Identifier.of("CPARTY", "C100"));
  private static final UniqueIdentifier POSITION_UID = UniqueIdentifier.of("P", "A");
  private static final Position POSITION = new PositionImpl(POSITION_UID, BigDecimal.ONE, Identifier.of("A", "B"));
  private static final Instant TRADE_INSTANT = Instant.now();

  @Test
  public void test_construction_Position_BigDecimal_Counterparty_Instant() {
    TradeImpl test = new TradeImpl(POSITION, BigDecimal.ONE, COUNTERPARTY, TRADE_INSTANT);
    assertNull(test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(POSITION_UID, test.getPosition());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertNull(test.getSecurity());
    
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_Position_BigDecimal_Counterparty_Instant_nullPosition() {
    new TradeImpl(null, BigDecimal.ONE, COUNTERPARTY, TRADE_INSTANT);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test_construction_Position_BigDecimal_Counterparty_Instant_nullBigDecimal() {
    new TradeImpl(POSITION, null, COUNTERPARTY, TRADE_INSTANT);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test_construction_Position_BigDecimal_Counterparty_Instant_nullCounterparty() {
    new TradeImpl(POSITION, BigDecimal.ONE, null, TRADE_INSTANT);
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test_construction_Position_BigDecimal_Counterparty_Instant_nullInstant() {
    new TradeImpl(POSITION, BigDecimal.ONE, COUNTERPARTY, null);
  }
  
  @Test
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_Instant() {
    IdentifierBundle securityKey = IdentifierBundle.of(Identifier.of("A", "B"));
    TradeImpl test = new TradeImpl(POSITION_UID, securityKey, BigDecimal.ONE, COUNTERPARTY, TRADE_INSTANT);
    assertNull(test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(POSITION_UID, test.getPosition());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertNull(test.getSecurity());
  }
  
  @Test
  public void test_construction_UniqueIdentifier_Security_BigDecimal_Counterparty_Instant() {
    
    IdentifierBundle securityKey = IdentifierBundle.of(Identifier.of("A", "B"));
    MockSecurity security = new MockSecurity("A");
    security.setIdentifiers(securityKey);
    
    TradeImpl test = new TradeImpl(POSITION_UID, security, BigDecimal.ONE, COUNTERPARTY, TRADE_INSTANT);
    assertNull(test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(POSITION_UID, test.getPosition());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertEquals(security, test.getSecurity());
  }
  
}
