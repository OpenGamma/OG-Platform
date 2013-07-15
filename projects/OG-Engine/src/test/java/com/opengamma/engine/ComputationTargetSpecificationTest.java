/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.testng.Assert.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.core.position.Counterparty;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.engine.target.ComputationTargetReference;
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test ComputationTargetSpecification.
 */
@Test(groups = TestGroup.UNIT)
public class ComputationTargetSpecificationTest {

  private static final UniqueId UID = UniqueId.of("Test", "1");
  private static final UniqueId UID2 = UniqueId.of("Test", "2");
  private static final SimplePortfolioNode NODE = new SimplePortfolioNode(UID, "Name");
  private static final Security SECURITY = new SimpleSecurity(UID, ExternalIdBundle.EMPTY, "", "");
  private static final Position POSITION = new SimplePosition(UID, new BigDecimal(1), SECURITY);
  private static final Trade TRADE = createTrade();

  private static Trade createTrade() {
    final SimpleTrade trade = new SimpleTrade(SECURITY, BigDecimal.ONE, new SimpleCounterparty(ExternalId.of(Counterparty.DEFAULT_SCHEME, "Foo")), LocalDate.now(), null);
    trade.setUniqueId(UID);
    return trade;
  }

  public void test_constructor_Object_Node() {
    ComputationTargetSpecification test = ComputationTargetSpecification.of(NODE);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, test.getType());
    assertEquals(NODE.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Position() {
    ComputationTargetSpecification test = ComputationTargetSpecification.of(POSITION);
    assertEquals(ComputationTargetType.POSITION, test.getType());
    assertEquals(POSITION.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Security() {
    ComputationTargetSpecification test = ComputationTargetSpecification.of(SECURITY);
    assertEquals(ComputationTargetType.SECURITY, test.getType());
    assertEquals(SECURITY.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Trade() {
    ComputationTargetSpecification test = ComputationTargetSpecification.of(TRADE);
    assertEquals(ComputationTargetType.TRADE, test.getType());
    assertEquals(TRADE.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Currency() {
    ComputationTargetSpecification test = ComputationTargetSpecification.of(Currency.USD);
    assertEquals(ComputationTargetType.CURRENCY, test.getType());
    assertEquals(Currency.USD.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_UniqueId() {
    ComputationTargetSpecification test = ComputationTargetSpecification.of(UID);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(UID, test.getUniqueId());
  }

  public void test_constructor_Type_UniqueId_ok() {
    new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UID);
    new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    new ComputationTargetSpecification(ComputationTargetType.SECURITY, UID);
    new ComputationTargetSpecification(ComputationTargetType.NULL, null);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void test_constructor_Type_UniqueId_nullType_validId() {
    new ComputationTargetSpecification(ComputationTargetType.NULL, UniqueId.of("Foo", "Bar"));
  }

  @Test(expectedExceptions = AssertionError.class)
  public void test_constructor_Type_UniqueId_nullType() {
    new ComputationTargetSpecification(null, UID);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_constructor_Type_UniqueId_nullId() {
    new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, (UniqueId) null);
  }

  //-------------------------------------------------------------------------
  public void test_getters_PortfolioNode() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UID);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, test.getType());
    assertEquals(UID.getScheme(), test.getUniqueId().getScheme());
    assertEquals(UID.getValue(), test.getUniqueId().getValue());
  }

  //-------------------------------------------------------------------------
  public void test_toSpecification() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    assertEquals(true, test.toString().contains("POSITION"));
    assertEquals(true, test.toString().contains(UID.toString()));
  }

  //-------------------------------------------------------------------------
  public void test_equals_similar() {
    ComputationTargetSpecification a1 = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    ComputationTargetSpecification a2 = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  public void test_equals_different() {
    ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    ComputationTargetSpecification b = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID2);
    ComputationTargetSpecification c = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UID2);
    
    assertEquals(true, a.equals(a));
    assertEquals(false, a.equals(b));
    assertEquals(false, a.equals(c));
    
    assertEquals(false, b.equals(a));
    assertEquals(true, b.equals(b));
    assertEquals(false, b.equals(c));
    
    assertEquals(false, c.equals(a));
    assertEquals(false, c.equals(b));
    assertEquals(true, c.equals(c));
  }

  public void test_equals_other() {
    ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    assertEquals(false, a.equals(null));
    assertEquals(false, a.equals("Rubbish"));
  }

  public void test_hashCode() {
    ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    ComputationTargetSpecification b = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    assertEquals(true, a.equals(b));
  }

  public void test_isCompatible() {
    ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.of(SimpleSecurity.class), UID);
    ComputationTargetSpecification b = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UID);
    assertFalse(a.isCompatible(b));
    assertTrue(b.isCompatible(a));
  }

  @Test(expectedExceptions = {IllegalStateException.class })
  public void test_getRequirement() {
    final ComputationTargetReference a = ComputationTargetSpecification.of(SECURITY);
    a.getRequirement();
  }

  public void test_getSpecification() {
    final ComputationTargetReference a = ComputationTargetSpecification.of(SECURITY);
    assertEquals(a.getSpecification(), a);
  }

  public void testContaining_id() {
    final ComputationTargetReference ref = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar"));
    final ComputationTargetReference underlying = ref.containing(ComputationTargetType.SECURITY, ExternalId.of("Foo", "Underlying"));
    assertEquals(underlying.getParent(), ref);
    assertEquals(underlying.getType(), ComputationTargetType.SECURITY.containing(ComputationTargetType.SECURITY));
  }

  public void testContaining_uid() {
    final ComputationTargetReference ref = new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar"));
    final ComputationTargetReference underlying = ref.containing(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Underlying"));
    assertEquals(underlying.getParent(), ref);
    assertEquals(underlying.getType(), ComputationTargetType.SECURITY.containing(ComputationTargetType.SECURITY));
  }

}
