/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.testng.AssertJUnit.assertEquals;

import java.math.BigDecimal;

import javax.time.calendar.OffsetDateTime;

import org.testng.annotations.Test;

import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.impl.MockPositionSource;
import com.opengamma.core.position.impl.SimpleCounterparty;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.core.security.impl.SimpleSecurity;
import com.opengamma.core.security.impl.SimpleSecurityLink;
import com.opengamma.engine.test.MockSecuritySource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;

/**
 * Test DefaultComputationTargetResolver.
 */
@Test
public class DefaultComputationTargetResolverTest {

  private static final Portfolio PORTFOLIO = new SimplePortfolio(UniqueId.of("Test", "1"), "Name");
  private static final SimplePortfolioNode NODE = new SimplePortfolioNode(UniqueId.of("A", "B"), "Name");
  private static final Position POSITION = new SimplePosition(UniqueId.of("Test", "1"), new BigDecimal(1), ExternalIdBundle.EMPTY);
  private static final Security SECURITY = new SimpleSecurity(UniqueId.of("Test", "SEC"), ExternalIdBundle.EMPTY, "Test security", "EQUITY");

  //-------------------------------------------------------------------------
  public void test_constructor() {
    SecuritySource secSource = new MockSecuritySource();
    PositionSource posSource = new MockPositionSource();
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    assertEquals(secSource, test.getSecuritySource());
    assertEquals(posSource, test.getPositionSource());
  }

  //-------------------------------------------------------------------------
  public void test_resolve_portfolio() {
    MockSecuritySource secSource = new MockSecuritySource();
    MockPositionSource posSource = new MockPositionSource();
    posSource.addPortfolio(PORTFOLIO);
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(PORTFOLIO);
    ComputationTarget expected = new ComputationTarget(PORTFOLIO);
    assertEquals(expected, test.resolve(spec));
  }

  public void test_resolve_portfolioNode() {
    MockSecuritySource secSource = new MockSecuritySource();
    MockPositionSource posSource = new MockPositionSource();
    SimplePortfolio p = new SimplePortfolio(UniqueId.of("Test", "1"), "Name");
    p.getRootNode().addChildNode(NODE);
    posSource.addPortfolio(p);
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(NODE);
    ComputationTarget expected = new ComputationTarget(NODE);
    assertEquals(expected, test.resolve(spec));
  }

  public void test_resolve_position() {
    MockSecuritySource secSource = new MockSecuritySource();
    MockPositionSource posSource = new MockPositionSource();
    SimplePortfolio p = new SimplePortfolio(UniqueId.of("Test", "1"), "Name");
    p.getRootNode().addPosition(POSITION);
    posSource.addPortfolio(p);
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(POSITION);
    ComputationTarget expected = new ComputationTarget(POSITION);
    assertEquals(expected, test.resolve(spec));
  }
  
  public void test_resolve_trade() {
    OffsetDateTime now = OffsetDateTime.now();
    MockSecuritySource secSource = new MockSecuritySource();
    MockPositionSource posSource = new MockPositionSource();
    SimplePortfolio portfolio = new SimplePortfolio(UniqueId.of("Test", "1"), "Name");
    SimplePosition position = new SimplePosition(UniqueId.of("Test", "1"), new BigDecimal(1), ExternalIdBundle.EMPTY);
    SimpleTrade trade = new SimpleTrade(position.getUniqueId(), new SimpleSecurityLink(), new BigDecimal(1), new SimpleCounterparty(ExternalId.of("CPARTY", "C100")), now.toLocalDate(), now.toOffsetTime());
    trade.setUniqueId(UniqueId.of("TradeScheme", "1"));
    position.addTrade(trade);
    portfolio.getRootNode().addPosition(position);
    posSource.addPortfolio(portfolio);
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(trade);
    ComputationTarget expected = new ComputationTarget(trade);
    assertEquals(expected, test.resolve(spec));
  }
  
  public void test_resolve_security() {
    MockSecuritySource secSource = new MockSecuritySource();
    MockPositionSource posSource = new MockPositionSource();
    secSource.addSecurity(SECURITY);
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(SECURITY);
    ComputationTarget expected = new ComputationTarget(SECURITY);
    assertEquals(expected, test.resolve(spec));
  }

  public void test_resolve_primitive() {
    MockSecuritySource secSource = new MockSecuritySource();
    MockPositionSource posSource = new MockPositionSource();
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, (UniqueId) null);
    ComputationTarget expected = new ComputationTarget(ComputationTargetType.PRIMITIVE, null);
    assertEquals(expected, test.resolve(spec));
  }

  @Test(expectedExceptions=NullPointerException.class)
  public void test_resolve_nullSpecification() {
    MockSecuritySource secSource = new MockSecuritySource();
    MockPositionSource posSource = new MockPositionSource();
    DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    test.resolve(null);
  }

}
