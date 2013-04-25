/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.math.BigDecimal;

import org.testng.annotations.Test;
import org.threeten.bp.OffsetDateTime;

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
import com.opengamma.engine.target.ComputationTargetType;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.money.Currency;
import com.opengamma.util.money.UnorderedCurrencyPair;
import com.opengamma.util.test.TestGroup;

/**
 * Test DefaultComputationTargetResolver.
 */
@Test(groups = TestGroup.UNIT)
public class DefaultComputationTargetResolverTest {

  private static final SimplePortfolioNode NODE = new SimplePortfolioNode(UniqueId.of("A", "B"), "Name");
  private static final Position POSITION = new SimplePosition(UniqueId.of("Test", "1"), new BigDecimal(1), ExternalIdBundle.EMPTY);
  private static final Security SECURITY = new SimpleSecurity(UniqueId.of("Test", "SEC"), ExternalIdBundle.EMPTY, "Test security", "EQUITY");

  public void test_constructor() {
    final SecuritySource secSource = new InMemorySecuritySource();
    final PositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    assertSame(secSource, test.getSecuritySource());
    assertSame(posSource, test.getPositionSource());
  }

  private void assertExpected(final ComputationTarget expected, final ComputationTarget actual) {
    assertTrue(expected.getType().isCompatible(actual.getType()));
    assertEquals(expected.getContextSpecification(), actual.getContextSpecification());
    assertEquals(expected.getUniqueId(), actual.getUniqueId());
  }

  public void test_resolve_portfolioNode() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final SimplePortfolio p = new SimplePortfolio(UniqueId.of("Test", "1"), "Name");
    p.getRootNode().addChildNode(NODE);
    posSource.addPortfolio(p);
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    final ComputationTargetSpecification spec = ComputationTargetSpecification.of(NODE);
    final ComputationTarget expected = new ComputationTarget(ComputationTargetType.PORTFOLIO_NODE, NODE);
    assertExpected(expected, test.resolve(spec, VersionCorrection.LATEST));
  }

  public void test_resolve_position() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final SimplePortfolio p = new SimplePortfolio(UniqueId.of("Test", "1"), "Name");
    p.getRootNode().addPosition(POSITION);
    posSource.addPortfolio(p);
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    final ComputationTargetSpecification spec = ComputationTargetSpecification.of(POSITION);
    final ComputationTarget expected = new ComputationTarget(ComputationTargetType.POSITION, POSITION);
    assertExpected(expected, test.resolve(spec, VersionCorrection.LATEST));
  }

  public void test_resolve_trade() {
    final OffsetDateTime now = OffsetDateTime.now();
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final SimplePortfolio portfolio = new SimplePortfolio(UniqueId.of("Test", "1"), "Name");
    final SimplePosition position = new SimplePosition(UniqueId.of("Test", "1"), new BigDecimal(1), ExternalIdBundle.EMPTY);
    final SimpleTrade trade = new SimpleTrade(new SimpleSecurityLink(), new BigDecimal(1), new SimpleCounterparty(ExternalId.of("CPARTY", "C100")), now.toLocalDate(), now.toOffsetTime());
    trade.setUniqueId(UniqueId.of("TradeScheme", "1"));
    position.addTrade(trade);
    portfolio.getRootNode().addPosition(position);
    posSource.addPortfolio(portfolio);
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    final ComputationTargetSpecification spec = ComputationTargetSpecification.of(trade);
    final ComputationTarget expected = new ComputationTarget(ComputationTargetType.TRADE, trade);
    assertExpected(expected, test.resolve(spec, VersionCorrection.LATEST));
  }

  public void test_resolve_security() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    secSource.addSecurity(SECURITY);
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    final ComputationTargetSpecification spec = ComputationTargetSpecification.of(SECURITY);
    final ComputationTarget expected = new ComputationTarget(ComputationTargetType.SECURITY, SECURITY);
    assertExpected(expected, test.resolve(spec, VersionCorrection.LATEST));
  }

  public void test_resolve_primitive() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    final ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueId.of("Foo", "Bar"));
    final ComputationTarget expected = new ComputationTarget(ComputationTargetType.PRIMITIVE, UniqueId.of("Foo", "Bar"));
    assertExpected(expected, test.resolve(spec, VersionCorrection.LATEST));
  }

  public void test_resolve_currency() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    final ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.CURRENCY, UniqueId.of("CurrencyISO", "USD"));
    final ComputationTarget expected = new ComputationTarget(ComputationTargetType.CURRENCY, Currency.USD);
    assertExpected(expected, test.resolve(spec, VersionCorrection.LATEST));
  }

  public void test_resolve_unorderedCurrencyPair() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    final ComputationTargetSpecification spec = new ComputationTargetSpecification(ComputationTargetType.UNORDERED_CURRENCY_PAIR, UniqueId.of("UnorderedCurrencyPair", "EURGBP"));
    final ComputationTarget expected = new ComputationTarget(ComputationTargetType.UNORDERED_CURRENCY_PAIR, UnorderedCurrencyPair.of(Currency.GBP, Currency.EUR));
    assertExpected(expected, test.resolve(spec, VersionCorrection.LATEST));
  }

  public void test_resolve_null() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    assertExpected(ComputationTarget.NULL, test.resolve(ComputationTargetSpecification.NULL, VersionCorrection.LATEST));
  }

  @Test(expectedExceptions = NullPointerException.class)
  public void test_resolve_nullSpecification() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    test.resolve(null, VersionCorrection.LATEST);
  }

  public void test_resolve_notFound() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    assertNull(test.resolve(new ComputationTargetSpecification(ComputationTargetType.SECURITY, UniqueId.of("Foo", "Bar")), VersionCorrection.LATEST));
  }

  public void test_simplifyType() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    // No changes
    assertEquals(test.simplifyType(ComputationTargetType.NULL), ComputationTargetType.NULL);
    assertEquals(test.simplifyType(ComputationTargetType.SECURITY), ComputationTargetType.SECURITY);
    assertEquals(test.simplifyType(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION).containing(ComputationTargetType.SECURITY)),
        ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION).containing(ComputationTargetType.SECURITY));
    assertEquals(test.simplifyType(ComputationTargetType.TRADE.or(ComputationTargetType.SECURITY)), ComputationTargetType.TRADE.or(ComputationTargetType.SECURITY));
    // Changes
    assertEquals(test.simplifyType(ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION).containing(ComputationTargetType.of(SimpleSecurity.class))),
        ComputationTargetType.PORTFOLIO_NODE.containing(ComputationTargetType.POSITION).containing(ComputationTargetType.SECURITY));
    assertEquals(test.simplifyType(ComputationTargetType.TRADE.or(ComputationTargetType.of(SimpleSecurity.class))), ComputationTargetType.TRADE.or(ComputationTargetType.SECURITY));
  }

  public void test_typeProvider_getSimple() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    assertEquals(test.getSimpleTypes().size(), 9);
  }

  public void test_typeProvider_getAdditional() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    assertEquals(test.getAdditionalTypes().size(), 0);
  }

  public void test_typeProvider_getAll() {
    final InMemorySecuritySource secSource = new InMemorySecuritySource();
    final MockPositionSource posSource = new MockPositionSource();
    final DefaultComputationTargetResolver test = new DefaultComputationTargetResolver(secSource, posSource);
    assertEquals(test.getAllTypes().size(), test.getAdditionalTypes().size() + test.getSimpleTypes().size());
  }

}
