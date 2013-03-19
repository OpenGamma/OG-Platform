/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import net.sf.ehcache.CacheManager;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.google.common.collect.ImmutableMap;
import com.opengamma.core.position.Trade;
import com.opengamma.core.position.impl.SimpleTrade;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.target.MockComputationTargetResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LazyResolvedTrade} class
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class LazyResolvedTradeTest {

  private CacheManager _cacheManager;

  @BeforeClass
  public void setUpClass() {
    _cacheManager = EHCacheUtils.createTestCacheManager(getClass());
  }

  @AfterClass
  public void tearDownClass() {
    EHCacheUtils.shutdownQuiet(_cacheManager);
  }

  @BeforeMethod
  public void setUp() {
    EHCacheUtils.clear(_cacheManager);
  }

  //-------------------------------------------------------------------------
  public void testBasicMethods() {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.resolved();
    final Trade underlying = resolver.getPositionSource().getTrade(UniqueId.of("Trade", "0"));
    Trade trade = new LazyResolvedTrade(new LazyResolveContext(resolver.getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), underlying);
    assertEquals(trade.getAttributes(), underlying.getAttributes());
    trade.setAttributes(ImmutableMap.of("K1", "V1"));
    assertEquals(trade.getAttributes(), underlying.getAttributes());
    trade.addAttribute("K2", "V2");
    assertEquals(trade.getAttributes().size(), 2);
    assertEquals(trade.getCounterparty(), underlying.getCounterparty());
    assertEquals(trade.getPremium(), underlying.getPremium());
    assertEquals(trade.getPremiumCurrency(), underlying.getPremiumCurrency());
    assertEquals(trade.getPremiumDate(), underlying.getPremiumDate());
    assertEquals(trade.getPremiumTime(), underlying.getPremiumTime());
    assertEquals(trade.getQuantity(), underlying.getQuantity());
    assertEquals(trade.getSecurity().getUniqueId(), underlying.getSecurity().getUniqueId());
  }

  public void testSerialization_full() throws Exception {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.resolved();
    final Trade underlying = resolver.getPositionSource().getTrade(UniqueId.of("Trade", "0"));
    underlying.setAttributes(ImmutableMap.of("K1", "V1"));
    Trade trade = new LazyResolvedTrade(new LazyResolveContext(resolver.getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), underlying);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(trade);
    final Object result = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    assertTrue(result instanceof SimpleTrade);
    trade = (Trade) result;
    assertEquals(trade.getAttributes(), underlying.getAttributes());
    trade.addAttribute("K2", "V2");
    assertEquals(trade.getAttributes().size(), 2);
    assertEquals(trade.getCounterparty(), underlying.getCounterparty());
    assertEquals(trade.getPremium(), underlying.getPremium());
    assertEquals(trade.getPremiumCurrency(), underlying.getPremiumCurrency());
    assertEquals(trade.getPremiumDate(), underlying.getPremiumDate());
    assertEquals(trade.getPremiumTime(), underlying.getPremiumTime());
    assertEquals(trade.getQuantity(), underlying.getQuantity());
    assertEquals(trade.getSecurity().getUniqueId(), underlying.getSecurity().getUniqueId());
  }

  public void testSerialization_targetResolver() throws Exception {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.resolved();
    final Trade underlying = resolver.getPositionSource().getTrade(UniqueId.of("Trade", "0"));
    underlying.setAttributes(ImmutableMap.of("K1", "V1"));
    Trade trade = new LazyResolvedTrade(new LazyResolveContext(resolver.getSecuritySource(), new DefaultCachingComputationTargetResolver(resolver,
        _cacheManager)).atVersionCorrection(VersionCorrection.LATEST), underlying);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(trade);
    final Object result = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    assertTrue(result instanceof TargetResolverTrade);
    trade = (Trade) result;
    assertEquals(trade.getAttributes(), underlying.getAttributes());
    trade.addAttribute("K2", "V2");
    assertEquals(trade.getAttributes().size(), 2);
    assertEquals(trade.getCounterparty(), underlying.getCounterparty());
    assertEquals(trade.getPremium(), underlying.getPremium());
    assertEquals(trade.getPremiumCurrency(), underlying.getPremiumCurrency());
    assertEquals(trade.getPremiumDate(), underlying.getPremiumDate());
    assertEquals(trade.getPremiumTime(), underlying.getPremiumTime());
    assertEquals(trade.getQuantity(), underlying.getQuantity());
    assertEquals(trade.getSecurity().getUniqueId(), underlying.getSecurity().getUniqueId());
  }

}
