/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionBean;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.Security;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test ComputationTarget.
 */
public class ComputationTargetTest {

  private static final Portfolio PORTFOLIO = new PortfolioImpl(UniqueIdentifier.of("Test", "1"), "Name");
  private static final PortfolioNodeImpl NODE = new PortfolioNodeImpl(UniqueIdentifier.of("A", "B"), "Name");
  private static final Position POSITION = new PositionBean(UniqueIdentifier.of("Test", "1"), new BigDecimal(1), new IdentifierBundle());
  private static final DefaultSecurity SECURITY = new DefaultSecurity();
  static {
    SECURITY.setIdentityKey(Identifier.of("A", "B"));
  }

  @Test
  public void test_constructor_Object_Portfolio() {
    ComputationTarget test = new ComputationTarget(PORTFOLIO);
    assertEquals(ComputationTargetType.MULTIPLE_POSITIONS, test.getType());
    assertEquals(PORTFOLIO, test.getValue());
  }

  public void test_constructor_Object_null() {
    ComputationTarget test = new ComputationTarget(null);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(null, test.getValue());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor_Type_Object_ok() {
    new ComputationTarget(ComputationTargetType.MULTIPLE_POSITIONS, PORTFOLIO);
    new ComputationTarget(ComputationTargetType.MULTIPLE_POSITIONS, NODE);
    new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    new ComputationTarget(ComputationTargetType.SECURITY, Identifier.of(Security.SECURITY_IDENTITY_KEY_DOMAIN, "B"));
    new ComputationTarget(ComputationTargetType.PRIMITIVE, null);
    new ComputationTarget(ComputationTargetType.PRIMITIVE, "String");
  }

  @Test(expected=NullPointerException.class)
  public void test_constructor_Type_Object_nullType() {
    new ComputationTarget(null, POSITION);
  }

  @Test(expected=IllegalArgumentException.class)
  public void test_constructor_Type_Object_invalidObjectForType() {
    new ComputationTarget(ComputationTargetType.POSITION, NODE);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getters_PortfolioNode() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.MULTIPLE_POSITIONS, NODE);
    assertEquals(ComputationTargetType.MULTIPLE_POSITIONS, test.getType());
    assertEquals(NODE, test.getValue());
    assertEquals(NODE.getUniqueIdentifier(), test.getUniqueIdentifier());
    assertEquals(NODE.getUniqueIdentifier().getScheme(), test.getIdentityKey().getScheme().getName());
    assertEquals(NODE.getUniqueIdentifier().getValue(), test.getIdentityKey().getValue());
    assertEquals(NODE, test.getPortfolioNode());
  }

  @Test(expected=IllegalStateException.class)
  public void test_getPortfolioNode_notNode() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    test.getPortfolioNode();
  }

  @Test
  public void test_getters_Position() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    assertEquals(ComputationTargetType.POSITION, test.getType());
    assertEquals(POSITION, test.getValue());
    assertEquals(POSITION.getUniqueIdentifier(), test.getUniqueIdentifier());
    assertEquals(POSITION.getUniqueIdentifier().getScheme(), test.getIdentityKey().getScheme().getName());
    assertEquals(POSITION.getUniqueIdentifier().getValue(), test.getIdentityKey().getValue());
    assertEquals(POSITION, test.getPosition());
  }

  @Test(expected=IllegalStateException.class)
  public void test_getPosition_notPosition() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    test.getPosition();
  }

  @Test
  public void test_getters_Security() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    assertEquals(ComputationTargetType.SECURITY, test.getType());
    assertEquals(SECURITY, test.getValue());
    assertEquals(SECURITY.getIdentityKey().getScheme().getName(), test.getUniqueIdentifier().getScheme());
    assertEquals(SECURITY.getIdentityKey().getValue(), test.getUniqueIdentifier().getValue());
    assertEquals(SECURITY.getIdentityKey(), test.getIdentityKey());
    assertEquals(SECURITY, test.getSecurity());
  }

  @Test(expected=IllegalStateException.class)
  public void test_getSecurity_notSecurity() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    test.getSecurity();
  }

  @Test
  public void test_getters_Primitive() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.PRIMITIVE, "Str");
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals("Str", test.getValue());
    assertEquals(null, test.getUniqueIdentifier());
    assertEquals(null, test.getIdentityKey());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toSpecification() {
    ComputationTarget test = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    ComputationTargetSpecification expected = new ComputationTargetSpecification(ComputationTargetType.POSITION, POSITION.getUniqueIdentifier());
    assertEquals(expected, test.toSpecification());
  }

}
