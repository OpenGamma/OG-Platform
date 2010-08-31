/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

import org.fudgemsg.FudgeContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.socket.ServerSocketFudgeRequestDispatcher;
import com.opengamma.transport.socket.SocketFudgeRequestSender;
import com.opengamma.util.ThreadUtil;

/**
 * A test of the remote View Computation Cache Source infrastucture operating
 * over proper sockets.
 *
 */
public class ServerSocketRemoteViewComputationCacheTest {
  private static final Logger s_logger = LoggerFactory.getLogger(ServerSocketRemoteViewComputationCacheTest.class);
  private static final int NUM_THREADS = 5;
  private static final int NUM_LOOKUPS = 1000;
  private ViewComputationCacheSource _cacheSource;
  private ServerSocketFudgeRequestDispatcher _serverSocketDispatcher;
  private SocketFudgeRequestSender _socketSender;
  
  @Before
  public void setupCacheSource() throws UnknownHostException {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource (FudgeContext.GLOBAL_DEFAULT);
    ViewComputationCacheServer server = new ViewComputationCacheServer (cache);
    _serverSocketDispatcher = new ServerSocketFudgeRequestDispatcher(server, cache.getFudgeContext ());
    _serverSocketDispatcher.start();
    
    _socketSender = new SocketFudgeRequestSender();
    _socketSender.setInetAddress(InetAddress.getLocalHost());
    _socketSender.setPortNumber(_serverSocketDispatcher.getPortNumber());
    
    RemoteCacheClient client = new RemoteCacheClient(_socketSender);
    _cacheSource = new RemoteViewComputationCacheSource (client, new InMemoryBinaryDataStoreFactory ());
  }
  
  @After
  public void shutDown() {
    if(_socketSender != null) {
      _socketSender.stop();
    }
    if(_serverSocketDispatcher != null) {
      _serverSocketDispatcher.stop();
    }
    _cacheSource = null;
    _socketSender = null;
    _serverSocketDispatcher = null;
  }
  
  @Test
  public void multiThreadedTest() {
    final Random rand = new Random();
    final AtomicBoolean failed = new AtomicBoolean(false);
    List<Thread> threads = new ArrayList<Thread>();
    for(int i = 0; i < NUM_THREADS; i++) {
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          final ViewComputationCache cache = _cacheSource.getCache("multiThreadedTest", "default", System.currentTimeMillis());
          try {
            for(int j = 0; j < NUM_LOOKUPS; j++) {
              int randomValue = rand.nextInt(100);
              String valueName = "Value" + randomValue;
              ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement(
                  "Test Value", 
                  new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk", valueName))),
                  "mockFunctionId");
              
              boolean putValue = true;
              Object ultimateValue = cache.getValue(valueSpec);
              if(ultimateValue != null) {
                assertTrue(ultimateValue instanceof Double);
                putValue = rand.nextDouble() < 0.3;
              }

              if(putValue) {
                ComputedValue cv = new ComputedValue(valueSpec, rand.nextDouble());
                cache.putSharedValue(cv);
              }
            }
          } catch (Throwable e) {
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
      ThreadUtil.safeJoin(t, 10000L);
    }
    assertFalse("One thread failed. Check logs.", failed.get());
  }

}
