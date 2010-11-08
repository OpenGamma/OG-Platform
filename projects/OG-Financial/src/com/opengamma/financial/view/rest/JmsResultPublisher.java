/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.concurrent.locks.ReentrantLock;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;
import com.opengamma.transport.jms.JmsByteArrayMessageSenderService;

/**
 * 
 */
public class JmsResultPublisher implements ComputationResultListener, DeltaComputationResultListener {

  private static final Logger s_logger = LoggerFactory.getLogger(JmsResultPublisher.class);
  
  private final ViewClient _viewClient;
  private final FudgeContext _fudgeContext;
  private final FudgeSerializationContext _fudgeSerializationContext;
  private final String _topicPrefix;
  private final JmsByteArrayMessageSenderService _messageSenderService;
  private final ReentrantLock _configLock = new ReentrantLock();
  
  private JmsByteArrayMessageSender _resultSender;
  private JmsByteArrayMessageSender _deltaSender;

  public JmsResultPublisher(ViewClient viewClient, FudgeContext fudgeContext, String topicPrefix, JmsByteArrayMessageSenderService messageSenderService) {
    _viewClient = viewClient;
    _fudgeContext = fudgeContext;
    _fudgeSerializationContext = new FudgeSerializationContext(fudgeContext);
    _topicPrefix = topicPrefix;
    _messageSenderService = messageSenderService;
  }
  
  public String startPublishingResults() {
    _configLock.lock();
    try {
      if (_resultSender == null) {
        String topic = getTopicName(_viewClient, "computation");
        s_logger.info("Set up JMS {}", topic);
        _resultSender = _messageSenderService.getMessageSender(topic);
      }
      s_logger.debug("Setting listener {} on view client {}'s computation result", this, _viewClient);
      _viewClient.setResultListener(this);
      return _resultSender.getDestinationName();
    } finally {
      _configLock.unlock();
    }
  }
  
  public void stopPublishingResults() {
    _configLock.lock();
    try {
      s_logger.debug("Removing listener {} from view client {}'s computation result", this, _viewClient);
      _viewClient.setResultListener(null);
      _resultSender = null;
    } finally {
      _configLock.unlock();
    }
  }
  
  public String startPublishingDeltas() {
    _configLock.lock();
    try {
      if (_deltaSender == null) {
        String topic = getTopicName(_viewClient, "delta");
        s_logger.info("Set up JMS {}", topic);
        // [ENG-107] the message sender job drops stuff if the JMS can't keep up, which may be a problem here
        _deltaSender = _messageSenderService.getMessageSender(topic);
      }
      s_logger.debug("Setting listener {} on view client {}'s delta result", this, _viewClient);
      _viewClient.setDeltaResultListener(this);
      return _deltaSender.getDestinationName();
    } finally {
      _configLock.unlock();
    }
  }
  
  public void stopPublishingDeltas() {
    _configLock.lock();
    try {
      s_logger.debug("Removing listener {} from view client {}'s computation result", this, _viewClient);
      _viewClient.setDeltaResultListener(null);
      _deltaSender = null;
    } finally {
      _configLock.unlock();
    }
  }
  
  @Override
  public UserPrincipal getUser() {
    return _viewClient.getUser();
  }
  
  @Override
  public void computationResultAvailable(ViewComputationResultModel resultModel) {
    s_logger.info("Write {} to JMS {}", resultModel, _resultSender);
    FudgeFieldContainer resultModelMsg = _fudgeSerializationContext.objectToFudgeMsg(resultModel);
    s_logger.debug("Results in Fudge msg {}", resultModelMsg);
    byte[] fudgeMsg = _fudgeContext.toByteArray(resultModelMsg);
    s_logger.debug("Writing {} bytes data", fudgeMsg.length);
    _resultSender.send(fudgeMsg);
  }

  @Override
  public void deltaResultAvailable(ViewDeltaResultModel deltaModel) {
    s_logger.info("Write {} to JMS {}", deltaModel, _deltaSender);
    byte[] fudgeMsg = _fudgeContext.toByteArray(_fudgeSerializationContext.objectToFudgeMsg(deltaModel));
    s_logger.debug("Writing {} bytes data", fudgeMsg.length);
    _deltaSender.send(fudgeMsg);
  }
  
  private String getTopicName(ViewClient viewClient, String suffix) {
    return _topicPrefix + "-" + viewClient.getUniqueIdentifier() + "-" + suffix;
  }
  
}
