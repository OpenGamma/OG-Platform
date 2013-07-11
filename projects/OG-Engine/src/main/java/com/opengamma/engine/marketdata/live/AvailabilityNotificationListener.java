/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Collection;
import java.util.Set;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.Lifecycle;

import com.opengamma.id.ExternalScheme;
import com.opengamma.transport.ByteArrayFudgeMessageReceiver;
import com.opengamma.transport.FudgeMessageReceiver;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsTopicContainer;

/**
 * Listens to JMS messages announcing that market data providers have become available and
 * notifies {@link LiveDataFactory} instances so they can retry failed subscriptions.
 */
public class AvailabilityNotificationListener implements Lifecycle {

  /** Logger */
  private static final Logger s_logger = LoggerFactory.getLogger(AvailabilityNotificationListener.class);

  /** For receiving JMS messages. */
  private final JmsTopicContainer _jmsTopicContainer;

  /**
   * @param topic The topic for {@link MarketDataAvailabilityNotification} messages
   * @param factories Factories that will be notified when a market data provider becomes available
   * @param jmsConnector For receiving JMS messages
   */
  public AvailabilityNotificationListener(String topic, Collection<LiveDataFactory> factories, JmsConnector jmsConnector) {
    ByteArrayFudgeMessageReceiver receiver = new ByteArrayFudgeMessageReceiver(new Receiver(factories));
    JmsByteArrayMessageDispatcher dispatcher = new JmsByteArrayMessageDispatcher(receiver);
    _jmsTopicContainer = jmsConnector.getTopicContainerFactory().create(topic, dispatcher);
  }

  @Override
  public void start() {
    _jmsTopicContainer.start();
  }

  @Override
  public void stop() {
    _jmsTopicContainer.stop();
  }

  @Override
  public boolean isRunning() {
    return _jmsTopicContainer.isRunning();
  }

  /**
   * Receives {@link MarketDataAvailabilityNotification}s via Fudge and calls {@link LiveDataFactory#resubscribe}
   * on each of its factories.
   */
  private static final class Receiver implements FudgeMessageReceiver {

    /** Factories to notify when a market data provider becomes available. */
    private final Collection<LiveDataFactory> _factories;

    /**
     * @param factories Factories to notify when a market data provider becomes available
     */
    private Receiver(Collection<LiveDataFactory> factories) {
      ArgumentChecker.notEmpty(factories, "factories");
      _factories = factories;
    }

    @Override
    public void messageReceived(FudgeContext fudgeContext, FudgeMsgEnvelope msgEnvelope) {
      FudgeDeserializer deserializer = new FudgeDeserializer(fudgeContext);
      FudgeMsg msg = msgEnvelope.getMessage();
      MarketDataAvailabilityNotification notification =
          deserializer.fudgeMsgToObject(MarketDataAvailabilityNotification.class, msg);
      s_logger.info("Received notification of market data availability: {}", notification);
      Set<ExternalScheme> schemes = notification.getSchemes();
      for (LiveDataFactory factory : _factories) {
        factory.resubscribe(schemes);
      }
    }
  }
}
