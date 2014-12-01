/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.net.URI;

import org.springframework.jms.core.JmsTemplate;
import org.threeten.bp.LocalDate;

import com.opengamma.core.value.MarketDataRequirementNames;
import com.opengamma.livedata.LiveDataClient;
import com.opengamma.livedata.client.JmsLiveDataClient;
import com.opengamma.provider.livedata.LiveDataMetaData;
import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataServerTypes;
import com.opengamma.timeseries.date.DateTimeSeries;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.jms.JmsConnector;
import com.opengamma.util.jms.JmsConnectorFactoryBean;
import com.opengamma.util.metric.OpenGammaMetricRegistry;

/**
 * Market data util methods.
 */
class MarketDataUtils {

  /** The default field name used for looking up data in a market data record. */
  static final FieldName MARKET_VALUE = FieldName.of(MarketDataRequirementNames.MARKET_VALUE);

  private MarketDataUtils() {
  }

  static LocalDateDoubleTimeSeries asLocalDateDoubleTimeSeries(DateTimeSeries<LocalDate, Double> timeSeries) {
    ArgumentChecker.notNull(timeSeries, "timeSeries");

    if (timeSeries instanceof LocalDateDoubleTimeSeries) {
      return (LocalDateDoubleTimeSeries) timeSeries;
    }
    throw new IllegalArgumentException("Time series of type " + timeSeries.getClass().getName() + " cannot be " +
                                           "converted to a LocalDateDoubleTimeSeries");
  }

  /**
   * Creates a live data client based on the information in the remote metadata.
   * <p>
   * This was copy-pasted from {@code LiveMarketDataProviderFactoryComponentFactory} because having application code
   * depending on configuration code seems particularly nasty.
   *
   * @param provider the metadata provider
   * @param jmsConnector the JMS connector
   * @return the client
   */
  @SuppressWarnings("deprecation")
  static LiveDataClient createLiveDataClient(LiveDataMetaDataProvider provider, JmsConnector jmsConnector) {
    ArgumentChecker.notNull(jmsConnector, "jmsConnector");
    ArgumentChecker.notNull(provider, "provider");

    LiveDataMetaData metaData = provider.metaData();
    URI jmsUri = metaData.getJmsBrokerUri();

    if (metaData.getServerType() != LiveDataServerTypes.STANDARD || jmsUri == null) {
      throw new IllegalArgumentException("Unsupported live data server type " + metaData.getServerType() +
                                             " for " + metaData.getDescription() + " live data provider.");
    }
    if (!jmsConnector.getClientBrokerUri().equals(jmsUri)) {
      JmsConnectorFactoryBean jmsFactory = new JmsConnectorFactoryBean(jmsConnector);
      jmsFactory.setClientBrokerUri(jmsUri);
      jmsConnector = jmsFactory.getObjectCreating();
    }
    JmsTemplate jmsTemplate = jmsConnector.getJmsTemplateTopic();
    JmsByteArrayRequestSender jmsSubscriptionRequestSender;

    if (metaData.getJmsSubscriptionQueue() != null) {
      JmsTemplate subscriptionRequestTemplate = jmsConnector.getJmsTemplateQueue();
      jmsSubscriptionRequestSender = new JmsByteArrayRequestSender(metaData.getJmsSubscriptionQueue(),
                                                                   subscriptionRequestTemplate);
    } else {
      jmsSubscriptionRequestSender = new JmsByteArrayRequestSender(metaData.getJmsSubscriptionTopic(), jmsTemplate);
    }
    ByteArrayFudgeRequestSender fudgeSubscriptionRequestSender =
        new ByteArrayFudgeRequestSender(jmsSubscriptionRequestSender);

    JmsByteArrayRequestSender jmsEntitlementRequestSender =
        new JmsByteArrayRequestSender(metaData.getJmsEntitlementTopic(), jmsTemplate);
    ByteArrayFudgeRequestSender fudgeEntitlementRequestSender =
        new ByteArrayFudgeRequestSender(jmsEntitlementRequestSender);

    JmsLiveDataClient liveDataClient = new JmsLiveDataClient(fudgeSubscriptionRequestSender,
                                                             fudgeEntitlementRequestSender,
                                                             jmsConnector,
                                                             OpenGammaFudgeContext.getInstance(),
                                                             JmsLiveDataClient.DEFAULT_NUM_SESSIONS);
    liveDataClient.setFudgeContext(OpenGammaFudgeContext.getInstance());

    if (metaData.getJmsHeartbeatTopic() != null) {
      JmsByteArrayMessageSender jmsHeartbeatSender =
          new JmsByteArrayMessageSender(metaData.getJmsHeartbeatTopic(), jmsTemplate);
      liveDataClient.setHeartbeatMessageSender(jmsHeartbeatSender);
    }
    liveDataClient.start();
    liveDataClient.registerMetrics(OpenGammaMetricRegistry.getSummaryInstance(),
                                   OpenGammaMetricRegistry.getDetailedInstance(),
                                   "LiveDataClient - " + provider.metaData().getDescription());
    return liveDataClient;
  }
}
