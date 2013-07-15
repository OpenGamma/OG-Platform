/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;


import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;
import java.util.Set;

import org.testng.annotations.Test;
import org.threeten.bp.OffsetDateTime;

import com.google.common.collect.Sets;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimpleTrade}.
 */
@Test(groups = TestGroup.UNIT)
public class SimpleTradeTest {

  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(ExternalId.of("CPARTY", "C100"));
  private static final UniqueId POSITION_UID = UniqueId.of("P", "A");
  private static final Position POSITION = new SimplePosition(POSITION_UID, BigDecimal.ONE, ExternalId.of("A", "B"));
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.now();
  private static final ExternalIdBundle BUNDLE = POSITION.getSecurityLink().getExternalId();

  public void test_construction_ExternalIdBundle_BigDecimal_Counterparty_LocalDate_OffsetTime() {
    SimpleTrade test = new SimpleTrade(new SimpleSecurityLink(BUNDLE), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertNull(test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getExternalId().size());
    assertEquals(ExternalId.of("A", "B"), test.getSecurityLink().getExternalId().iterator().next());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertNull(test.getSecurityLink().getTarget());
    assertEquals(TRADE_OFFSET_DATETIME.toLocalDate(), test.getTradeDate());
    assertEquals(TRADE_OFFSET_DATETIME.toOffsetTime(), test.getTradeTime());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_ExternalIdBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullLink() {
    new SimpleTrade((SecurityLink) null, BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_ExternalIdBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullBigDecimal() {
    new SimpleTrade(new SimpleSecurityLink(BUNDLE), null, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_ExternalIdBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullCounterparty() {
    new SimpleTrade(new SimpleSecurityLink(BUNDLE), BigDecimal.ONE, null, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_ExternalIdBundle_BigDecimal_Counterparty_LocalDate_OffsetTime_nullLocalDate() {
    new SimpleTrade(new SimpleSecurityLink(BUNDLE), BigDecimal.ONE, COUNTERPARTY, null, TRADE_OFFSET_DATETIME.toOffsetTime());
  }

  public void test_construction_Security_BigDecimal_Counterparty_Instant() {
    ExternalIdBundle securityKey = ExternalIdBundle.of(ExternalId.of("A", "B"));
    SimpleSecurity security = new SimpleSecurity("A");
    security.setExternalIdBundle(securityKey);
    
    SimpleTrade test = new SimpleTrade(security, BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertNull(test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getExternalId().size());
    assertEquals(ExternalId.of("A", "B"), test.getSecurityLink().getExternalId().iterator().next());
    assertEquals(COUNTERPARTY, test.getCounterparty());
    assertEquals(security, test.getSecurityLink().getTarget());
  }

  public void test_construction_copyFromPosition() {
    SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trade.addAttribute("A", "B");
    trade.addAttribute("C", "D");
    
    SimpleTrade copy = new SimpleTrade(trade);
    assertEquals(copy, trade);
  }
  
  public void test_collectionsOfTradesWithDifferentFields() {
    Set<SimpleTrade> trades = Sets.newHashSet();
    
    SimpleTrade trade1 = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trades.add(trade1);
    
    SimpleTrade trade2 = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("C", "D")), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trade2.setPremium(100.00);
    trade2.setPremiumCurrency(Currency.USD);
    trade2.setPremiumDate(TRADE_OFFSET_DATETIME.toLocalDate().plusDays(1));
    trade2.setPremiumTime(TRADE_OFFSET_DATETIME.toOffsetTime().plusHours(1));
    trades.add(trade2);
    
    SimpleTrade trade3 = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("E", "F")), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    trades.add(trade3);
    
    trades.add(new SimpleTrade(trade3));
    
    SimpleTrade trade4 = new SimpleTrade(trade1);
    trade4.addAttribute("key1", "value1");
    trade4.addAttribute("key2", "value2");
    trades.add(trade4);
    
    assertEquals(4, trades.size());
    assertTrue(trades.contains(trade1));
    assertTrue(trades.contains(trade2));
    assertTrue(trades.contains(trade3));
    assertTrue(trades.contains(trade4));
    
    trades.remove(trade1);
    assertEquals(3, trades.size());
    assertFalse(trades.contains(trade1));
    
    trades.remove(trade2);
    assertEquals(2, trades.size());
    assertFalse(trades.contains(trade2));
    
    trades.remove(trade3);
    assertEquals(1, trades.size());
    assertFalse(trades.contains(trade3));
    
    trades.remove(trade4);
    assertTrue(trades.isEmpty());
  }
  
  //------------------------------------------------------------------------
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_addAttribute_null_key() {
    SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertTrue(trade.getAttributes().isEmpty());
    trade.addAttribute(null, "B");
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_addAttribute_null_value() {
    SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertTrue(trade.getAttributes().isEmpty());
    trade.addAttribute("A", null);
  }
  
  public void test_addAttribute() {
    SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertTrue(trade.getAttributes().isEmpty());
    trade.addAttribute("A", "B");
    assertEquals(1, trade.getAttributes().size());
    assertEquals("B", trade.getAttributes().get("A"));
    trade.addAttribute("C", "D");
    assertEquals(2, trade.getAttributes().size());
    assertEquals("D", trade.getAttributes().get("C"));
  }
  
  public void test_removeAttribute() {
    SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(ExternalId.of("A", "B")), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    assertTrue(trade.getAttributes().isEmpty());
    trade.addAttribute("A", "B");
    trade.addAttribute("C", "D");
    assertEquals(2, trade.getAttributes().size());
    trade.removeAttribute("A");
    assertEquals(1, trade.getAttributes().size());
    assertNull(trade.getAttributes().get("A"));
  }

}
