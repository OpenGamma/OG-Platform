/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;

import javax.time.calendar.OffsetDateTime;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.test.MockSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test PositionImpl.
 */
public class PositionImplTest {
  
  private static final Counterparty COUNTERPARTY = new CounterpartyImpl(Identifier.of("CPARTY", "C100"));
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.now();

  @Test
  public void test_construction_BigDecimal_Identifier() {
    PositionImpl test = new PositionImpl(BigDecimal.ONE, Identifier.of("A", "B"));
    assertEquals(null, test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals("Position[, 1 Bundle[A::B]]", test.toString());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_BigDecimal_Identifier_nullBigDecimal() {
    new PositionImpl(null, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_BigDecimal_Identifier_nullIdentifier() {
    new PositionImpl(BigDecimal.ONE, (Identifier) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_BigDecimal_IdentifierBundle() {
    PositionImpl test = new PositionImpl(BigDecimal.ONE, IdentifierBundle.of(Identifier.of("A", "B")));
    assertEquals(null, test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals("Position[, 1 Bundle[A::B]]", test.toString());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_BigDecimal_IdentifierBundle_nullBigDecimal() {
    new PositionImpl(null, IdentifierBundle.of(Identifier.of("A", "B")));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_BigDecimal_IdentifierBundle_nullIdentifier() {
    new PositionImpl(BigDecimal.ONE, (IdentifierBundle) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_UniqueIdentifier_BigDecimal_Identifier() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertEquals(UniqueIdentifier.of("B", "C"), test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals("Position[B::C, 1 Bundle[A::B]]", test.toString());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Identifier_nullUniqueIdentifier() {
    new PositionImpl(null, BigDecimal.ONE, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Identifier_nullBigDecimal() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), null, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Identifier_nullIdentifier() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, (Identifier) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_UniqueIdentifier_BigDecimal_IdentifierBundle() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, IdentifierBundle.of(Identifier.of("A", "B")));
    assertEquals(UniqueIdentifier.of("B", "C"), test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals("Position[B::C, 1 Bundle[A::B]]", test.toString());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_IdentifierBundle_nullUniqueIdentifier() {
    new PositionImpl(null, BigDecimal.ONE, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_IdentifierBundle_nullBigDecimal() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), null, IdentifierBundle.of(Identifier.of("A", "B")));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_IdentifierBundle_nullIdentifierBundle() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, (IdentifierBundle) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_construction_UniqueIdentifier_BigDecimal_Security() {
    MockSecurity sec = new MockSecurity("A");
    sec.setIdentifiers(IdentifierBundle.of(Identifier.of("A", "B")));
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, sec);
    assertEquals(UniqueIdentifier.of("B", "C"), test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityKey().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityKey().getIdentifiers().iterator().next());
    assertEquals(true, test.toString().startsWith("Position[B::C, 1"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Security_nullUniqueIdentifier() {
    new PositionImpl(null, BigDecimal.ONE, Identifier.of("A", "B"));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Security_nullBigDecimal() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), null, IdentifierBundle.of(Identifier.of("A", "B")));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_construction_UniqueIdentifier_BigDecimal_Security_nullSecurity() {
    new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, (Security) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setUniqueId() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setUniqueId(UniqueIdentifier.of("B", "D"));
    assertEquals(UniqueIdentifier.of("B", "D"), test.getUniqueId());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_setUniqueId_null() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setUniqueId(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setQuantity() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setQuantity(BigDecimal.ZERO);
    assertSame(BigDecimal.ZERO, test.getQuantity());
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_setQuantity_null() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setQuantity(null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setSecurityKey() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setSecurityKey(IdentifierBundle.EMPTY);
    assertSame(IdentifierBundle.EMPTY, test.getSecurityKey());
  }

  @Test
  public void test_setSecurityKey_nullAllowed() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setSecurityKey(null);
    assertEquals(null, test.getSecurityKey());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_setSecurity() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    Security sec = new MockSecurity("");
    test.setSecurity(sec);
    assertSame(sec, test.getSecurity());
  }

  @Test
  public void test_setSecurity_nullAllowed() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setSecurity(null);
    assertEquals(null, test.getSecurity());
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_addTrade() {
    PositionImpl testPosition = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertTrue(testPosition.getTrades().isEmpty());
    TradeImpl testTrade1 = new TradeImpl(testPosition.getUniqueId(), Identifier.of("A", "B"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade1);
    
    TradeImpl testTrade2 = new TradeImpl(testPosition.getUniqueId(), Identifier.of("C", "D"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade2);
    
    assertEquals(2, testPosition.getTrades().size());
    assertTrue(testPosition.getTrades().containsAll(Lists.newArrayList(testTrade1, testTrade2)));
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_addTrade_null() {
    PositionImpl test = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.addTrade(null);
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_removeTrade() {
    PositionImpl testPosition = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertTrue(testPosition.getTrades().isEmpty());
    TradeImpl testTrade1 = new TradeImpl(testPosition.getUniqueId(), Identifier.of("A", "B"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade1);
    TradeImpl testTrade2 = new TradeImpl(testPosition.getUniqueId(), Identifier.of("C", "D"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade2);
    
    TradeImpl testTrade3 = new TradeImpl(testPosition.getUniqueId(), Identifier.of("E", "F"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    
    assertTrue(testPosition.removeTrade(testTrade1));
    assertTrue(testPosition.removeTrade(testTrade2));
    assertFalse(testPosition.removeTrade(testTrade3));
    assertTrue(testPosition.getTrades().isEmpty());
    
  }
  
  //-------------------------------------------------------------------------
  @Test
  public void test_getTrades_readOnly() {
    PositionImpl testPosition = new PositionImpl(UniqueIdentifier.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    TradeImpl testTrade = new TradeImpl(testPosition.getUniqueId(), Identifier.of("A", "B"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    int sizeBeforeAddition = testPosition.getTrades().size();
    try {
      testPosition.getTrades().add(testTrade);
    } catch (Exception ex) {
      //do nothing
    }
    assertEquals(sizeBeforeAddition, testPosition.getTrades().size());
  }

}
