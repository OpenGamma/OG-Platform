/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;

import com.opengamma.engine.position.MockPositionSource;
import com.opengamma.engine.position.Portfolio;
import com.opengamma.engine.position.PortfolioImpl;
import com.opengamma.engine.position.PortfolioNodeImpl;
import com.opengamma.engine.position.Position;
import com.opengamma.engine.position.PositionImpl;
import com.opengamma.engine.position.PositionSource;
import com.opengamma.engine.security.DefaultSecurity;
import com.opengamma.engine.security.MockSecuritySource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;

/**
 * Test DefaultComputationTargetResolver.
 */
public class DefaultComputationTargetResolverTest {

  private static final Portfolio PORTFOLIO = new PortfolioImpl(UniqueIdentifier.of("Test", "1"), "Name");
  private static final PortfolioNodeImpl NODE = new PortfolioNodeImpl(UniqueIdentifier.of("A", "B"), "Name");
  private static final Position POSITION = new PositionImpl(UniqueIdentifier.of("Test", "1"), new BigDecimal(1), new IdentifierBundle());
  private static final DefaultSecurity SECURITY = new DefaultSecurity(UniqueIdentifier.of("Test", "SEC"), "EQUITY", new IdentifierBundle());

  //-------------------------------------------------------------------------
  @Test
  public void test_constructor() {
    SecuritySource secMaster = new MockSecuritySource();
    PositionSource posMaster = new MockPositionSource();
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secMaster, posMaster);
    assertEquals(secMaster, test.getSecuritySource());
    assertEquals(posMaster, test.getPositionMaster());
  }

  @Test(expected=NullPointerException.class)
  public void test_constructor_nullSecuritySource() {
    SecuritySource secMaster = new MockSecuritySource();
    new DefaultComputationTargetResolver(secMaster, null);
  }

  @Test(expected=NullPointerException.class)
  public void test_constructor_nullPositionMaster() {
    PositionSource posMaster = new MockPositionSource();
    new DefaultComputationTargetResolver(null, posMaster);
  }

  //-------------------------------------------------------------------------
  @Test
  public void test_resolve_portfolio() {
    MockSecuritySource secMaster = new MockSecuritySource();
    MockPositionSource posMaster = new MockPositionSource();
    posMaster.addPortfolio(PORTFOLIO);
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secMaster, posMaster);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(PORTFOLIO);
    ComputationTarget expected = new ComputationTarget(PORTFOLIO);
    assertEquals(expected, test.resolve(spec));
  }

  @Test
  public void test_resolve_portfolioNode() {
    MockSecuritySource secMaster = new MockSecuritySource();
    MockPositionSource posMaster = new MockPositionSource();
    PortfolioImpl p = new PortfolioImpl(UniqueIdentifier.of("Test", "1"), "Name");
    p.getRootNode().addChildNode(NODE);
    posMaster.addPortfolio(p);
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secMaster, posMaster);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(NODE);
    ComputationTarget expected = new ComputationTarget(NODE);
    assertEquals(expected, test.resolve(spec));
  }

  @Test
  public void test_resolve_position() {
    MockSecuritySource secMaster = new MockSecuritySource();
    MockPositionSource posMaster = new MockPositionSource();
    PortfolioImpl p = new PortfolioImpl(UniqueIdentifier.of("Test", "1"), "Name");
    p.getRootNode().addPosition(POSITION);
    posMaster.addPortfolio(p);
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secMaster, posMaster);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(POSITION);
    ComputationTarget expected = new ComputationTarget(POSITION);
    assertEquals(expected, test.resolve(spec));
  }

  @Test
  public void test_resolve_security() {
    MockSecuritySource secMaster = new MockSecuritySource();
    MockPositionSource posMaster = new MockPositionSource();
    secMaster.addSecurity(SECURITY);
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secMaster, posMaster);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(SECURITY);
    ComputationTarget expected = new ComputationTarget(SECURITY);
    assertEquals(expected, test.resolve(spec));
  }

  @Test
  public void test_resolve_primitive() {
    MockSecuritySource secMaster = new MockSecuritySource();
    MockPositionSource posMaster = new MockPositionSource();
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secMaster, posMaster);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, (UniqueIdentifier) null);
    ComputationTarget expected = new ComputationTarget(ComputationTargetType.PRIMITIVE, null);
    assertEquals(expected, test.resolve(spec));
  }

  @Test(expected=NullPointerException.class)
  public void test_resolve_nullSpecification() {
    MockSecuritySource secMaster = new MockSecuritySource();
    MockPositionSource posMaster = new MockPositionSource();
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secMaster, posMaster);
    test.resolve(null);
  }

}
