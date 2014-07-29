/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import org.springframework.context.Lifecycle;

import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.ResubscribingLiveDataClient;
import com.opengamma.util.jms.JmsConnector;

/**
 * A {@link LiveDataClient} wrapper that resubscribes based on {@link AvailabilityNotificationListener} notifications.
 */
public class JmsResubscribingLiveDataClient extends ResubscribingLiveDataClient implements Lifecycle {

  private final LiveDataClientAvailabilityListener _listener;

  public JmsResubscribingLiveDataClient(final LiveDataClient delegate, final String topic, final JmsConnector connector) {
    super(delegate);
    _listener = new LiveDataClientAvailabilityListener(this, topic, connector);
  }

  // Lifecycle

  @Override
  public void start() {
    _listener.start();
  }

  @Override
  public void stop() {
    _listener.stop();
  }

  @Override
  public boolean isRunning() {
    return _listener.isRunning();
  }
}
