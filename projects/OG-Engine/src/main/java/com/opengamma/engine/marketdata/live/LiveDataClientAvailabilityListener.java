/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Set;

import com.opengamma.id.ExternalScheme;
import com.opengamma.livedata.ResubscribingLiveDataClient;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;

/**
 * Listens for notifications that market data is available and invokes {@link ResubscribingLiveDataClient#resubscribe()}.
 */
public class LiveDataClientAvailabilityListener extends AvailabilityNotificationListener {

  private final ResubscribingLiveDataClient _client;

  /**
   * @param client the client to resubscribe when a notification occurs
   * @param topic The topic for {@link MarketDataAvailabilityNotification} messages
   * @param jmsConnector For receiving JMS messages
   */
  public LiveDataClientAvailabilityListener(ResubscribingLiveDataClient client, String topic, JmsConnector jmsConnector) {
    super(topic, jmsConnector);
    _client = ArgumentChecker.notNull(client, "client");
  }

  @Override
  protected void notificationReceived(Set<ExternalScheme> schemes) {
    _client.resubscribe();
  }
}
