/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.position.impl;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertSame;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;

import javax.time.calendar.OffsetDateTime;

import org.testng.annotations.Test;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.test.MockSecurity;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueId;

/**
 * Test PositionImpl.
 */
@Test
public class PositionImplTest {
  
  private static final Counterparty COUNTERPARTY = new CounterpartyImpl(Identifier.of("CPARTY", "C100"));
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.now();

  public void test_construction_BigDecimal_Identifier() {
    PositionImpl test = new PositionImpl(BigDecimal.ONE, Identifier.of("A", "B"));
    assertEquals(null, test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getBundleId().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityLink().getBundleId().iterator().next());
    assertEquals("Position[, 1 Bundle[A~B]]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_BigDecimal_Identifier_nullBigDecimal() {
    new PositionImpl(null, Identifier.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_BigDecimal_Identifier_nullIdentifier() {
    new PositionImpl(BigDecimal.ONE, (Identifier) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_BigDecimal_IdentifierBundle() {
    PositionImpl test = new PositionImpl(BigDecimal.ONE, IdentifierBundle.of(Identifier.of("A", "B")));
    assertEquals(null, test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getBundleId().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityLink().getBundleId().iterator().next());
    assertEquals("Position[, 1 Bundle[A~B]]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_BigDecimal_IdentifierBundle_nullBigDecimal() {
    new PositionImpl(null, IdentifierBundle.of(Identifier.of("A", "B")));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_BigDecimal_IdentifierBundle_nullIdentifier() {
    new PositionImpl(BigDecimal.ONE, (IdentifierBundle) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_UniqueId_BigDecimal_Identifier() {
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertEquals(UniqueId.of("B", "C"), test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getBundleId().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityLink().getBundleId().iterator().next());
    assertEquals("Position[B~C, 1 Bundle[A~B]]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_Identifier_nullUniqueId() {
    new PositionImpl(null, BigDecimal.ONE, Identifier.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_Identifier_nullBigDecimal() {
    new PositionImpl(UniqueId.of("B", "C"), null, Identifier.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_Identifier_nullIdentifier() {
    new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, (Identifier) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_UniqueId_BigDecimal_IdentifierBundle() {
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, IdentifierBundle.of(Identifier.of("A", "B")));
    assertEquals(UniqueId.of("B", "C"), test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getBundleId().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityLink().getBundleId().iterator().next());
    assertEquals("Position[B~C, 1 Bundle[A~B]]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_IdentifierBundle_nullUniqueId() {
    new PositionImpl(null, BigDecimal.ONE, Identifier.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_IdentifierBundle_nullBigDecimal() {
    new PositionImpl(UniqueId.of("B", "C"), null, IdentifierBundle.of(Identifier.of("A", "B")));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_IdentifierBundle_nullIdentifierBundle() {
    new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, (IdentifierBundle) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_UniqueId_BigDecimal_Security() {
    MockSecurity sec = new MockSecurity("A");
    sec.setIdentifiers(IdentifierBundle.of(Identifier.of("A", "B")));
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, sec);
    assertEquals(UniqueId.of("B", "C"), test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getBundleId().size());
    assertEquals(Identifier.of("A", "B"), test.getSecurityLink().getBundleId().iterator().next());
    assertEquals(true, test.toString().startsWith("Position[B~C, 1"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_Security_nullUniqueId() {
    new PositionImpl(null, BigDecimal.ONE, Identifier.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_Security_nullBigDecimal() {
    new PositionImpl(UniqueId.of("B", "C"), null, IdentifierBundle.of(Identifier.of("A", "B")));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_Security_nullSecurity() {
    new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, (Security) null);
  }
  
  public void test_construction_copyFromPosition() {
    PositionImpl position = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    position.addAttribute("A", "B");
    position.addAttribute("C", "D");
    
    PositionImpl copy = new PositionImpl(position);
    assertEquals(copy, position);
  }

  //-------------------------------------------------------------------------
  public void test_setUniqueId() {
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setUniqueId(UniqueId.of("B", "D"));
    assertEquals(UniqueId.of("B", "D"), test.getUniqueId());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setUniqueId_null() {
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setUniqueId(null);
  }

  //-------------------------------------------------------------------------
  public void test_setQuantity() {
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setQuantity(BigDecimal.ZERO);
    assertSame(BigDecimal.ZERO, test.getQuantity());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setQuantity_null() {
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setQuantity(null);
  }

  //-------------------------------------------------------------------------
  public void test_setSecurityLink() {
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setSecurityLink(new SecurityLink());
    assertEquals(new SecurityLink(), test.getSecurityLink());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setSecurityKey_null() {
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.setSecurityLink(null);
  }

  //-------------------------------------------------------------------------
  public void test_addTrade() {
    PositionImpl testPosition = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertTrue(testPosition.getTrades().isEmpty());
    TradeImpl testTrade1 = new TradeImpl(testPosition.getUniqueId(), createLink("A", "B"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade1);
    
    TradeImpl testTrade2 = new TradeImpl(testPosition.getUniqueId(), createLink("C", "D"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade2);
    
    assertEquals(2, testPosition.getTrades().size());
    assertTrue(testPosition.getTrades().containsAll(Lists.newArrayList(testTrade1, testTrade2)));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_addTrade_null() {
    PositionImpl test = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    test.addTrade(null);
  }
  
  //-------------------------------------------------------------------------
  public void test_removeTrade() {
    PositionImpl testPosition = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertTrue(testPosition.getTrades().isEmpty());
    TradeImpl testTrade1 = new TradeImpl(testPosition.getUniqueId(), createLink("A", "B"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade1);
    TradeImpl testTrade2 = new TradeImpl(testPosition.getUniqueId(), createLink("C", "D"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade2);
    
    TradeImpl testTrade3 = new TradeImpl(testPosition.getUniqueId(), createLink("E", "F"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    
    assertTrue(testPosition.removeTrade(testTrade1));
    assertTrue(testPosition.removeTrade(testTrade2));
    assertFalse(testPosition.removeTrade(testTrade3));
    assertTrue(testPosition.getTrades().isEmpty());
    
  }
  
  //-------------------------------------------------------------------------
  public void test_getTrades_readOnly() {
    PositionImpl testPosition = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    TradeImpl testTrade = new TradeImpl(testPosition.getUniqueId(), createLink("A", "B"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    int sizeBeforeAddition = testPosition.getTrades().size();
    try {
      testPosition.getTrades().add(testTrade);
    } catch (Exception ex) {
      //do nothing
    }
    assertEquals(sizeBeforeAddition, testPosition.getTrades().size());
  }
  
  //------------------------------------------------------------------------
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_addAttribute_null_key() {
    PositionImpl testPosition = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertTrue(testPosition.getAttributes().isEmpty());
    testPosition.addAttribute(null, "B");
  }
  
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_addAttribute_null_value() {
    PositionImpl testPosition = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertTrue(testPosition.getAttributes().isEmpty());
    testPosition.addAttribute("A", null);
  }
  
  public void test_addAttribute() {
    PositionImpl testPosition = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertTrue(testPosition.getAttributes().isEmpty());
    testPosition.addAttribute("A", "B");
    assertEquals(1, testPosition.getAttributes().size());
    assertEquals("B", testPosition.getAttributes().get("A"));
    testPosition.addAttribute("C", "D");
    assertEquals(2, testPosition.getAttributes().size());
    assertEquals("D", testPosition.getAttributes().get("C"));
  }
  
  public void test_removeAttribute() {
    PositionImpl testPosition = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertTrue(testPosition.getAttributes().isEmpty());
    testPosition.addAttribute("A", "B");
    testPosition.addAttribute("C", "D");
    assertEquals(2, testPosition.getAttributes().size());
    testPosition.removeAttribute("A");
    assertEquals(1, testPosition.getAttributes().size());
    assertNull(testPosition.getAttributes().get("A"));
  }
  
  public void test_clearAttributes() {
    PositionImpl testPosition = new PositionImpl(UniqueId.of("B", "C"), BigDecimal.ONE, Identifier.of("A", "B"));
    assertTrue(testPosition.getAttributes().isEmpty());
    testPosition.addAttribute("A", "B");
    testPosition.addAttribute("C", "D");
    assertEquals(2, testPosition.getAttributes().size());
    testPosition.clearAttributes();
    assertTrue(testPosition.getAttributes().isEmpty());
  }

  private SecurityLink createLink(String scheme, String value) {
    return new SecurityLink(Identifier.of(scheme, value));
  }

}
