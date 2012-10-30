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
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdentifiable;

/**
 * Test ComputationTargetSpecification.
 */
@Test
public class ComputationTargetSpecificationTest {

  private static final ExternalId ID = ExternalId.of("Test", "0");
  private static final UniqueId UID = UniqueId.of("Test", "1");
  private static final UniqueId UID2 = UniqueId.of("Test", "2");
  private static final ExternalIdentifiable IDENTIFIABLE = new ExternalIdentifiable() {
    @Override
    public ExternalId getExternalId() {
      return ExternalId.of("Test", "3");
    }
  };
  private static final UniqueIdentifiable UNIQUE_IDENTIFIABLE = new UniqueIdentifiable() {
    @Override
    public UniqueId getUniqueId() {
      return UniqueId.of("Test", "4");
    }
  };
  private static final Portfolio PORTFOLIO = new SimplePortfolio(UID, "Name");
  private static final SimplePortfolioNode NODE = new SimplePortfolioNode(UID, "Name");
  private static final Position POSITION = new SimplePosition(UID, new BigDecimal(1), ExternalIdBundle.EMPTY);
  private static final Security SECURITY = new SimpleSecurity(UID, ExternalIdBundle.EMPTY, "", "");

  public void test_constructor_Object_Portfolio() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(PORTFOLIO);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, test.getType());
    assertEquals(PORTFOLIO.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Node() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(NODE);
    assertEquals(ComputationTargetType.PORTFOLIO_NODE, test.getType());
    assertEquals(NODE.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Position() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(POSITION);
    assertEquals(ComputationTargetType.POSITION, test.getType());
    assertEquals(POSITION.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_Security() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(SECURITY);
    assertEquals(ComputationTargetType.SECURITY, test.getType());
    assertEquals(SECURITY.getUniqueId(), test.getUniqueId());
  }

  public void test_constructor_Object_null() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(null);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(null, test.getUniqueId());
  }
  
  public void test_constructor_Object_ID() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(ID);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(UniqueId.of("Test", "0"), test.getUniqueId());
  }

  public void test_constructor_Object_Identifiable() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(IDENTIFIABLE);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(UniqueId.of("Test", "3"), test.getUniqueId());
  }

  public void test_constructor_Object_UniqueIdentifiable() {
    ComputationTargetSpecification test = new ComputationTargetSpecification(UNIQUE_IDENTIFIABLE);
    assertEquals(ComputationTargetType.PRIMITIVE, test.getType());
    assertEquals(UNIQUE_IDENTIFIABLE.getUniqueId(), test.getUniqueId());
  }

  //-------------------------------------------------------------------------
  public void test_constructor_Type_UniqueId_ok() {
    new ComputationTargetSpecification(ComputationTargetType.PORTFOLIO_NODE, UID);
    new ComputationTargetSpecification(ComputationTargetType.POSITION, UID);
    new ComputationTargetSpecification(ComputationTargetType.SECURITY, UID);
    new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, (UniqueId) null);
  }

  @Test(expectedExceptions=IllegalArgumentException.class)
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
    assertEquals(UID.getScheme(), test.getIdentifier().getScheme().getName());
    assertEquals(UID.getValue(), test.getIdentifier().getValue());
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

}
