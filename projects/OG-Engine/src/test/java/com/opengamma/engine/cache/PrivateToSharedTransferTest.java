/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.cache;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.sf.ehcache.CacheManager;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.cache.DefaultViewComputationCacheSource.MissingValueLoader;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueProperties;
import com.opengamma.engine.value.ValuePropertyNames;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.DirectFudgeConnection;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.test.Timeout;
import com.opengamma.util.tuple.Pair;
import com.opengamma.util.tuple.Pairs;

@Test(groups = {TestGroup.INTEGRATION, "ehcache"})
public class PrivateToSharedTransferTest {

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
  private static ValueSpecification[] createValueSpecifications(final int count) {
    final ValueSpecification[] specs = new ValueSpecification[count];
    final ComputationTargetSpecification target = ComputationTargetSpecification.of(Currency.USD);
    for (int i = 0; i < specs.length; i++) {
      specs[i] = new ValueSpecification(Integer.toString(i), target, ValueProperties.with(ValuePropertyNames.FUNCTION, "Bar").get());
    }
    return specs;
  }

  private static FudgeMessageStoreFactory createInMemoryFudgeMessageStoreFactory(final FudgeContext fudgeContext) {
    return new DefaultFudgeMessageStoreFactory(new InMemoryBinaryDataStoreFactory(), fudgeContext);
  }

  @Test
  public void testMissingValueLoader_noCallback() {
    final IdentifierMap identifiers = new InMemoryIdentifierMap();
    final DefaultViewComputationCacheSource source = new DefaultViewComputationCacheSource(identifiers,
        FudgeContext.GLOBAL_DEFAULT, createInMemoryFudgeMessageStoreFactory(FudgeContext.GLOBAL_DEFAULT));
    final ViewComputationCache cache = source.getCache(UniqueId.of("Test", "ViewCycle"), "Default");
    final ValueSpecification[] specs = createValueSpecifications(4);
    cache.putPrivateValue(new ComputedValue(specs[0], "Zero"));
    cache.putSharedValue(new ComputedValue(specs[1], "One"));
    Object value = cache.getValue(specs[0]);
    assertEquals("Zero", value);
    value = cache.getValue(specs[1]);
    assertEquals("One", value);
    value = cache.getValue(specs[2]);
    assertNull(value);
    Collection<Pair<ValueSpecification, Object>> values = cache.getValues(Arrays.asList(specs[2], specs[3]));
    assertNotNull(values);
    assertTrue(values.isEmpty());
  }

  @Test
  public void testMissingValueLoader_withCallback() {
    final IdentifierMap identifiers = new InMemoryIdentifierMap();
    final Set<Integer> missing = new HashSet<Integer>();
    final DefaultViewComputationCacheSource source = new DefaultViewComputationCacheSource(identifiers,
        FudgeContext.GLOBAL_DEFAULT, createInMemoryFudgeMessageStoreFactory(FudgeContext.GLOBAL_DEFAULT));
    final UniqueId viewCycleId = UniqueId.of("Test", "ViewCycle");
    source.setMissingValueLoader(new MissingValueLoader() {

      @Override
      public FudgeMsg findMissingValue(final ViewComputationCacheKey cache, final long identifier) {
        assertEquals(viewCycleId, cache.getViewCycleId());
        assertEquals("Default", cache.getCalculationConfigurationName());
        final ValueSpecification spec = identifiers.getValueSpecification(identifier);
        int i = Integer.parseInt(spec.getValueName());
        missing.add(i);
        switch (i) {
          case 0:
            return FudgeContext.GLOBAL_DEFAULT.toFudgeMsg("Zero").getMessage();
          case 1:
            return FudgeContext.GLOBAL_DEFAULT.toFudgeMsg("One").getMessage();
          case 2:
            return FudgeContext.GLOBAL_DEFAULT.toFudgeMsg("Two").getMessage();
          case 3:
            return FudgeContext.GLOBAL_DEFAULT.toFudgeMsg("Three").getMessage();
        }
        return null;
      }

      @Override
      public Map<Long, FudgeMsg> findMissingValues(final ViewComputationCacheKey cache,
          final Collection<Long> identifiers) {
        final Map<Long, FudgeMsg> map = new HashMap<Long, FudgeMsg>();
        for (Long identifier : identifiers) {
          map.put(identifier, findMissingValue(cache, identifier));
        }
        return map;
      }

    });
    final ViewComputationCache cache = source.getCache(viewCycleId, "Default");
    final ValueSpecification[] specs = createValueSpecifications(4);
    cache.putPrivateValue(new ComputedValue(specs[0], "Zero"));
    cache.putSharedValue(new ComputedValue(specs[1], "One"));
    Object value = cache.getValue(specs[0]);
    assertEquals("Zero", value);
    value = cache.getValue(specs[1]);
    assertEquals("One", value);
    value = cache.getValue(specs[2]);
    assertEquals("Two", value);
    assertEquals(1, missing.size());
    assertTrue(missing.contains(2));
    missing.clear();
    Collection<Pair<ValueSpecification, Object>> values = cache.getValues(Arrays.asList(specs[2], specs[3]));
    assertEquals(2, values.size());
    assertTrue(values.contains(Pairs.of(specs[2], "Two")));
    assertTrue(values.contains(Pairs.of(specs[3], "Three")));
    assertEquals(2, missing.size());
    assertTrue(missing.contains(2));
    assertTrue(missing.contains(3));
    missing.clear();
    values = cache.getValues(Arrays.asList(specs[0], specs[1], specs[2]));
    assertEquals(3, values.size());
    assertTrue(values.contains(Pairs.of(specs[0], "Zero")));
    assertTrue(values.contains(Pairs.of(specs[1], "One")));
    assertTrue(values.contains(Pairs.of(specs[2], "Two")));
    assertEquals(1, missing.size());
    assertTrue(missing.contains(2));
  }

  @Test
  public void testFindMessage() throws InterruptedException {
    // Create the test infrastructure
    final FudgeContext fudgeContext = OpenGammaFudgeContext.getInstance();
    final DefaultViewComputationCacheSource serverCacheSource = new DefaultViewComputationCacheSource(
        new InMemoryIdentifierMap(), fudgeContext, createInMemoryFudgeMessageStoreFactory(fudgeContext));
    final ViewComputationCacheServer server = new ViewComputationCacheServer(serverCacheSource);
    server.getBinaryDataStore().setFindValueTimeout(Timeout.standardTimeoutMillis() * 5);
    final DirectFudgeConnection conduit1 = new DirectFudgeConnection(fudgeContext);
    conduit1.connectEnd1(server);
    final DirectFudgeConnection conduit2 = new DirectFudgeConnection(fudgeContext);
    conduit2.connectEnd1(server);
    final DirectFudgeConnection conduit3 = new DirectFudgeConnection(fudgeContext);
    conduit3.connectEnd1(server);
    final RemoteViewComputationCacheSource remoteCacheSource1 = new RemoteViewComputationCacheSource(
        new RemoteCacheClient(conduit1.getEnd2()), createInMemoryFudgeMessageStoreFactory(fudgeContext), _cacheManager);
    final RemoteViewComputationCacheSource remoteCacheSource2 = new RemoteViewComputationCacheSource(
        new RemoteCacheClient(conduit2.getEnd2()), createInMemoryFudgeMessageStoreFactory(fudgeContext), _cacheManager);
    final RemoteViewComputationCacheSource remoteCacheSource3 = new RemoteViewComputationCacheSource(
        new RemoteCacheClient(conduit3.getEnd2()), createInMemoryFudgeMessageStoreFactory(fudgeContext), _cacheManager);
    // Populate the test caches
    final ValueSpecification[] specs = createValueSpecifications(10);
    UniqueId viewCycleId = UniqueId.of("Test", "ViewCycle");
    final ViewComputationCache serverCache = serverCacheSource.getCache(viewCycleId, "Default");
    final ViewComputationCache remoteCache1 = remoteCacheSource1.getCache(viewCycleId, "Default");
    final ViewComputationCache remoteCache2 = remoteCacheSource2.getCache(viewCycleId, "Default");
    final ViewComputationCache remoteCache3 = remoteCacheSource3.getCache(viewCycleId, "Default");
    serverCache.putSharedValue(new ComputedValue(specs[0], "Zero"));
    serverCache.putPrivateValue(new ComputedValue(specs[1], "One"));
    remoteCache1.putPrivateValue(new ComputedValue(specs[2], "Two"));
    remoteCache1.putPrivateValue(new ComputedValue(specs[3], "Three"));
    remoteCache1.putPrivateValue(new ComputedValue(specs[4], "Four"));
    remoteCache2.putPrivateValue(new ComputedValue(specs[3], "Three"));
    remoteCache2.putPrivateValue(new ComputedValue(specs[5], "Five"));
    remoteCache2.putPrivateValue(new ComputedValue(specs[6], "Six"));
    remoteCache3.putPrivateValue(new ComputedValue(specs[7], "Seven"));
    // Direct lookup on server
    Object value = serverCache.getValue(specs[0]);
    assertEquals("Zero", value);
    value = serverCache.getValue(specs[1]);
    assertEquals("One", value);
    // Only identifier lookup messages expected
    Thread.sleep(Timeout.standardTimeoutMillis());
    assertEquals(3, conduit1.getAndResetMessages1To2());
    assertEquals(3, conduit1.getAndResetMessages2To1());
    assertEquals(3, conduit2.getAndResetMessages1To2());
    assertEquals(3, conduit2.getAndResetMessages2To1());
    assertEquals(1, conduit3.getAndResetMessages1To2());
    assertEquals(1, conduit3.getAndResetMessages2To1());
    // Query for a value elsewhere
    value = serverCache.getValue(specs[2]);
    assertEquals("Two", value);
    // One message sent to each, 1 response (and ack) from client1
    Thread.sleep(Timeout.standardTimeoutMillis());
    assertEquals(2, conduit1.getAndResetMessages1To2());
    assertEquals(1, conduit1.getAndResetMessages2To1());
    assertEquals(1, conduit2.getAndResetMessages1To2());
    assertEquals(0, conduit2.getAndResetMessages2To1());
    assertEquals(1, conduit3.getAndResetMessages1To2());
    assertEquals(0, conduit3.getAndResetMessages2To1());
    // Query for a value duplicated on multiple nodes
    value = serverCache.getValue(specs[3]);
    assertEquals("Three", value);
    // One message sent to each, 1 response (and ack) from client1 and client2
    Thread.sleep(Timeout.standardTimeoutMillis());
    assertRange(1, 2, conduit1.getAndResetMessages1To2());
    assertRange(0, 1, conduit1.getAndResetMessages2To1());
    assertRange(1, 2, conduit2.getAndResetMessages1To2());
    assertRange(0, 1, conduit2.getAndResetMessages2To1());
    assertEquals(1, conduit3.getAndResetMessages1To2());
    assertEquals(0, conduit3.getAndResetMessages2To1());
    // Query for a set of values on two different nodes
    Collection<Pair<ValueSpecification, Object>> values = serverCache.getValues(Arrays.asList(specs[4], specs[5],
        specs[6]));
    assertEquals(3, values.size());
    assertTrue(values.contains(Pairs.of(specs[4], "Four")));
    assertTrue(values.contains(Pairs.of(specs[5], "Five")));
    assertTrue(values.contains(Pairs.of(specs[6], "Six")));
    // One message sent to each, 1 response (and ack) from client1 and 2
    Thread.sleep(Timeout.standardTimeoutMillis());
    assertEquals(2, conduit1.getAndResetMessages1To2());
    assertEquals(1, conduit1.getAndResetMessages2To1());
    assertEquals(2, conduit2.getAndResetMessages1To2());
    assertEquals(1, conduit2.getAndResetMessages2To1());
    assertEquals(1, conduit3.getAndResetMessages1To2());
    assertEquals(0, conduit3.getAndResetMessages2To1());
    // Query for a non-existent single value
    value = serverCache.getValue(specs[8]);
    assertNull(value);
    assertEquals(1, conduit3.getAndResetMessages1To2());
    assertEquals(0, conduit3.getAndResetMessages2To1());
    // Query for one existing and one non-existent value
    values = serverCache.getValues(Arrays.asList(specs[7], specs[8]));
    assertEquals(1, values.size());
    assertTrue(values.contains(Pairs.of(specs[7], "Seven")));
    assertEquals(2, conduit3.getAndResetMessages1To2());
    assertEquals(1, conduit3.getAndResetMessages2To1());
    // Query for two non-existing values
    values = serverCache.getValues(Arrays.asList(specs[8], specs[9]));
    assertNotNull(values);
    assertTrue(values.isEmpty());
    assertEquals(1, conduit3.getAndResetMessages1To2());
    assertEquals(0, conduit3.getAndResetMessages2To1());
  }

  private void assertRange(final int loInclusive, final int hiInclusive, final int value) {
    assertTrue("value " + value + " < " + loInclusive, loInclusive <= value);
    assertTrue("value " + value + " > " + hiInclusive, value <= hiInclusive);
  }

}
