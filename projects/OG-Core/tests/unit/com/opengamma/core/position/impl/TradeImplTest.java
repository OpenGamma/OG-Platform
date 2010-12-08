package com.opengamma.core.position.impl;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.math.BigDecimal;

import javax.time.calendar.OffsetDateTime;

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
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.nowSystemClock();

  @Test
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime() {
    TradeImpl test = new TradeImpl(POSITION.getUniqueIdentifier(), POSITION.getSecurityKey(), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertNull(test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(POSITION_UID, test.getPositionId());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertNull(test.getSecurity());
    assertEquals(TRADE_OFFSET_DATETIME.toLocalDate(), test.getTradeDate());
    assertEquals(TRADE_OFFSET_DATETIME.toOffsetTime(), test.getTradeTime());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullUniqueIdentifier() {
    new TradeImpl(null, POSITION.getSecurityKey(), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullIdentifierBundle() {
    new TradeImpl(POSITION.getUniqueIdentifier(), (IdentifierBundle) null, BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullBigDecimal() {
    new TradeImpl(POSITION.getUniqueIdentifier(), POSITION.getSecurityKey(), null, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullCounterparty() {
    new TradeImpl(POSITION.getUniqueIdentifier(), POSITION.getSecurityKey(), BigDecimal.ONE, null, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullLocalDate() {
    new TradeImpl(POSITION.getUniqueIdentifier(), POSITION.getSecurityKey(), BigDecimal.ONE, COUNTERPARTY, null, TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  @Test
  public void test_construction_UniqueIdentifier_Security_BigDecimal_Counterparty_Instant() {
    
    IdentifierBundle securityKey = IdentifierBundle.of(Identifier.of("A", "B"));
    MockSecurity security = new MockSecurity("A");
    security.setIdentifiers(securityKey);
    
    TradeImpl test = new TradeImpl(POSITION_UID, security, BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertNull(test.getUniqueIdentifier());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(POSITION_UID, test.getPositionId());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertEquals(security, test.getSecurity());
  }
  
}
