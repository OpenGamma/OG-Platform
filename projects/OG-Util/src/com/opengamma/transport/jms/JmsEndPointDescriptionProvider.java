/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport.jms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.transport.EndPointDescriptionProvider;

/**
 * An implementation of {@link EndPointDescriptionProvider} that describes a JMS topic.
 */
public class JmsEndPointDescriptionProvider implements EndPointDescriptionProvider {

  /**
   * Type of connection. Always {@link #TYPE_VALUE}.
   */
  public static final String TYPE_KEY = "type";
  /**
   * Value of the type of connection.
   */
  public static final String TYPE_VALUE = "Jms";
  /**
   * Topic name(s).
   */
  public static final String TOPIC_KEY = "topic";

  private final List<String> _topics;

  public JmsEndPointDescriptionProvider(final String topic) {
    _topics = Collections.singletonList(topic);
  }

  public JmsEndPointDescriptionProvider(final List<String> topics) {
    _topics = new ArrayList<String>(topics);
  }

  @Override
  public FudgeMsg getEndPointDescription(final FudgeContext fudgeContext) {
    final MutableFudgeMsg msg = fudgeContext.newMessage();
    msg.add(TYPE_KEY, TYPE_VALUE);
    for (String topic : _topics) {
      msg.add(TOPIC_KEY, topic);
    }
    return msg;
  }

  /**
   * Default message production allows simple use in a configuration resource.
   * 
   * @param fudgeContext the Fudge context
   * @return the end point description message, as returned by {@link #getEndPointDescription}
   */
  public FudgeMsg toFudgeMsg(final FudgeContext fudgeContext) {
    return getEndPointDescription(fudgeContext);
  }

}
