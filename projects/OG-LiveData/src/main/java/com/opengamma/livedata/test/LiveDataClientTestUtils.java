/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.springframework.context.Lifecycle;

import com.opengamma.livedata.client.DistributedLiveDataClient;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.livedata.entitlement.EntitlementServer;
import com.opengamma.livedata.server.StandardLiveDataServer;
import com.opengamma.livedata.server.SubscriptionRequestReceiver;
import com.opengamma.livedata.server.distribution.FudgeSenderFactory;
import com.opengamma.livedata.server.distribution.JmsSenderFactory;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.ByteArrayFudgeMessageSender;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.DirectInvocationByteArrayMessageSender;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.FudgeRequestDispatcher;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.InMemoryByteArrayRequestConduit;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.test.ActiveMQTestUtils;
import com.opengamma.util.test.TestLifecycle;

/**
 * Utility methods to get LiveData clients suitable for testing.
 */
public class LiveDataClientTestUtils {

  private static ExecutorService executor(final int threads) {
    if (threads == 0) {
      return null;
    }
    final ExecutorService executor = Executors.newFixedThreadPool(threads);
    TestLifecycle.register(new Lifecycle() {

      @Override
      public void start() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void stop() {
        executor.shutdown();
      }

      @Override
      public boolean isRunning() {
        return !executor.isShutdown();
      }

    });
    return executor;
  }

  /**
   * Creates a test client connected to the server.
   * 
   * @param server the test server, not null
   * @param threads the number of communication threads, or 0 for direct/inline calls to the server
   * @return the client
   */
  public static DistributedLiveDataClient getInMemoryConduitClient(final StandardLiveDataServer server, final int threads) {
    final ExecutorService executor = executor(threads);
    final FudgeRequestSender subscriptionRequestSender = getSubscriptionRequestSender(server, executor);
    final FudgeRequestSender entitlementRequestSender = getEntitlementRequestSender(server, executor);
    final DistributedLiveDataClient liveDataClient = new DistributedLiveDataClient(subscriptionRequestSender, entitlementRequestSender);
    final FudgeSenderFactory factory = new FudgeSenderFactory(new ByteArrayFudgeMessageSender(new DirectInvocationByteArrayMessageSender(new ByteArrayFudgeMessageReceiver(liveDataClient))));
    server.setMarketDataSenderFactory(factory);
    liveDataClient.setFudgeContext(liveDataClient.getFudgeContext());
    return liveDataClient;
  }

  public static DistributedLiveDataClient getInMemoryConduitClient(final StandardLiveDataServer server) {
    return getInMemoryConduitClient(server, 0);
  }

  /**
   * Creates a test client connected to the server.
   * 
   * @param server the test server, not null
   * @param threads the number of communication threads, or 0 for direct/inline calls to the server
   * @return the client
   */
  public static JmsLiveDataClient getJmsClient(final StandardLiveDataServer server, final int threads) {
    final ExecutorService executor = executor(threads);
    final FudgeRequestSender subscriptionRequestSender = getSubscriptionRequestSender(server, executor);
    final FudgeRequestSender entitlementRequestSender = getEntitlementRequestSender(server, executor);
    final JmsConnector jmsConnector = ActiveMQTestUtils.createTestJmsConnector();
    final JmsLiveDataClient liveDataClient = new JmsLiveDataClient(subscriptionRequestSender, entitlementRequestSender, jmsConnector);
    final JmsSenderFactory factory = new JmsSenderFactory();
    factory.setJmsConnector(jmsConnector);
    server.setMarketDataSenderFactory(factory);
    liveDataClient.setFudgeContext(liveDataClient.getFudgeContext());
    liveDataClient.start();
    return liveDataClient;
  }

  public static JmsLiveDataClient getJmsClient(final StandardLiveDataServer server) {
    return getJmsClient(server, 0);
  }

  private static FudgeRequestSender sender(final FudgeRequestSender sender, final ExecutorService executor) {
    if (executor == null) {
      return sender;
    } else {
      return new FudgeRequestSender() {

        @Override
        public FudgeContext getFudgeContext() {
          return sender.getFudgeContext();
        }

        @Override
        public void sendRequest(final FudgeMsg request, final FudgeMessageReceiver responseReceiver) {
          executor.execute(new Runnable() {
            @Override
            public void run() {
              try {
                sender.sendRequest(request, responseReceiver);
              } catch (Throwable t) {
                t.printStackTrace();
              }
            }
          });
        }

      };
    }
  }

  private static FudgeRequestSender getEntitlementRequestSender(final StandardLiveDataServer server, final ExecutorService executor) {
    return sender(new ByteArrayFudgeRequestSender(new InMemoryByteArrayRequestConduit(new FudgeRequestDispatcher(new EntitlementServer(server.getEntitlementChecker())))), executor);
  }

  private static FudgeRequestSender getSubscriptionRequestSender(final StandardLiveDataServer server, final ExecutorService executor) {
    return sender(new ByteArrayFudgeRequestSender(new InMemoryByteArrayRequestConduit(new FudgeRequestDispatcher(new SubscriptionRequestReceiver(server)))), executor);
  }

}
