/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.target.lazy;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
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
import com.opengamma.core.position.Portfolio;
import com.opengamma.core.position.impl.SimplePortfolio;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.target.MockComputationTargetResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LazyResolvedPortfolio} class
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class LazyResolvedPortfolioTest {

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
    final Portfolio underlying = resolver.getPositionSource().getPortfolio(UniqueId.of("Portfolio", "0"), VersionCorrection.LATEST);
    final Portfolio portfolio = new LazyResolvedPortfolio(new LazyResolveContext(resolver.getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), underlying);
    assertEquals(portfolio.getAttributes(), underlying.getAttributes());
    portfolio.setAttributes(ImmutableMap.of("K1", "V1", "K2", "V2"));
    assertEquals(portfolio.getAttributes(), underlying.getAttributes());
    portfolio.addAttribute("K3", "V3");
    assertEquals(portfolio.getAttributes().size(), 3);
    assertEquals(underlying.getAttributes().size(), 3);
    assertEquals(portfolio.getName(), underlying.getName());
    assertEquals(portfolio.getUniqueId(), underlying.getUniqueId());
    assertEquals(portfolio.getRootNode().getUniqueId(), underlying.getRootNode().getUniqueId());
  }

  public void testSerialization_full() throws Exception {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.resolved();
    final Portfolio underlying = resolver.getPositionSource().getPortfolio(UniqueId.of("Portfolio", "0"), VersionCorrection.LATEST);
    Portfolio portfolio = new LazyResolvedPortfolio(new LazyResolveContext(resolver.getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), underlying);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(portfolio);
    final Object resultObject = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    assertTrue(resultObject instanceof SimplePortfolio);
    portfolio = (Portfolio) resultObject;
    assertEquals(portfolio.getUniqueId(), underlying.getUniqueId());
    assertEquals(portfolio.getAttributes(), underlying.getAttributes());
    portfolio.setAttributes(ImmutableMap.of("K1", "V1", "K2", "V2"));
    assertNotEquals(portfolio.getAttributes(), underlying.getAttributes());
    portfolio.addAttribute("K3", "V3");
    assertNotEquals(portfolio.getAttributes(), underlying.getAttributes());
    assertEquals(portfolio.getAttributes().size(), 3);
    assertEquals(portfolio.getName(), underlying.getName());
    assertEquals(portfolio.getRootNode().getUniqueId(), underlying.getRootNode().getUniqueId());
    assertEquals(portfolio.getRootNode().getChildNodes().size(), underlying.getRootNode().getChildNodes().size());
  }

  public void testSerialization_targetResolver() throws Exception {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.resolved();
    final Portfolio underlying = resolver.getPositionSource().getPortfolio(UniqueId.of("Portfolio", "0"), VersionCorrection.LATEST);
    Portfolio portfolio = new LazyResolvedPortfolio(
        new LazyResolveContext(resolver.getSecuritySource(), new DefaultCachingComputationTargetResolver(resolver, _cacheManager)).atVersionCorrection(VersionCorrection.LATEST),
        underlying);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(portfolio);
    final Object resultObject = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    assertTrue(resultObject instanceof TargetResolverPortfolio);
    portfolio = (Portfolio) resultObject;
    assertEquals(portfolio.getUniqueId(), underlying.getUniqueId());
    assertEquals(portfolio.getAttributes(), underlying.getAttributes());
    portfolio.setAttributes(ImmutableMap.of("K1", "V1", "K2", "V2"));
    assertNotEquals(portfolio.getAttributes(), underlying.getAttributes());
    portfolio.addAttribute("K3", "V3");
    assertNotEquals(portfolio.getAttributes(), underlying.getAttributes());
    assertEquals(portfolio.getAttributes().size(), 3);
    assertEquals(portfolio.getName(), underlying.getName());
    assertEquals(portfolio.getRootNode().getUniqueId(), underlying.getRootNode().getUniqueId());
    assertEquals(portfolio.getRootNode().getChildNodes().size(), underlying.getRootNode().getChildNodes().size());
  }

}
