/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.List;

import javax.time.calendar.OffsetDateTime;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.test.MockSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.money.Currency;

/**
 * Test TradeImpl.
 */
@Test
public class TradeImplTest {
  
  private static final Counterparty COUNTERPARTY = new CounterpartyImpl(Identifier.of("CPARTY", "C100"));
  private static final UniqueIdentifier POSITION_UID = UniqueIdentifier.of("P", "A");
  private static final Position POSITION = new PositionImpl(POSITION_UID, BigDecimal.ONE, Identifier.of("A", "B"));
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.now();

  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime() {
    TradeImpl test = new TradeImpl(POSITION.getUniqueId(), POSITION.getSecurityKey(), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertNull(test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(POSITION_UID, test.getParentPositionId());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertNull(test.getSecurity());
    assertEquals(TRADE_OFFSET_DATETIME.toLocalDate(), test.getTradeDate());
    assertEquals(TRADE_OFFSET_DATETIME.toOffsetTime(), test.getTradeTime());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullUniqueIdentifier() {
    new TradeImpl(null, POSITION.getSecurityKey(), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullIdentifierBundle() {
    new TradeImpl(POSITION.getUniqueId(), (IdentifierBundle) null, BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullBigDecimal() {
    new TradeImpl(POSITION.getUniqueId(), POSITION.getSecurityKey(), null, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullCounterparty() {
    new TradeImpl(POSITION.getUniqueId(), POSITION.getSecurityKey(), BigDecimal.ONE, null, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_IdentifierBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullLocalDate() {
    new TradeImpl(POSITION.getUniqueId(), POSITION.getSecurityKey(), BigDecimal.ONE, COUNTERPARTY, null, TRADE_OFFSET_DATETIME.toOffsetTime());
  }
  
  public void test_construction_UniqueIdentifier_Security_BigDecimal_Counterparty_Instant() {
    
    IdentifierBundle securityKey = IdentifierBundle.of(Identifier.of("A", "B"));
    MockSecurity security = new MockSecurity("A");
    security.setIdentifiers(securityKey);
    
    TradeImpl test = new TradeImpl(POSITION_UID, security, BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertNull(test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(POSITION_UID, test.getParentPositionId());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertEquals(security, test.getSecurity());
  }
  
  public void test_equals() {
    List<TradeImpl> trades = Lists.newArrayList();
    
    TradeImpl trade1 = new TradeImpl(POSITION_UID, Identifier.of("A", "B"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trades.add(trade1);
    
    TradeImpl trade2 = new TradeImpl(POSITION_UID, Identifier.of("C", "D"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trade2.setPremuim(100.00);
    trade2.setPremiumCurrency(Currency.USD);
    trade2.setPremiumDate(TRADE_OFFSET_DATETIME.toLocalDate().plusDays(1));
    trade2.setPremiumTime(TRADE_OFFSET_DATETIME.toOffsetTime().plusHours(1));
    trades.add(trade2);
    
    TradeImpl trade3 = new TradeImpl(POSITION_UID, Identifier.of("E", "F"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trades.add(trade3);
    
    assertEquals(3, trades.size());
    assertTrue(trades.contains(trade1));
    assertTrue(trades.contains(trade2));
    assertTrue(trades.contains(trade3));
    
  }
  
}
