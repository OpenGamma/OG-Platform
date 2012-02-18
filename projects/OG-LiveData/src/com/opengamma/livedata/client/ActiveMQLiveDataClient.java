/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.livedata.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.Topic;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.support.JmsUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.transport.FudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayMessageDispatcher;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.PublicAPI;
import com.opengamma.util.jms.JmsConnector;

/**
 * An ActiveMQ LiveData client. Behaves the same as {@link JmsLiveDataClient} except
 * that subscriptions are made to Composite Destinations in order to reduce
 * the time made to make subscriptions.
 * 
 * Currently subscriptions are only removed when none of the block is in use any more, this could be improved later. 
 */
@PublicAPI
public class ActiveMQLiveDataClient extends JmsLiveDataClient {
  //TODO: Could migrate to individual/smaller subscriptions in background 

  private static final Logger s_logger = LoggerFactory.getLogger(ActiveMQLiveDataClient.class);

  
  public ActiveMQLiveDataClient(FudgeRequestSender subscriptionRequestSender,
      FudgeRequestSender entitlementRequestSender, JmsConnector jmsConnector) {
    super(subscriptionRequestSender, entitlementRequestSender, jmsConnector);
  }

  public ActiveMQLiveDataClient(FudgeRequestSender subscriptionRequestSender,
      FudgeRequestSender entitlementRequestSender, JmsConnector jmsConnector, FudgeContext fudgeContext, int maxSessions) {
    super(subscriptionRequestSender, entitlementRequestSender, jmsConnector, fudgeContext, maxSessions);
  }

  public ActiveMQLiveDataClient(FudgeRequestSender subscriptionRequestSender,
      FudgeRequestSender entitlementRequestSender, JmsConnector jmsConnector, FudgeContext fudgeContext) {
    super(subscriptionRequestSender, entitlementRequestSender, jmsConnector, fudgeContext);
  }

  private final Map<String, ConsumerRecord> _messageConsumersBySpec = new ConcurrentHashMap<String, ConsumerRecord>();
  
  private class ConsumerRecord {
    private final MessageConsumer _consumer;
    private final Set<String> _allReceiving;
    private final Set<String> _receiving;

    public ConsumerRecord(MessageConsumer consumer, Collection<String> receiving) {
      super();
      _consumer = consumer;
      _receiving = new HashSet<String>(receiving);
      _allReceiving = Collections.unmodifiableSet(new HashSet<String>(receiving));
    }

    public MessageConsumer getConsumer() {
      return _consumer;
    }

    public Set<String> getReceiving() {
      return _receiving;
    }

    public Set<String> getAllReceiving() {
      return _allReceiving;
    }
  }
  @Override
  protected Map<String, Runnable> startReceivingTicks(List<String> specs, Session session,
      JmsByteArrayMessageDispatcher jmsDispatcher) {
    Map<String, Runnable> ret = new HashMap<String, Runnable>();
    if (specs.isEmpty()) {
      return ret;
    }
    
    for (String spec : specs) {
      ConsumerRecord record = _messageConsumersBySpec.get(spec);
      if (record != null) {
        //NOTE: could be on the wrong session, but we don't touch it
        record.getReceiving().add(spec);
        ret.put(spec, getCloseAction(spec, record));
      }
    }
    SetView<String> remaining = Sets.difference(new HashSet<String>(specs), ret.keySet());
    List<String> remainingList = new ArrayList<String>(remaining);
    for (List<String> partition : Lists.partition(remainingList, 100)) {
      String topicName = getCompositeTopicName(partition);
      try {
        Topic topic = session.createTopic(topicName);

        final MessageConsumer messageConsumer = session.createConsumer(topic);
        messageConsumer.setMessageListener(jmsDispatcher);
        ConsumerRecord record = new ConsumerRecord(messageConsumer, partition);
        for (String tickDistributionSpecification : partition) {
          _messageConsumersBySpec.put(tickDistributionSpecification, record);
          ret.put(tickDistributionSpecification, getCloseAction(tickDistributionSpecification, record)); 
        }
      } catch (JMSException e) {
        throw new OpenGammaRuntimeException("Failed to create subscription to JMS topics " + partition, e);
      }  
    }
    return ret;
  }

  private Runnable getCloseAction(final String tickDistributionSpecification, final ConsumerRecord record) {
    return new Runnable() {
      
      @Override
      public void run() {
        record.getReceiving().remove(tickDistributionSpecification);
        if (record.getReceiving().isEmpty()) {
          s_logger.debug("Closing connection after last unsubscribe {}", tickDistributionSpecification);
          JmsUtils.closeMessageConsumer(record.getConsumer());
          for (String receiving : record.getAllReceiving()) {
            _messageConsumersBySpec.remove(receiving);
          }
        } else {
          //TODO: Should I shrink the subscription? 
          s_logger.debug("Not closing composite connection remaining subscribtions {}", record.getReceiving());
        }
      }
    };
  }

  private String getCompositeTopicName(Collection<String> specs) {
    ArgumentChecker.notEmpty(specs, "specs");
    StringBuilder topicNameBuilder = new StringBuilder();
    for (String spec : specs) {
      topicNameBuilder.append(spec);
      topicNameBuilder.append(',');
    }
    topicNameBuilder.setLength(topicNameBuilder.length() - 1);
    String topicName = topicNameBuilder.toString();
    return topicName;
  }

  @Override
  public synchronized void close() {
    _messageConsumersBySpec.clear();
    super.close();
  }
}
