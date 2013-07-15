/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Collection;
import java.util.Set;

import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;

/**
 * Listens to JMS messages announcing that market data providers have become available and
 * notifies {@link LiveDataFactory} instances so they can retry failed subscriptions.
 */
public class LiveDataAvailabilityNotificationListener extends AvailabilityNotificationListener {

  /** Factories to notify when a market data provider becomes available. */
  private final Collection<LiveDataFactory> _factories;

  /**
   * @param topic The topic for {@link MarketDataAvailabilityNotification} messages
   * @param factories Factories that will be notified when a market data provider becomes available
   * @param jmsConnector For receiving JMS messages
   */
  public LiveDataAvailabilityNotificationListener(String topic,
                                                  Collection<LiveDataFactory> factories,
                                                  JmsConnector jmsConnector) {
    super(topic, jmsConnector);
    ArgumentChecker.notEmpty(factories, "factories");
    _factories = factories;
  }

  @Override
  protected void notificationReceived(Set<ExternalScheme> schemes) {
    for (LiveDataFactory factory : _factories) {
      factory.resubscribe(schemes);
    }
  }
}
