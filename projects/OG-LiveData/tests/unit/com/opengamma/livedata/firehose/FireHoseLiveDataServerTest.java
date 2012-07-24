/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */

package com.opengamma.livedata.firehose;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.MutableFudgeMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.livedata.LiveDataValueUpdateBean;
import com.opengamma.livedata.server.AbstractLiveDataServer;
import com.opengamma.livedata.server.distribution.MarketDataDistributor;
import com.opengamma.livedata.server.distribution.MarketDataSender;
import com.opengamma.livedata.server.distribution.MarketDataSenderFactory;
import com.opengamma.util.test.Timeout;

/**
 * Test.
 */
@Test(groups = {"unit", "slow"})
public class FireHoseLiveDataServerTest {

  private static final Logger s_logger = LoggerFactory.getLogger(FireHoseLiveDataServerTest.class);

  private static class FireHose extends AbstractFireHoseLiveData {

    @Override
    public void start() {
    }

    @Override
    public void stop() {
    }

    @Override
    public boolean isStarted() {
      return false;
    }
  }

  public void testBasicSnapshotOperation() {
    final FireHose fireHose = new FireHose();
    final FireHoseLiveDataServer liveDataServer = new FireHoseLiveDataServer(ExternalSchemes.SURF, fireHose);
    liveDataServer.start();
    try {
      final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
      msg.add("X", "Y");
      fireHose.storeValue("Foo", msg);
      assertEquals(liveDataServer.doSnapshot("Foo").getString("X"), "Y");
    } finally {
      liveDataServer.stop();
    }
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testMissingSnapshotOperation() {
    final FireHose fireHose = new FireHose();
    final FireHoseLiveDataServer liveDataServer = new FireHoseLiveDataServer(ExternalSchemes.SURF, fireHose);
    liveDataServer.start();
    try {
      FireHoseLiveDataServer.getExecutorService().submit(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(Timeout.standardTimeoutMillis() / 8);
          } catch (InterruptedException e) {
            throw new OpenGammaRuntimeException("Interrupted", e);
          }
          fireHose.setMarketDataComplete(true);
        }
      });
      liveDataServer.doSnapshot("Foo");
    } finally {
      liveDataServer.stop();
    }
  }

  @Test(expectedExceptions = {OpenGammaRuntimeException.class })
  public void testSnapshotTimeoutOperation() {
    final FireHose fireHose = new FireHose();
    final FireHoseLiveDataServer liveDataServer = new FireHoseLiveDataServer(ExternalSchemes.SURF, fireHose);
    liveDataServer.setMarketDataTimeout(Timeout.standardTimeoutMillis() / 4, TimeUnit.MILLISECONDS);
    liveDataServer.start();
    try {
      liveDataServer.doSnapshot("Foo");
    } finally {
      liveDataServer.stop();
    }
  }

  public void testSnapshotOperationLateData() {
    final FireHose fireHose = new FireHose();
    final FireHoseLiveDataServer liveDataServer = new FireHoseLiveDataServer(ExternalSchemes.SURF, fireHose);
    liveDataServer.start();
    try {
      FireHoseLiveDataServer.getExecutorService().submit(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(Timeout.standardTimeoutMillis() / 8);
          } catch (InterruptedException e) {
            throw new OpenGammaRuntimeException("Interrupted", e);
          }
          final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
          msg.add("X", "Y");
          fireHose.storeValue("Foo", msg);
        }
      });
      assertEquals(liveDataServer.doSnapshot("Foo").getString("X"), "Y");
    } finally {
      liveDataServer.stop();
    }
  }

  private BlockingQueue<LiveDataValueUpdateBean> connect(final AbstractLiveDataServer liveDataServer, final int buffer) {
    final BlockingQueue<LiveDataValueUpdateBean> queue = new LinkedBlockingQueue<LiveDataValueUpdateBean>(buffer);
    liveDataServer.setMarketDataSenderFactory(new MarketDataSenderFactory() {
      @Override
      public Collection<MarketDataSender> create(final MarketDataDistributor distributor) {
        return Collections.<MarketDataSender>singleton(new MarketDataSender() {

          @Override
          public void sendMarketData(final LiveDataValueUpdateBean data) {
            try {
              queue.put(data);
            } catch (InterruptedException e) {
              throw new OpenGammaRuntimeException("Interrupted", e);
            }
          }

          @Override
          public MarketDataDistributor getDistributor() {
            return distributor;
          }

        });
      }
    });
    return queue;
  }

  public void testBasicSubscription() throws InterruptedException {
    final FireHose fireHose = new FireHose();
    final FireHoseLiveDataServer liveDataServer = new FireHoseLiveDataServer(ExternalSchemes.SURF, fireHose);
    final BlockingQueue<LiveDataValueUpdateBean> updates = connect(liveDataServer, Integer.MAX_VALUE);
    liveDataServer.start();
    try {
      final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
      msg.add("X", "Y");
      fireHose.storeValue("Foo", msg);
      assertNotNull(liveDataServer.subscribe("Foo"));
      final LiveDataValueUpdateBean update = updates.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNotNull(update);
      assertEquals(update.getFields().getString("X"), "Y");
    } finally {
      liveDataServer.stop();
    }
  }

  public void testMissingSubscription() throws InterruptedException {
    final FireHose fireHose = new FireHose();
    final FireHoseLiveDataServer liveDataServer = new FireHoseLiveDataServer(ExternalSchemes.SURF, fireHose);
    final BlockingQueue<LiveDataValueUpdateBean> updates = connect(liveDataServer, Integer.MAX_VALUE);
    liveDataServer.start();
    try {
      assertNotNull(liveDataServer.subscribe("Foo"));
      final LiveDataValueUpdateBean update = updates.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
      assertNull(update);
    } finally {
      liveDataServer.stop();
    }
  }

  @Test(invocationCount = 3, successPercentage = 25)
  public void testRapidUpdates() throws InterruptedException {
    // If the live data server abstraction is slow to consume the fire hose, updates will be lost and the
    // most recent values should win.
    final FireHose fireHose = new FireHose();
    final FireHoseLiveDataServer liveDataServer = new FireHoseLiveDataServer(ExternalSchemes.SURF, fireHose);
    final BlockingQueue<LiveDataValueUpdateBean> updates = connect(liveDataServer, 1);
    liveDataServer.start();
    try {
      FireHoseLiveDataServer.getExecutorService().submit(new Runnable() {
        @Override
        public void run() {
          try {
            Thread.sleep(Timeout.standardTimeoutMillis() / 4);
            s_logger.debug("Generating updates");
            for (int i = 0; i < 100; i++) {
              final MutableFudgeMsg msg = FudgeContext.GLOBAL_DEFAULT.newMessage();
              msg.add("X", i);
              fireHose.storeValue("Foo", msg);
              if (i == 50) {
                s_logger.debug("Pausing at 50");
                Thread.sleep(Timeout.standardTimeoutMillis());
              }
            }
            s_logger.debug("Updates complete");
          } catch (InterruptedException e) {
            throw new OpenGammaRuntimeException("Interrupted", e);
          }
        }
      });
      liveDataServer.subscribe("Foo");
      // An early value (<=50) will be written to the queue
      // A second will be blocked (<=50) until we have finished polling and then written
      // A third will be blocked (<=50) while we are paused
      // During that pause, values up to 50 will be produced, and 50 ready for delivery.
      // We will thus see four updates less than or equal to 50.
      // We will then see one or more values up to 99.
      int low = 0;
      int high = 0;
      do {
        final LiveDataValueUpdateBean update = updates.poll(Timeout.standardTimeoutMillis(), TimeUnit.MILLISECONDS);
        s_logger.info("Got update {}", update);
        final int v = update.getFields().getInt("X");
        if (v <= 50) {
          low++;
          if (low == 1) {
            Thread.sleep(Timeout.standardTimeoutMillis() / 2);
          }
        } else {
          high++;
          if (v == 99) {
            break;
          }
        }
      } while (true);
      s_logger.info("Low = {}, High = {}", low, high);
      assertEquals(low, 4);
      assertTrue((high > 0) && (high < 50));
    } finally {
      liveDataServer.stop();
    }
  }
}
