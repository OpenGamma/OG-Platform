/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertSame;
import static org.testng.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.Position;
import com.opengamma.core.position.PositionSource;
import com.opengamma.core.position.Trade;
import com.opengamma.core.security.Security;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.CachingComputationTargetResolver;
import com.opengamma.engine.ComputationTarget;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

/**
 * Tests the {@link CacheNotifyingSecuritySource} class.
 */
@Test
public class CacheNotifyingSecuritySourceTest {

  private static class TargetResolver implements CachingComputationTargetResolver {

    private final List<Security> _passed = new LinkedList<Security>();

    @Override
    public ComputationTarget resolve(final ComputationTargetSpecification specification) {
      throw new UnsupportedOperationException();
    }

    @Override
    public SecuritySource getSecuritySource() {
      throw new UnsupportedOperationException();
    }

    @Override
    public PositionSource getPositionSource() {
      throw new UnsupportedOperationException();
    }

    @Override
    public void cachePositions(final Collection<Position> positions) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void cacheSecurities(final Collection<Security> securities) {
      ArgumentChecker.notNull(securities, "securities");
      ArgumentChecker.isFalse(securities.isEmpty(), "securities.isEmpty");
      _passed.addAll(securities);
    }

    @Override
    public void cachePortfolioNodes(final Collection<PortfolioNode> portfolioNodes) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void cacheTrades(final Collection<Trade> trades) {
      throw new UnsupportedOperationException();
    }

  }

  public void testGetSecurity_byUniqueId() {
    final TargetResolver resolver = new TargetResolver();
    final CacheNotifyingSecuritySource ss = new CacheNotifyingSecuritySource(MockComputationTargetResolver.resolved().getSecuritySource(), resolver);
    Security security = ss.get(UniqueId.of("Security", "0"));
    assertNotNull(security);
    assertEquals(resolver._passed.size(), 1);
    assertSame(security, resolver._passed.get(0));
  }

  public void testGetSecurities_byUniqueId() {
    final TargetResolver resolver = new TargetResolver();
    final CacheNotifyingSecuritySource ss = new CacheNotifyingSecuritySource(MockComputationTargetResolver.resolved().getSecuritySource(), resolver);
    Map<UniqueId, Security> securities = ss.get(Arrays.asList(UniqueId.of("Invalid", "Identifier")));
    assertNotNull(securities);
    assertTrue(resolver._passed.isEmpty());
    securities = ss.get(Arrays.asList(UniqueId.of("Security", "0"), UniqueId.of("Security", "1")));
    assertNotNull(securities);
    assertEquals(resolver._passed.size(), 2);
    assertTrue(securities.values().contains(resolver._passed.get(0)));
    assertTrue(securities.values().contains(resolver._passed.get(1)));
  }

  public void testGetSecurity_byObjectId() {
    final TargetResolver resolver = new TargetResolver();
    final CacheNotifyingSecuritySource ss = new CacheNotifyingSecuritySource(MockComputationTargetResolver.resolved().getSecuritySource(), resolver);
    Security security = ss.get(ObjectId.of("Security", "0"), VersionCorrection.LATEST);
    assertNotNull(security);
    assertEquals(resolver._passed.size(), 1);
    assertSame(security, resolver._passed.get(0));
  }

  public void testGetSecurity_byExternalIdBundle() {
    final TargetResolver resolver = new TargetResolver();
    final CacheNotifyingSecuritySource ss = new CacheNotifyingSecuritySource(MockComputationTargetResolver.resolved().getSecuritySource(), resolver);
    Security security = ss.getSingle(ExternalIdBundle.of(ExternalId.of("Ticker", "0")));
    assertNotNull(security);
    assertEquals(resolver._passed.size(), 1);
    assertSame(security, resolver._passed.get(0));
  }

  public void testGetSecurities_byExternalIdBundle() {
    final TargetResolver resolver = new TargetResolver();
    final CacheNotifyingSecuritySource ss = new CacheNotifyingSecuritySource(MockComputationTargetResolver.resolved().getSecuritySource(), resolver);
    Collection<Security> securities = ss.get(ExternalIdBundle.of(ExternalId.of("Ticker", "0"), ExternalId.of("Ticker", "1")));
    assertNotNull(securities);
    assertEquals(resolver._passed.size(), 2);
    assertTrue(securities.contains(resolver._passed.get(0)));
    assertTrue(securities.contains(resolver._passed.get(1)));
  }

  public void testGetSecurity_byExternalIdBundleWithVC() {
    final TargetResolver resolver = new TargetResolver();
    final CacheNotifyingSecuritySource ss = new CacheNotifyingSecuritySource(MockComputationTargetResolver.resolved().getSecuritySource(), resolver);
    Security security = ss.getSingle(ExternalIdBundle.of(ExternalId.of("Ticker", "0")), VersionCorrection.LATEST) ;
    assertNotNull(security);
    assertEquals(resolver._passed.size(), 1);
    assertSame(security, resolver._passed.get(0));
  }

  public void testGetSecurities_byExternalIdBundleWithVC() {
    final TargetResolver resolver = new TargetResolver();
    final CacheNotifyingSecuritySource ss = new CacheNotifyingSecuritySource(MockComputationTargetResolver.resolved().getSecuritySource(), resolver);
    Collection<Security> securities = ss.get(ExternalIdBundle.of(ExternalId.of("Ticker", "0"), ExternalId.of("Ticker", "1")), VersionCorrection.LATEST);
    assertNotNull(securities);
    assertEquals(resolver._passed.size(), 2);
    assertTrue(securities.contains(resolver._passed.get(0)));
    assertTrue(securities.contains(resolver._passed.get(1)));
  }

}
