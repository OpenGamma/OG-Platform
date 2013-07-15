/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.marketdata.live;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ViewProcess;
import com.opengamma.engine.view.impl.ViewProcessImpl;
import com.opengamma.engine.view.impl.ViewProcessorInternal;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.jms.JmsConnector;

/**
 * Listens to JMS messages announcing that market data providers have become available and
 * forces all view processes to rebuild their graph. This retries any failed market data subscriptions.
 * @deprecated This is a temporary fix, once PLAT-3908 is resolved this class won't be necessary
 */
@Deprecated
public class ViewProcessAvailabilityNotificationListener extends AvailabilityNotificationListener {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ViewProcessAvailabilityNotificationListener.class);

  /** The view processor, used to obtain the running view processes. */
  private final ViewProcessorInternal _viewProcessor;

  /**
   * @param topic The topic for {@link MarketDataAvailabilityNotification} messages
   * @param jmsConnector For receiving JMS messages
   * @param viewProcessor The view processor, used to obtain the running view processes.
   */
  public ViewProcessAvailabilityNotificationListener(String topic,
                                                     JmsConnector jmsConnector,
                                                     ViewProcessorInternal viewProcessor) {
    super(topic, jmsConnector);
    ArgumentChecker.notNull(viewProcessor, "viewProcessor");
    _viewProcessor = viewProcessor;
  }

  @Override
  protected void notificationReceived(Set<ExternalScheme> schemes) {
    for (ViewProcess viewProcess : _viewProcessor.getViewProcesses()) {
      if (viewProcess instanceof ViewProcessImpl) {
        s_logger.info("Forcing graph rebuild for {}", viewProcess);
        ((ViewProcessImpl) viewProcess).forceGraphRebuild();
      }
    }
  }
}
