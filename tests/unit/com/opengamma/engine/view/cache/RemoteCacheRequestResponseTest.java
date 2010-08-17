/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fudgemsg.FudgeContext;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.InMemoryRequestConduit;

/**
 * 
 *
 * @author kirk
 */
public class RemoteCacheRequestResponseTest {
  private static final Logger s_logger = LoggerFactory.getLogger(RemoteCacheRequestResponseTest.class);
  
  @Test(timeout=10000l)
  public void singleThreadSpecLookupDifferentIdentifierValues() {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource (FudgeContext.GLOBAL_DEFAULT);
    ViewComputationCacheServer server = new ViewComputationCacheServer (cache);
    FudgeRequestSender conduit = InMemoryRequestConduit.create(server);
    RemoteCacheClient client = new RemoteCacheClient(conduit);
    IdentifierMap identifierMap = new RemoteIdentifierMap (client);
    
    BitSet seenIds = new BitSet();
    for(int i = 0; i < 10; i++) {
      ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk", "Value" + i))));
      long id = identifierMap.getIdentifier(valueSpec);
      assertTrue(id <= Integer.MAX_VALUE);
      assertFalse(seenIds.get((int) id));
      seenIds.set((int) id);
    }
  }
  
  @Test(timeout=10000l)
  public void singleThreadLookupDifferentIdentifierValuesRepeated() {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource (FudgeContext.GLOBAL_DEFAULT);
    ViewComputationCacheServer server = new ViewComputationCacheServer (cache);
    FudgeRequestSender conduit = InMemoryRequestConduit.create(server);
    RemoteCacheClient client = new RemoteCacheClient(conduit);
    IdentifierMap identifierMap = new RemoteIdentifierMap (client);

    Map<String, Long> _idsByValueName = new HashMap<String, Long>();
    for(int i = 0; i < 10; i++) {
      String valueName = "Value" + i;
      ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk", valueName))));
      long id = identifierMap.getIdentifier(valueSpec);
      _idsByValueName.put(valueName, id);
    }

    for(int i = 0; i < 10; i++) {
      String valueName = "Value" + i;
      ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk", valueName))));
      long id = identifierMap.getIdentifier(valueSpec);
      assertEquals(_idsByValueName.get(valueName), new Long(id));
    }
  }
  
  @Test(timeout=30000l)
  public void multiThreadLookupDifferentIdentifierValuesRepeatedSharedClient() throws InterruptedException {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource (FudgeContext.GLOBAL_DEFAULT);
    ViewComputationCacheServer server = new ViewComputationCacheServer (cache);
    FudgeRequestSender conduit = InMemoryRequestConduit.create(server);
    final RemoteCacheClient client = new RemoteCacheClient(conduit);
    final IdentifierMap identifierMap = new RemoteIdentifierMap (client);

    final ConcurrentMap<String, Long> _idsByValueName = new ConcurrentHashMap<String, Long>();
    final Random rand = new Random();
    final AtomicBoolean failed = new AtomicBoolean(false);
    List<Thread> threads = new ArrayList<Thread>();
    for(int i = 0; i < 10; i++) {
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            for(int j = 0; j < 1000; j++) {
              int randomValue = rand.nextInt(100);
              String valueName = "Value" + randomValue;
              ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value", new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk", valueName))));
              long id = identifierMap.getIdentifier(valueSpec);
              Long previousValue = _idsByValueName.putIfAbsent(valueName, id);
              if(previousValue != null) {
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
    for(Thread t : threads) {
      t.start();
    }
    for(Thread t : threads) {
      t.join();
    }
    assertFalse("One thread failed. Check logs.", failed.get());
  }

  @Test(timeout=30000l)
  public void multiThreadLookupDifferentIdentifierValuesRepeatedDifferentClient() throws InterruptedException {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource (FudgeContext.GLOBAL_DEFAULT);
    final ViewComputationCacheServer server = new ViewComputationCacheServer (cache);
    final FudgeRequestSender conduit = InMemoryRequestConduit.create(server);

    final ConcurrentMap<String, Long> _idsByValueName = new ConcurrentHashMap<String, Long>();
    final Random rand = new Random();
    final AtomicBoolean failed = new AtomicBoolean(false);
    List<Thread> threads = new ArrayList<Thread>();
    for(int i = 0; i < 10; i++) {
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          final RemoteCacheClient client = new RemoteCacheClient(conduit);
          final IdentifierMap identifierMap = new RemoteIdentifierMap (client); 
          try {
            for(int j = 0; j < 1000; j++) {
              int randomValue = rand.nextInt(100);
              String valueName = "Value" + randomValue;
              ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value",
                  new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk", valueName))));
              long id = identifierMap.getIdentifier (valueSpec);
              Long previousValue = _idsByValueName.putIfAbsent(valueName, id);
              if(previousValue != null) {
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
    for(Thread t : threads) {
      t.start();
    }
    for(Thread t : threads) {
      t.join();
    }
    assertFalse("One thread failed. Check logs.", failed.get());
  }
  
  //@Test(timeout=10000l)
  @Test
  public void singleThreadPutLoad() throws InterruptedException {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource (FudgeContext.GLOBAL_DEFAULT);
    ViewComputationCacheServer server = new ViewComputationCacheServer (cache);
    FudgeRequestSender conduit = InMemoryRequestConduit.create(server);
    RemoteCacheClient client = new RemoteCacheClient(conduit);
    final long timestamp = System.currentTimeMillis();
    BinaryDataStore dataStore = new RemoteBinaryDataStore (client, new ViewComputationCacheKey ("View1", "Config1", timestamp));
    final byte[] inputValue = new byte[256];
    for (int i = 0; i < inputValue.length; i++) {
      inputValue[i] = (byte)i;
    }
    final long identifier = 1L;
    dataStore.put(identifier, inputValue);
    
    byte[] outputValue = dataStore.get(identifier);
    assertNotNull (outputValue);
    assertArrayEquals(inputValue, outputValue);
    
    outputValue = dataStore.get (identifier + 1);
    assertNull (outputValue);
    
    outputValue = dataStore.get (identifier);
    assertNotNull (outputValue);
    assertArrayEquals (inputValue, outputValue);
    
  }

  @Test(timeout=10000l)
  public void singleThreadPutLoadPurgeLoad() throws InterruptedException {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource (FudgeContext.GLOBAL_DEFAULT);
    ViewComputationCacheServer server = new ViewComputationCacheServer (cache);
    FudgeRequestSender conduit = InMemoryRequestConduit.create(server);
    RemoteCacheClient client = new RemoteCacheClient(conduit);
    final long timestamp = System.currentTimeMillis();
    BinaryDataStore dataStore = new RemoteBinaryDataStore (client, new ViewComputationCacheKey ("View1", "Config1", timestamp));
    final byte[] inputValue = new byte[256];
    for (int i = 0; i < inputValue.length; i++) {
      inputValue[i] = (byte)i;
    }
    final long identifier = 1L;
    dataStore.put(identifier, inputValue);
    
    byte[] outputValue = dataStore.get (identifier);
    assertNotNull (outputValue);
    assertArrayEquals (inputValue, outputValue);
    
    dataStore.delete ();
    
    outputValue = dataStore.get (identifier);
    assertNull (outputValue);
  }

}
