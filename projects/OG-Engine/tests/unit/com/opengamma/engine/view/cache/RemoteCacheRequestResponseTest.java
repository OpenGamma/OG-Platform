/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertNotNull;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.MutableFudgeFieldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.DirectFudgeConnection;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * 
 */
@Test
public class RemoteCacheRequestResponseTest {
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteCacheRequestResponseTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();

  @Test(timeOut = 10000l)
  public void singleThreadSpecLookupDifferentIdentifierValues() {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(s_fudgeContext);
    ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    DirectFudgeConnection conduit = new DirectFudgeConnection(cache.getFudgeContext());
    RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1());
    conduit.connectEnd2(server);
    IdentifierMap identifierMap = new RemoteIdentifierMap(client);

    final ValueSpecification[] valueSpec = new ValueSpecification[10];
    for (int i = 0; i < valueSpec.length; i++) {
      valueSpec[i] = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk", "Value" + i))),
          "mockFunctionId");
    }
    // Make single value calls
    s_logger.debug("Begin single value lookup");
    final BitSet seenIds = new BitSet();
    for (int i = 0; i < valueSpec.length; i++) {
      long id = identifierMap.getIdentifier(valueSpec[i]);
      assertTrue(id <= Integer.MAX_VALUE);
      assertFalse(seenIds.get((int) id));
      seenIds.set((int) id);
    }
    s_logger.debug("End single value lookup");
    // Make a bulk lookup call
    s_logger.debug("Begin bulk lookup");
    final Map<ValueSpecification, Long> identifiers = identifierMap.getIdentifiers(Arrays.asList(valueSpec));
    assertNotNull(identifiers);
    assertEquals(valueSpec.length, identifiers.size());
    for (ValueSpecification spec : valueSpec) {
      assertTrue(identifiers.containsKey(spec));
      assertTrue(seenIds.get((int) (long) identifiers.get(spec)));
    }
    s_logger.debug("End bulk lookup");
  }

  @Test(timeOut = 10000l)
  public void singleThreadLookupDifferentIdentifierValuesRepeated() {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(s_fudgeContext);
    ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    DirectFudgeConnection conduit = new DirectFudgeConnection(cache.getFudgeContext());
    conduit.connectEnd2(server);
    RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1());
    IdentifierMap identifierMap = new RemoteIdentifierMap(client);

    Map<String, Long> _idsByValueName = new HashMap<String, Long>();
    for (int i = 0; i < 10; i++) {
      String valueName = "Value" + i;
      ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk",
          valueName))), "mockFunctionId");
      long id = identifierMap.getIdentifier(valueSpec);
      _idsByValueName.put(valueName, id);
    }

    for (int i = 0; i < 10; i++) {
      String valueName = "Value" + i;
      ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk",
          valueName))), "mockFunctionId");
      long id = identifierMap.getIdentifier(valueSpec);
      assertEquals(_idsByValueName.get(valueName), new Long(id));
    }
  }

  @Test(timeOut = 30000l)
  public void multiThreadLookupDifferentIdentifierValuesRepeatedSharedClient() throws InterruptedException {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(s_fudgeContext);
    ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    DirectFudgeConnection conduit1 = new DirectFudgeConnection(cache.getFudgeContext());
    conduit1.connectEnd2(server);
    DirectFudgeConnection conduit2 = new DirectFudgeConnection(cache.getFudgeContext());
    conduit2.connectEnd2(server);
    final RemoteCacheClient client = new RemoteCacheClient(conduit1.getEnd1(), conduit2.getEnd1());
    final IdentifierMap identifierMap = new RemoteIdentifierMap(client);

    final ConcurrentMap<String, Long> _idsByValueName = new ConcurrentHashMap<String, Long>();
    final Random rand = new Random();
    final AtomicBoolean failed = new AtomicBoolean(false);
    List<Thread> threads = new ArrayList<Thread>();
    for (int i = 0; i < 10; i++) {
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            for (int j = 0; j < 1000; j++) {
              int randomValue = rand.nextInt(100);
              String valueName = "Value" + randomValue;
              ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk",
                  valueName))), "mockFunctionId");
              long id = identifierMap.getIdentifier(valueSpec);
              Long previousValue = _idsByValueName.putIfAbsent(valueName, id);
              if (previousValue != null) {
                assertEquals(previousValue, new Long(id));
              }
            }
          } catch (Exception e) {
            s_logger.error("Failed", e);
            failed.set(true);
          }
        }
      });
      threads.add(t);
    }
    for (Thread t : threads) {
      t.start();
    }
    for (Thread t : threads) {
      t.join();
    }
    assertFalse("One thread failed. Check logs.", failed.get());
  }

  @Test(timeOut = 30000l)
  public void multiThreadLookupDifferentIdentifierValuesRepeatedDifferentClient() throws InterruptedException {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(s_fudgeContext);
    final ViewComputationCacheServer server = new ViewComputationCacheServer(cache);

    final ConcurrentMap<String, Long> _idsByValueName = new ConcurrentHashMap<String, Long>();
    final Random rand = new Random();
    final AtomicBoolean failed = new AtomicBoolean(false);
    List<Thread> threads = new ArrayList<Thread>();
    for (int i = 0; i < 10; i++) {
      final DirectFudgeConnection conduit = new DirectFudgeConnection (cache.getFudgeContext ());
      conduit.connectEnd2 (server);
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          final RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1 ());
          final IdentifierMap identifierMap = new RemoteIdentifierMap(client);
          try {
            for (int j = 0; j < 1000; j++) {
              int randomValue = rand.nextInt(100);
              String valueName = "Value" + randomValue;
              ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk",
                  valueName))), "mockFunctionId");
              long id = identifierMap.getIdentifier(valueSpec);
              Long previousValue = _idsByValueName.putIfAbsent(valueName, id);
              if (previousValue != null) {
                assertEquals(previousValue, new Long(id));
              }
            }
          } catch (Exception e) {
            s_logger.error("Failed", e);
            failed.set(true);
          }
        }
      });
      threads.add(t);
    }
    for (Thread t : threads) {
      t.start();
    }
    for (Thread t : threads) {
      t.join();
    }
    assertFalse("One thread failed. Check logs.", failed.get());
  }

  // @Test(timeout=10000l)
  @Test
  public void singleThreadPutLoad() throws InterruptedException {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(s_fudgeContext);
    ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    DirectFudgeConnection conduit = new DirectFudgeConnection (cache.getFudgeContext ());
    conduit.connectEnd2  (server);
    RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1());
    final long timestamp = System.currentTimeMillis();
    FudgeMessageStore dataStore = new RemoteFudgeMessageStore(client, new ViewComputationCacheKey(UniqueIdentifier.of("Test", "ViewProcess1"), "Config1", timestamp));

    // Single value
    final MutableFudgeFieldContainer inputValue1 = s_fudgeContext.newMessage();
    for (int i = 0; i < 32; i++) {
      inputValue1.add(i, Integer.toString(i));
    }
    long identifier1 = 1L;
    dataStore.put(identifier1, inputValue1);

    FudgeFieldContainer outputValue = dataStore.get(identifier1);
    assertNotNull(outputValue);
    assertEquals(inputValue1.getAllFields(), outputValue.getAllFields());

    outputValue = dataStore.get(identifier1 + 1);
    assertNull(outputValue);

    outputValue = dataStore.get(identifier1);
    assertNotNull(outputValue);
    assertEquals(inputValue1.getAllFields(), outputValue.getAllFields());

    // Multiple value
    final MutableFudgeFieldContainer inputValue2 = s_fudgeContext.newMessage();
    for (int i = 32; i < 64; i++) {
      inputValue2.add(i, Integer.toString(i));
    }
    final Map<Long, FudgeFieldContainer> inputMap = new HashMap<Long, FudgeFieldContainer>();
    identifier1++;
    long identifier2 = identifier1 + 1;
    inputMap.put(identifier1, inputValue1);
    inputMap.put(identifier2, inputValue2);
    dataStore.put(inputMap);

    final Map<Long, FudgeFieldContainer> outputMap = dataStore.get(Arrays.asList(identifier1, identifier2));
    assertEquals(2, outputMap.size());
    assertEquals(inputValue1.getAllFields(), outputMap.get(identifier1).getAllFields());
    assertEquals(inputValue2.getAllFields(), outputMap.get(identifier2).getAllFields());
  }

  @Test(timeOut = 10000l)
  public void singleThreadPutLoadPurgeLoad() throws InterruptedException {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(s_fudgeContext);
    ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    DirectFudgeConnection conduit = new DirectFudgeConnection (cache.getFudgeContext ());
    conduit.connectEnd2 (server);
    RemoteCacheClient client = new RemoteCacheClient(conduit.getEnd1());
    final long timestamp = System.currentTimeMillis();
    FudgeMessageStore dataStore = new RemoteFudgeMessageStore(client, new ViewComputationCacheKey(UniqueIdentifier.of("Test", "ViewProcess1"), "Config1", timestamp));
    final MutableFudgeFieldContainer inputValue = s_fudgeContext.newMessage();
    for (int i = 0; i < 32; i++) {
      inputValue.add(i, Integer.toString(i));
    }
    final long identifier = 1L;
    dataStore.put(identifier, inputValue);

    FudgeFieldContainer outputValue = dataStore.get(identifier);
    assertNotNull(outputValue);
    assertEquals(inputValue.getAllFields(), outputValue.getAllFields());

    dataStore.delete();

    outputValue = dataStore.get(identifier);
    assertNull(outputValue);
  }

}
