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

import org.testng.annotations.Test;
import org.threeten.bp.OffsetDateTime;

import com.google.common.collect.Lists;
import com.opengamma.core.position.Counterparty;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecurityLink;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test {@link SimplePosition}.
 */
@Test(groups = TestGroup.UNIT)
public class SimplePositionTest {
  
  private static final Counterparty COUNTERPARTY = new SimpleCounterparty(ExternalId.of("CPARTY", "C100"));
  private static final OffsetDateTime TRADE_OFFSET_DATETIME = OffsetDateTime.now();

  public void test_construction_BigDecimal_ExternalId() {
    SimplePosition test = new SimplePosition(BigDecimal.ONE, ExternalId.of("A", "B"));
    assertEquals(null, test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getExternalId().size());
    assertEquals(ExternalId.of("A", "B"), test.getSecurityLink().getExternalId().iterator().next());
    assertEquals("Position[, 1 Bundle[A~B]]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_BigDecimal_ExternalId_nullBigDecimal() {
    new SimplePosition(null, ExternalId.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_BigDecimal_ExternalId_nullExternalId() {
    new SimplePosition(BigDecimal.ONE, (ExternalId) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_BigDecimal_ExternalIdBundle() {
    SimplePosition test = new SimplePosition(BigDecimal.ONE, ExternalIdBundle.of(ExternalId.of("A", "B")));
    assertEquals(null, test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getExternalId().size());
    assertEquals(ExternalId.of("A", "B"), test.getSecurityLink().getExternalId().iterator().next());
    assertEquals("Position[, 1 Bundle[A~B]]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_BigDecimal_ExternalIdBundle_nullBigDecimal() {
    new SimplePosition(null, ExternalIdBundle.of(ExternalId.of("A", "B")));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_BigDecimal_ExternalIdBundle_nullExternalId() {
    new SimplePosition(BigDecimal.ONE, (ExternalIdBundle) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_UniqueId_BigDecimal_ExternalId() {
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    assertEquals(UniqueId.of("B", "C"), test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getExternalId().size());
    assertEquals(ExternalId.of("A", "B"), test.getSecurityLink().getExternalId().iterator().next());
    assertEquals("Position[B~C, 1 Bundle[A~B]]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_ExternalId_nullUniqueId() {
    new SimplePosition(null, BigDecimal.ONE, ExternalId.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_ExternalId_nullBigDecimal() {
    new SimplePosition(UniqueId.of("B", "C"), null, ExternalId.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_ExternalId_nullExternalId() {
    new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, (ExternalId) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_UniqueId_BigDecimal_ExternalIdBundle() {
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalIdBundle.of(ExternalId.of("A", "B")));
    assertEquals(UniqueId.of("B", "C"), test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getExternalId().size());
    assertEquals(ExternalId.of("A", "B"), test.getSecurityLink().getExternalId().iterator().next());
    assertEquals("Position[B~C, 1 Bundle[A~B]]", test.toString());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_ExternalIdBundle_nullUniqueId() {
    new SimplePosition(null, BigDecimal.ONE, ExternalId.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_ExternalIdBundle_nullBigDecimal() {
    new SimplePosition(UniqueId.of("B", "C"), null, ExternalIdBundle.of(ExternalId.of("A", "B")));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_ExternalIdBundle_nullExternalIdBundle() {
    new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, (ExternalIdBundle) null);
  }

  //-------------------------------------------------------------------------
  public void test_construction_UniqueId_BigDecimal_Security() {
    SimpleSecurity sec = new SimpleSecurity("A");
    sec.setExternalIdBundle(ExternalIdBundle.of(ExternalId.of("A", "B")));
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, sec);
    assertEquals(UniqueId.of("B", "C"), test.getUniqueId());
    assertEquals(BigDecimal.ONE, test.getQuantity());
    assertEquals(1, test.getSecurityLink().getExternalId().size());
    assertEquals(ExternalId.of("A", "B"), test.getSecurityLink().getExternalId().iterator().next());
    assertEquals(true, test.toString().startsWith("Position[B~C, 1"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_Security_nullUniqueId() {
    new SimplePosition(null, BigDecimal.ONE, ExternalId.of("A", "B"));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_Security_nullBigDecimal() {
    new SimplePosition(UniqueId.of("B", "C"), null, ExternalIdBundle.of(ExternalId.of("A", "B")));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_construction_UniqueId_BigDecimal_Security_nullSecurity() {
    new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, (Security) null);
  }
  
  public void test_construction_copyFromPosition() {
    SimplePosition position = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    position.addAttribute("A", "B");
    position.addAttribute("C", "D");
    
    SimplePosition copy = new SimplePosition(position);
    assertEquals(copy, position);
  }

  //-------------------------------------------------------------------------
  public void test_setUniqueId() {
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    test.setUniqueId(UniqueId.of("B", "D"));
    assertEquals(UniqueId.of("B", "D"), test.getUniqueId());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setUniqueId_null() {
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    test.setUniqueId(null);
  }

  //-------------------------------------------------------------------------
  public void test_setQuantity() {
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    test.setQuantity(BigDecimal.ZERO);
    assertSame(BigDecimal.ZERO, test.getQuantity());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setQuantity_null() {
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    test.setQuantity(null);
  }

  //-------------------------------------------------------------------------
  public void test_setSecurityLink() {
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    test.setSecurityLink(new SimpleSecurityLink());
    assertEquals(new SimpleSecurityLink(), test.getSecurityLink());
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_setSecurityKey_null() {
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    test.setSecurityLink(null);
  }

  //-------------------------------------------------------------------------
  public void test_addTrade() {
    SimplePosition testPosition = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    assertTrue(testPosition.getTrades().isEmpty());
    SimpleTrade testTrade1 = new SimpleTrade(createLink("A", "B"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade1);
    
    SimpleTrade testTrade2 = new SimpleTrade(createLink("C", "D"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade2);
    
    assertEquals(2, testPosition.getTrades().size());
    assertTrue(testPosition.getTrades().containsAll(Lists.newArrayList(testTrade1, testTrade2)));
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_addTrade_null() {
    SimplePosition test = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    test.addTrade(null);
  }
  
  //-------------------------------------------------------------------------
  public void test_removeTrade() {
    SimplePosition testPosition = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    assertTrue(testPosition.getTrades().isEmpty());
    SimpleTrade testTrade1 = new SimpleTrade(createLink("A", "B"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade1);
    SimpleTrade testTrade2 = new SimpleTrade(createLink("C", "D"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    testPosition.addTrade(testTrade2);
    
    SimpleTrade testTrade3 = new SimpleTrade(createLink("E", "F"), BigDecimal.ONE, COUNTERPARTY, TRADE_OFFSET_DATETIME.toLocalDate(), TRADE_OFFSET_DATETIME.toOffsetTime());
    
    assertTrue(testPosition.removeTrade(testTrade1));
    assertTrue(testPosition.removeTrade(testTrade2));
    assertFalse(testPosition.removeTrade(testTrade3));
    assertTrue(testPosition.getTrades().isEmpty());
  }

  //------------------------------------------------------------------------
  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_addAttribute_null_key() {
    SimplePosition testPosition = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    assertTrue(testPosition.getAttributes().isEmpty());
    testPosition.addAttribute(null, "B");
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_addAttribute_null_value() {
    SimplePosition testPosition = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    assertTrue(testPosition.getAttributes().isEmpty());
    testPosition.addAttribute("A", null);
  }

  public void test_addAttribute() {
    SimplePosition testPosition = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    assertTrue(testPosition.getAttributes().isEmpty());
    testPosition.addAttribute("A", "B");
    assertEquals(1, testPosition.getAttributes().size());
    assertEquals("B", testPosition.getAttributes().get("A"));
    testPosition.addAttribute("C", "D");
    assertEquals(2, testPosition.getAttributes().size());
    assertEquals("D", testPosition.getAttributes().get("C"));
  }

  public void test_removeAttribute() {
    SimplePosition testPosition = new SimplePosition(UniqueId.of("B", "C"), BigDecimal.ONE, ExternalId.of("A", "B"));
    assertTrue(testPosition.getAttributes().isEmpty());
    testPosition.addAttribute("A", "B");
    testPosition.addAttribute("C", "D");
    assertEquals(2, testPosition.getAttributes().size());
    testPosition.removeAttribute("A");
    assertEquals(1, testPosition.getAttributes().size());
    assertNull(testPosition.getAttributes().get("A"));
  }

  private SecurityLink createLink(String scheme, String value) {
    return new SimpleSecurityLink(ExternalId.of(scheme, value));
  }

}
