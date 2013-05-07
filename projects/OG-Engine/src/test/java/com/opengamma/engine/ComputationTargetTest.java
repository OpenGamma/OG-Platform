/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;
import org.threeten.bp.OffsetTime;

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test ComputationTarget.
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetTest {

  private final SimplePortfolioNode NODE = new SimplePortfolioNode(UniqueId.of("A", "B"), "Name");
  private final Position POSITION = new SimplePosition(UniqueId.of("Test", "1"), new BigDecimal(1), ExternalId.of("Foo", "Sec").toBundle());
  private final Security SECURITY = new SimpleSecurity(UniqueId.of("Test", "SEC"), ExternalId.of("Foo", "Sec").toBundle(), "EQUITY", "Test Security");
  private final SimpleTrade TRADE = new SimpleTrade(SECURITY, BigDecimal.ONE, new SimpleCounterparty(ExternalId.of("Cpty", "Foo")), LocalDate.now(), OffsetTime.now());

  public ComputationTargetTest() {
    TRADE.setUniqueId(UniqueId.of("Test", "Trade"));
  }

  public void testConstructor_null() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.NULL, null);
    assertNull(target.getContextSpecification());
    assertEquals(target.toSpecification(), ComputationTargetSpecification.NULL);
  }

  public void testConstructor_single() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, NODE);
    assertNull(target.getContextSpecification());
    assertEquals(target.toSpecification(), ComputationTargetSpecification.of(NODE));
  }

  public void testConstructor_nested_1() {
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(NODE).containing(ComputationTargetType.POSITION, POSITION.getUniqueId());
    final ComputationTarget target = new ComputationTarget(targetSpec, POSITION);
    assertEquals(target.toSpecification(), ComputationTargetSpecification.of(NODE).containing(ComputationTargetType.POSITION, POSITION.getUniqueId()));
    assertEquals(target.getContextSpecification(), ComputationTargetSpecification.of(NODE));
    assertEquals(target.getLeafSpecification(), ComputationTargetSpecification.of(POSITION));
  }

  public void testConstructor_nested_2() {
    final ComputationTargetSpecification targetSpec = ComputationTargetSpecification.of(NODE).containing(ComputationTargetType.POSITION, POSITION.getUniqueId())
        .containing(ComputationTargetType.SECURITY, SECURITY.getUniqueId());
    final ComputationTarget target = new ComputationTarget(targetSpec, SECURITY);
    assertEquals(target.toSpecification(), targetSpec);
    assertEquals(target.getContextSpecification(), ComputationTargetSpecification.of(NODE).containing(ComputationTargetType.POSITION, POSITION.getUniqueId()));
    assertEquals(target.getLeafSpecification(), ComputationTargetSpecification.of(SECURITY));
  }

  public void testGetPortfolioNode_ok() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, NODE);
    assertEquals(target.getPortfolioNode(), NODE);
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testGetPortfolioNode_fail() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.NULL, null);
    target.getPortfolioNode();
  }

  public void testGetPosition_ok() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    assertEquals(target.getPosition(), POSITION);
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testGetPosition_fail() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.NULL, null);
    target.getPosition();
  }

  public void testGetTrade_ok() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.TRADE, TRADE);
    target.getTrade();
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testGetTrade_fail() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.NULL, null);
    target.getTrade();
  }

  public void testGetPositionOrTrade_ok() {
    ComputationTarget target = new ComputationTarget(ComputationTargetType.TRADE, TRADE);
    assertEquals(target.getPositionOrTrade(), TRADE);
    target = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    assertEquals(target.getPositionOrTrade(), POSITION);
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testGetPositionOrTrade_fail() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.NULL, null);
    target.getPositionOrTrade();
  }

  public void testGetSecurity_ok() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    target.getSecurity();
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testGetSecurity_fail() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.NULL, null);
    target.getSecurity();
  }

  public void testGetName() {
    ComputationTarget target = new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, NODE);
    assertEquals(target.getName(), "Name");
    target = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    assertEquals(target.getName(), "1 x Foo~Sec");
    target = new ComputationTarget(ComputationTargetType.TRADE, TRADE);
    assertEquals(target.getName(), "Test Security");
    target = new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    assertEquals(target.getName(), "Test Security");
    target = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Currency", "GBP"));
    assertEquals(target.getName(), "Currency~GBP");
    target = new ComputationTarget(ComputationTargetType.NULL, null);
    assertEquals(target.getName(), null);
  }

  public void testEquals() {
    final ComputationTarget pos1 = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    final ComputationTarget pos2 = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    final ComputationTarget prim1 = new ComputationTarget(ComputationTargetType.CURRENCY, Currency.USD);
    final ComputationTarget prim2 = new ComputationTarget(ComputationTargetType.CURRENCY, Currency.GBP);
    final ComputationTarget prtPos = new ComputationTarget(ComputationTargetSpecification.of(NODE).containing(ComputationTargetType.POSITION, POSITION.getUniqueId()), POSITION);
    final ComputationTarget nil = new ComputationTarget(ComputationTargetType.NULL, null);
    assertTrue(pos1.equals(pos2));
    assertTrue(pos2.equals(pos1));
    assertFalse(pos1.equals(prim1));
    assertFalse(prim1.equals(pos1));
    assertTrue(prim1.equals(prim1));
    assertFalse(prim1.equals(prim2));
    assertFalse(pos1.equals(prtPos));
    assertFalse(prtPos.equals(pos1));
    assertFalse(pos1.equals(ComputationTarget.NULL));
    assertFalse(ComputationTarget.NULL.equals(pos1));
    assertFalse(pos1.equals(null));
    assertFalse(ComputationTarget.NULL.equals(null));
    assertTrue(nil.equals(ComputationTarget.NULL));
    assertTrue(ComputationTarget.NULL.equals(nil));
  }

  public void testHashCode() {
    final ComputationTarget pos1 = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    final ComputationTarget pos2 = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    assertEquals(pos1.hashCode(), pos2.hashCode());
    final ComputationTarget prtPos1 = new ComputationTarget(ComputationTargetSpecification.of(NODE).containing(ComputationTargetType.POSITION, POSITION.getUniqueId()), POSITION);
    final ComputationTarget prtPos2 = new ComputationTarget(ComputationTargetSpecification.of(NODE).containing(ComputationTargetType.POSITION, POSITION.getUniqueId()), POSITION);
    assertEquals(prtPos1.hashCode(), prtPos2.hashCode());
    final ComputationTarget nil1 = new ComputationTarget(ComputationTargetType.NULL, null);
    assertEquals(nil1.hashCode(), ComputationTarget.NULL.hashCode());
  }

  public void testGetValue_ok() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.CURRENCY, Currency.GBP);
    assertEquals(target.getValue(), Currency.GBP);
    final Currency c = target.getValue(ComputationTargetType.CURRENCY);
    assertEquals(c, Currency.GBP);
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void testGetValue_fail() {
    final ComputationTarget target = new ComputationTarget(ComputationTargetType.CURRENCY, Currency.GBP);
    target.getValue(ComputationTargetType.UNORDERED_CURRENCY_PAIR);
  }

}
