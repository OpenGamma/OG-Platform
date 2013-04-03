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

import com.opengamma.core.position.Position;
import com.opengamma.core.position.impl.SimplePosition;
import com.opengamma.engine.DefaultCachingComputationTargetResolver;
import com.opengamma.engine.target.MockComputationTargetResolver;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.test.TestGroup;

/**
 * Tests the {@link LazyResolvedPosition} class
 */
@Test(groups = {TestGroup.UNIT, "ehcache" })
public class LazyResolvedPositionTest {

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
    final Position underlying = resolver.getPositionSource().getPosition(UniqueId.of("Position", "0"));
    final Position position = new LazyResolvedPosition(new LazyResolveContext(resolver.getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), underlying);
    assertEquals(position.getAttributes(), underlying.getAttributes());
    assertEquals(position.getQuantity(), underlying.getQuantity());
    assertEquals(position.getTrades().size(), underlying.getTrades().size());
    assertEquals(position.getUniqueId(), underlying.getUniqueId());
  }

  public void testSerialization_full() throws Exception {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.resolved();
    final Position underlying = resolver.getPositionSource().getPosition(UniqueId.of("Position", "0"));
    Position position = new LazyResolvedPosition(new LazyResolveContext(resolver.getSecuritySource(), null).atVersionCorrection(VersionCorrection.LATEST), underlying);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(position);
    final Object resultObject = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    assertTrue(resultObject instanceof SimplePosition);
    position = (Position) resultObject;
    assertEquals(position.getAttributes(), underlying.getAttributes());
    assertEquals(position.getQuantity(), underlying.getQuantity());
    assertEquals(position.getTrades().size(), underlying.getTrades().size());
    assertEquals(position.getUniqueId(), underlying.getUniqueId());
  }

  public void testSerialization_targetResolver() throws Exception {
    final MockComputationTargetResolver resolver = MockComputationTargetResolver.resolved();
    final Position underlying = resolver.getPositionSource().getPosition(UniqueId.of("Position", "0"));
    Position position = new LazyResolvedPosition(new LazyResolveContext(resolver.getSecuritySource(), new DefaultCachingComputationTargetResolver(resolver,
        _cacheManager)).atVersionCorrection(VersionCorrection.LATEST), underlying);
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    new ObjectOutputStream(baos).writeObject(position);
    final Object resultObject = new ObjectInputStream(new ByteArrayInputStream(baos.toByteArray())).readObject();
    assertTrue(resultObject instanceof TargetResolverPosition);
    position = (Position) resultObject;
    assertEquals(position.getAttributes(), underlying.getAttributes());
    assertEquals(position.getQuantity(), underlying.getQuantity());
    assertEquals(position.getTrades().size(), underlying.getTrades().size());
    assertEquals(position.getUniqueId(), underlying.getUniqueId());
  }

}
