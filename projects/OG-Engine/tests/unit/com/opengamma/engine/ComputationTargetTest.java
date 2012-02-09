/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Test ComputationTarget.
 */
@Test
public class ComputationTargetTest {

  private static final Portfolio PORTFOLIO = new SimplePortfolio(UniqueId.of("Test", "1"), "Name");
  private static final SimplePortfolioNode NODE = new SimplePortfolioNode(UniqueId.of("A", "B"), "Name");
  private static final Position POSITION = new SimplePosition(UniqueId.of("Test", "1"), new BigDecimal(1), ExternalIdBundle.EMPTY);
  private static final Security SECURITY = new SimpleSecurity(UniqueId.of("Test", "SEC"), ExternalIdBundle.EMPTY, "Test security", "EQUITY");

  public void test_constructor_Object_Portfolio() {
    ComputationTarget test = new ComputationTarget(PORTFOLIO);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, test.getType());
    assertEquals(PORTFOLIO, test.getValue());
  }

  public void test_constructor_Object_null() {
    ComputationTarget test = new ComputationTarget(null);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(null, test.getValue());
  }

  //-------------------------------------------------------------------------
  public void test_constructor_Type_Object_ok() {
    new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, PORTFOLIO);
    new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, NODE);
    new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    new ComputationTarget(ComputationTargetType.PRIMITIVE, null);
    new ComputationTarget(ComputationTargetType.PRIMITIVE, "String");
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_constructor_Type_Object_nullType() {
    new ComputationTarget(null, POSITION);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
  public void test_constructor_Type_Object_invalidObjectForType() {
    new ComputationTarget(ComputationTargetType.POSITION, NODE);
  }

  //-------------------------------------------------------------------------
  public void test_getters_PortfolioNode() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, NODE);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, test.getType());
    assertEquals(NODE, test.getValue());
    assertEquals(NODE.getUniqueId(), test.getUniqueId());
    assertEquals(NODE, test.getPortfolioNode());
  }

  @Test(expectedExceptions=IllegalStateException.class)
  public void test_getPortfolioNode_notNode() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    test.getPortfolioNode();
  }

  public void test_getters_Position() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    assertEquals(ComputationTargetType.POSITION, test.getType());
    assertEquals(POSITION, test.getValue());
    assertEquals(POSITION.getUniqueId(), test.getUniqueId());
    assertEquals(POSITION, test.getPosition());
  }

  @Test(expectedExceptions=IllegalStateException.class)
  public void test_getPosition_notPosition() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    test.getPosition();
  }

  public void test_getters_Security() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    assertEquals(ComputationTargetType.SECURITY, test.getType());
    assertEquals(SECURITY, test.getValue());
    assertEquals(SECURITY.getUniqueId(), test.getUniqueId());
    assertEquals(SECURITY, test.getSecurity());
  }

  @Test(expectedExceptions=IllegalStateException.class)
  public void test_getSecurity_notSecurity() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    test.getSecurity();
  }

  public void test_getters_Primitive() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.PRIMITIVE, "Str");
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals("Str", test.getValue());
    assertEquals(null, test.getUniqueId());
  }

  //-------------------------------------------------------------------------
  public void test_toSpecification() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    ComputationTargetSpecification expected = new ComputationTargetSpecification(ComputationTargetType.POSITION, POSITION.getUniqueId());
    assertEquals(expected, test.toSpecification());
  }

  //-------------------------------------------------------------------------
  public void test_equals_similar() {
    ComputationTarget a1 = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    ComputationTarget a2 = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  public void test_equals_different() {
    ComputationTarget a = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    ComputationTarget b = new ComputationTarget(ComputationTargetType.PRIMITIVE, null);
    ComputationTarget c = new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    
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
    ComputationTarget a = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    assertEquals(false, a.equals(null));
    assertEquals(false, a.equals("Rubbish"));
  }

  public void test_hashCode() {
    ComputationTarget a = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    ComputationTarget b = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    assertEquals(true, a.equals(b));
  }

}
