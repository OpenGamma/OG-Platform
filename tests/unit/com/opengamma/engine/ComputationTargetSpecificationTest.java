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
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test ComputationTargetSpecification.
 */
public class ComputationTargetSpecificationTest {

  private static final UniqueIdentifier UID = UniqueIdentifier.of("Test", "1");
  private static final Identifier ID = Identifier.of("A", "B");
  private static final Portfolio PORTFOLIO = new PortfolioImpl(UID, "Name");
  private static final PortfolioNodeImpl NODE = new PortfolioNodeImpl(UID, "Name");
  private static final Position POSITION = new PositionBean(UID, new BigDecimal(1), new IdentifierBundle());
  private static final DefaultSecurity SECURITY = new DefaultSecurity();
  static {
    SECURITY.setIdentityKey(ID);
  }

  @Test
  public void test_constructor_Object_Portfolio() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(PORTFOLIO);
    assertEquals(ComputationTargetType.MULTIPLE_POSITIONS, test.getType());
    assertEquals(PORTFOLIO.getUniqueIdentifier().getScheme(), test.getIdentifier().getScheme().getName());
    assertEquals(PORTFOLIO.getUniqueIdentifier().getValue(), test.getIdentifier().getValue());
  }

  @Test
  public void test_constructor_Object_Node() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(NODE);
    assertEquals(ComputationTargetType.MULTIPLE_POSITIONS, test.getType());
    assertEquals(NODE.getUniqueIdentifier().getScheme(), test.getIdentifier().getScheme().getName());
    assertEquals(NODE.getUniqueIdentifier().getValue(), test.getIdentifier().getValue());
  }

  @Test
  public void test_constructor_Object_Position() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(POSITION);
    assertEquals(ComputationTargetType.POSITION, test.getType());
    assertEquals(POSITION.getUniqueIdentifier().getScheme(), test.getIdentifier().getScheme().getName());
    assertEquals(POSITION.getUniqueIdentifier().getValue(), test.getIdentifier().getValue());
  }

  @Test
  public void test_constructor_Object_Security() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(SECURITY);
    assertEquals(ComputationTargetType.SECURITY, test.getType());
    assertEquals(SECURITY.getIdentityKey(), test.getIdentifier());
  }

  @Test
  public void test_constructor_Object_null() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(null);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(null, test.getIdentifier());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor_Type_UniqueIdentifier_ok() {
    new ComputationTargetSpecification(ComputationTargetType.MULTIPLE_POSITIONS, UID);
    new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    new ComputationTargetSpecification(ComputationTargetType.SECURITY, UID);
    new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, (UniqueIdentifier) null);
    new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, (Identifier) null);
  }

  @Test(expected=NullPointerException.class)
  public void test_constructor_Type_UniqueIdentifier_nullType() {
    new ComputationTargetSpecification(null, UID);
  }

  @Test(expected=NullPointerException.class)
  public void test_constructor_Type_UniqueIdentifier_nullId() {
    new ComputationTargetSpecification(ComputationTargetType.MULTIPLE_POSITIONS, (UniqueIdentifier) null);
    new ComputationTargetSpecification(ComputationTargetType.MULTIPLE_POSITIONS, (Identifier) null);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_getters_PortfolioNode() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(ComputationTargetType.MULTIPLE_POSITIONS, UID);
    assertEquals(ComputationTargetType.MULTIPLE_POSITIONS, test.getType());
    assertEquals(UID.getScheme(), test.getIdentifier().getScheme().getName());
    assertEquals(UID.getValue(), test.getIdentifier().getValue());
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_equals_similar() {
    ComputationTargetSpecification a1 = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    ComputationTargetSpecification a2 = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    
    assertEquals(true, a1.equals(a1));
    assertEquals(true, a1.equals(a2));
    
    assertEquals(true, a2.equals(a1));
    assertEquals(true, a2.equals(a2));
  }

  @Test
  public void test_equals_different() {
    ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    ComputationTargetSpecification b = new ComputationTargetSpecification(ComputationTargetType.POSITION, ID);
    ComputationTargetSpecification c = new ComputationTargetSpecification(ComputationTargetType.SECURITY, ID);
    
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

  @Test
  public void test_hashCode() {
    ComputationTargetSpecification a = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    ComputationTargetSpecification b = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    assertEquals(true, a.equals(b));
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_toSpecification() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    assertEquals(true, test.toString().contains("POSITION"));
    assertEquals(true, test.toString().contains(UID.toString()));
  }

}
