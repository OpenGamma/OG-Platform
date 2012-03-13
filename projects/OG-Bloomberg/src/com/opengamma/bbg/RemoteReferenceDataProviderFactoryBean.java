/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg;

import org.fudgemsg.FudgeContext;
import org.springframework.jms.core.JmsTemplate;

import com.opengamma.transport.ByteArrayFudgeRequestSender;
import com.opengamma.transport.jms.JmsByteArrayRequestSender;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.jms.JmsConnector;

/**
 * Factory bean for {@link RemoteReferenceDataProvider}.
 */
public class RemoteReferenceDataProviderFactoryBean extends SingletonFactoryBean<RemoteReferenceDataProvider> {

  private JmsConnector _jmsConnector;
  private String _requestTopic;
  private FudgeContext _fudgeContext;

  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  public void setJmsConnector(JmsConnector jmsConnector) {
    _jmsConnector = jmsConnector;
  }

  public String getRequestTopic() {
    return _requestTopic;
  }

  public void setRequestTopic(String requestTopic) {
    _requestTopic = requestTopic;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  @Override
  protected RemoteReferenceDataProvider createObject() {
    if (getJmsConnector() == null) {
      throw new IllegalArgumentException("connectionFactory must be set");
    }
    if (getRequestTopic() == null) {
      throw new IllegalArgumentException("requestTopic must be set");
    }
    if (getFudgeContext() == null) {
      throw new IllegalArgumentException("fudgeContext must be set");
    }
    final JmsTemplate jmsTemplate = getJmsConnector().getJmsTemplateTopic();
    JmsByteArrayRequestSender requestSender = new JmsByteArrayRequestSender(getRequestTopic(), jmsTemplate);
    ByteArrayFudgeRequestSender fudgeRequestSender = new ByteArrayFudgeRequestSender(requestSender, getFudgeContext());
    return new RemoteReferenceDataProvider(fudgeRequestSender, getFudgeContext());
  }

}
