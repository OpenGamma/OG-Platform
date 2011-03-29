/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.view.cache;

import static org.testng.AssertJUnit.assertFalse;
import static org.testng.AssertJUnit.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.ComputationTargetSpecification;
import com.opengamma.engine.ComputationTargetType;
import com.opengamma.engine.value.ComputedValue;
import com.opengamma.engine.value.ValueRequirement;
import com.opengamma.engine.value.ValueSpecification;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.socket.ServerSocketFudgeConnectionReceiver;
import com.opengamma.transport.socket.SocketFudgeConnection;
import com.opengamma.util.ThreadUtils;
import com.opengamma.util.ehcache.EHCacheUtils;
import com.opengamma.util.fudge.OpenGammaFudgeContext;
import com.opengamma.util.tuple.Pair;

/**
 * A test of the remote View Computation Cache Source infrastucture operating
 * over proper sockets.
 */
public class ServerSocketRemoteViewComputationCacheTest {
  private static final Logger s_logger = LoggerFactory.getLogger(ServerSocketRemoteViewComputationCacheTest.class);
  private static final FudgeContext s_fudgeContext = OpenGammaFudgeContext.getInstance();
  private static final int NUM_THREADS = 5;
  private static final int NUM_LOOKUPS = 1000;
  private static final int FLUSH_DELAY = 600;
  private ViewComputationCacheSource _cacheSource;
  private ServerSocketFudgeConnectionReceiver _serverSocket;
  private SocketFudgeConnection _socket;

  private void setupCacheSource(final boolean lazyReads, final int cacheSize, final int flushDelay) {
    InMemoryViewComputationCacheSource cache = new InMemoryViewComputationCacheSource(s_fudgeContext);
    ViewComputationCacheServer server = new ViewComputationCacheServer(cache);
    _serverSocket = new ServerSocketFudgeConnectionReceiver(cache.getFudgeContext(), server, Executors
        .newCachedThreadPool());
    _serverSocket.setLazyFudgeMsgReads(lazyReads);
    _serverSocket.start();
    _socket = new SocketFudgeConnection(cache.getFudgeContext());
    _socket.setFlushDelay(flushDelay);
    try {
      _socket.setInetAddress(InetAddress.getLocalHost());
    } catch (UnknownHostException e) {
      throw new OpenGammaRuntimeException("", e);
    }
    _socket.setPortNumber(_serverSocket.getPortNumber());

    RemoteCacheClient client = new RemoteCacheClient(_socket);
    _cacheSource = new RemoteViewComputationCacheSource(client, new DefaultFudgeMessageStoreFactory(
        new InMemoryBinaryDataStoreFactory(), s_fudgeContext), EHCacheUtils.createCacheManager(), cacheSize);
  }

  private void shutDown() {
    if (_socket != null) {
      _socket.stop();
    }
    if (_serverSocket != null) {
      _serverSocket.stop();
    }
    _cacheSource = null;
    _socket = null;
    _serverSocket = null;
  }

  @SuppressWarnings("unchecked")
  private void assertLikelyValue(final Object value) {
    assertTrue(value instanceof List<?>);
    for (Object valueElement : (List<Object>) value) {
      assertTrue(valueElement instanceof double[]);
    }
  }

  private Object createValue(final Random rand) {
    final double[][] data = new double[rand.nextInt(100)][rand.nextInt(100)];
    for (int i = 0; i < data.length; i++) {
      for (int j = 0; j < data[i].length; j++) {
        data[i][j] = rand.nextDouble();
      }
    }
    return data;
  }

  private Pair<Double, Double> multiThreadedTestImpl() {
    final Random rand = new Random();
    final AtomicBoolean failed = new AtomicBoolean(false);
    final AtomicLong getTime = new AtomicLong(0);
    final AtomicLong putTime = new AtomicLong(0);
    List<Thread> threads = new ArrayList<Thread>();
    final long timestamp = System.currentTimeMillis();
    for (int i = 0; i < NUM_THREADS; i++) {
      // Half the threads on one cache, half on another
      final long cacheTimestamp = ((i & 1) == 0) ? timestamp : (timestamp + 1);
      Thread t = new Thread(new Runnable() {
        @Override
        public void run() {
          final ViewComputationCache cache = _cacheSource.getCache("multiThreadedTest", "default", cacheTimestamp);
          try {
            long tGet = 0;
            long tPut = 0;
            for (int j = 0; j < NUM_LOOKUPS; j++) {
              int randomValue = rand.nextInt(100);
              String valueName = "Value" + randomValue;
              ValueSpecification valueSpec = new ValueSpecification(new ValueRequirement("Test Value",
                  new ComputationTargetSpecification(ComputationTargetType.PRIMITIVE, UniqueIdentifier.of("Kirk",
                      valueName))), "mockFunctionId");

              boolean putValue = true;
              if (j > 0) {
                // Don't try and get on the first attempt as the cache probably isn't created at the server
                tGet -= System.nanoTime();
                Object ultimateValue = cache.getValue(valueSpec);
                tGet += System.nanoTime();
                if (ultimateValue != null) {
                  assertLikelyValue(ultimateValue);
                  putValue = rand.nextDouble() < 0.3;
                }
              }

              if (putValue) {
                ComputedValue cv = new ComputedValue(valueSpec, createValue(rand));
                tPut -= System.nanoTime();
                cache.putSharedValue(cv);
                tPut += System.nanoTime();
              }
            }
            s_logger.debug("Get = {}ms, Put = {}ms", (double) tGet / 1e6, (double) tPut / 1e6);
            getTime.addAndGet(tGet);
            putTime.addAndGet(tPut);
          } catch (Throwable e) {
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
      ThreadUtils.safeJoin(t, 10000L);
    }
    assertFalse("One thread failed. Check logs.", failed.get());
    final double get = (double) getTime.get() / (1e6 * NUM_THREADS * NUM_LOOKUPS);
    final double put = (double) putTime.get() / (1e6 * NUM_THREADS * NUM_LOOKUPS);
    s_logger.info("{} get operations @ {}ms", NUM_THREADS * NUM_LOOKUPS, get);
    s_logger.info("{} put operations @ {}ms", NUM_THREADS * NUM_LOOKUPS, put);
    return Pair.of(get, put);
  }

  @Test
  public void multiThreadedTestLazyReadsNoCache() {
    setupCacheSource(true, 1, FLUSH_DELAY);
    multiThreadedTestImpl();
    shutDown();
  }

  @Test
  public void multiThreadedTestLazyReadsFullCache() {
    setupCacheSource(true, 100, FLUSH_DELAY);
    multiThreadedTestImpl();
    shutDown();
  }

  @Test
  public void multiThreadedTestFullReadsNoCache() {
    setupCacheSource(false, 1, FLUSH_DELAY);
    multiThreadedTestImpl();
    shutDown();
  }

  @Test
  public void multiThreadedTestFullReadsFullCache() {
    setupCacheSource(false, 100, FLUSH_DELAY);
    multiThreadedTestImpl();
    shutDown();
  }

  @Test(enabled = false)
  public void varyingFlushDelays() {
    final int[] delays = new int[] {400, 500, 600, 700};
    // Repeat to allow for timing discrepencies
    for (int i = 0; i < 5; i++) {
      final StringBuilder result = new StringBuilder();
      for (int delay : delays) {
        setupCacheSource(true, 1, delay);
        final Pair<Double, Double> times = multiThreadedTestImpl();
        shutDown();
        result.append("\r\n").append("Delay=").append(delay).append("ms. Get=").append(times.getFirst()).append(
            "ms, Put=").append(times.getSecond()).append("ms");
      }
      s_logger.info("{}", result);
    }
  }

}
