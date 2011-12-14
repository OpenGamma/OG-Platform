/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.transport;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;

import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.transport.jms.JmsEndPointDescriptionProvider;
import com.opengamma.transport.socket.AbstractServerSocketProcess;
import com.opengamma.transport.socket.SocketFudgeRequestSender;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.jms.JmsConnector;

/**
 * Creates a {@link FudgeRequestSender} based on an end-point description. An end-point
 * could be a determined at runtime by a REST request to another host, or specified
 * statically. Examples of end points include TCP/IP host/socket pairs, REST URLs, and JMS
 * topic names.
 */
public class FudgeRequestSenderFactoryBean extends SingletonFactoryBean<FudgeRequestSender> {

  private FudgeContext _fudgeContext;
  private EndPointDescriptionProvider _endPointDescriptionProvider;
  private JmsConnector _jmsConnector;

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setEndPointDescription(final EndPointDescriptionProvider endPoint) {
    _endPointDescriptionProvider = endPoint;
  }

  public EndPointDescriptionProvider getEndPointDescription() {
    return _endPointDescriptionProvider;
  }

  public void setJmsConnector(final JmsConnector jmsConnector) {
    _jmsConnector = jmsConnector;
  }

  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  private FudgeMsg resolveEndPointDescription() {
    if (_endPointDescriptionProvider != null) {
      ArgumentChecker.notNullInjected(getFudgeContext(), "fudgeContext");
      return _endPointDescriptionProvider.getEndPointDescription(getFudgeContext());
    } else {
      return null;
    }
  }

  private FudgeRequestSender createSocketFudgeRequestSender(final FudgeMsg endPoint) {
    final SocketFudgeRequestSender sender = new SocketFudgeRequestSender(getFudgeContext());
    sender.setServer(endPoint);
    return sender;
  }

  private FudgeRequestSender createJmsFudgeRequestSender(final FudgeMsg endPoint) {
    ArgumentChecker.notNullInjected(getJmsConnector(), "jmsConnector");
    final String topic = endPoint.getString(JmsEndPointDescriptionProvider.TOPIC_KEY);
    return new ByteArrayFudgeRequestSender(new JmsByteArrayRequestSender(topic, getJmsConnector().getJmsTemplateTopic()), getFudgeContext());
  }

  @Override
  protected FudgeRequestSender createObject() {
    final FudgeMsg endPoint = resolveEndPointDescription();
    ArgumentChecker.notNullInjected(endPoint, "endPointDescription");
    if (AbstractServerSocketProcess.TYPE_VALUE.equals(endPoint.getString(AbstractServerSocketProcess.TYPE_KEY))) {
      return createSocketFudgeRequestSender(endPoint);
    }
    if (JmsEndPointDescriptionProvider.TYPE_VALUE.equals(endPoint.getString(JmsEndPointDescriptionProvider.TYPE_KEY))) {
      return createJmsFudgeRequestSender(endPoint);
    }
    throw new IllegalArgumentException("Don't know how to create end-point " + endPoint);
  }

}
