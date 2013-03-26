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

import com.opengamma.core.position.PortfolioNode;
import com.opengamma.core.position.impl.SimplePortfolioNode;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.target.MockComputationTargetResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LazyResolvedPortfolioNode} class
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class LazyResolvedPortfolioNodeTest {

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
    final PortfolioNode underlying = resolver.getPositionSource().getPortfolioNode(UniqueId.of("Node", "0"), VersionCorrection.LATEST);
    final PortfolioNode node = new LazyResolvedPortfolioNode(new LazyResolveContext(resolver.getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), underlying);
    assertEquals(node.getName(), underlying.getName());
    assertEquals(node.getParentNodeId(), underlying.getParentNodeId());
    assertEquals(node.getUniqueId(), underlying.getUniqueId());
    assertEquals(node.getChildNodes().size(), underlying.getChildNodes().size());
    assertEquals(node.getPositions().size(), underlying.getPositions().size());
  }

  public void testSerialization_full() throws Exception {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.resolved();
    final PortfolioNode underlying = resolver.getPositionSource().getPortfolioNode(UniqueId.of("Node", "0"), VersionCorrection.LATEST);
    PortfolioNode node = new LazyResolvedPortfolioNode(new LazyResolveContext(resolver.getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), underlying);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(node);
    final Object resultObject = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    assertTrue(resultObject instanceof SimplePortfolioNode);
    node = (PortfolioNode) resultObject;
    assertEquals(node.getName(), underlying.getName());
    assertEquals(node.getChildNodes().size(), underlying.getChildNodes().size());
    assertEquals(node.getPositions().size(), underlying.getPositions().size());
  }

  public void testSerialization_targetResolver() throws Exception {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.resolved();
    final PortfolioNode underlying = resolver.getPositionSource().getPortfolioNode(UniqueId.of("Node", "0"), VersionCorrection.LATEST);
    PortfolioNode node = new LazyResolvedPortfolioNode(new LazyResolveContext(resolver.getSecuritySource(), new DefaultCachingComputationTargetResolver(resolver,
        _cacheManager)).atVersionCorrection(VersionCorrection.LATEST), underlying);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(node);
    final Object resultObject = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    assertTrue(resultObject instanceof TargetResolverPortfolioNode);
    node = (PortfolioNode) resultObject;
    assertEquals(node.getName(), underlying.getName());
    assertEquals(node.getChildNodes().size(), underlying.getChildNodes().size());
    assertEquals(node.getPositions().size(), underlying.getPositions().size());
  }

}
