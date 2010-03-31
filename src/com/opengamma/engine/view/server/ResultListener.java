/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.view.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeSerializationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;

/* package */ class ResultListener implements ComputationResultListener, DeltaComputationResultListener {
  
  // TODO 2010-03-30 Andrew -- needs to give up if no clients are subscribing to a topic (and get itself unregistered with the underlying View)
  
  private static final Logger s_logger = LoggerFactory.getLogger(ResultListener.class);
  
  private ViewProcessorResource _viewProcessor;
  private JmsByteArrayMessageSender _computationResults;
  private JmsByteArrayMessageSender _deltaResults;
  
  public ResultListener (final ViewProcessorResource viewProcessor) {
    _viewProcessor = viewProcessor;
  }
  
  protected ViewProcessorResource getViewProcessor () {
    return _viewProcessor;
  }
  
  protected FudgeContext getFudgeContext () {
    return getViewProcessor ().getFudgeContext ();
  }
  
  protected FudgeSerializationContext getFudgeSerializationContext () {
    return new FudgeSerializationContext (getFudgeContext ());
  }
  
  @Override
  public void computationResultAvailable(ViewComputationResultModel resultModel) {
    s_logger.info ("Write {} to JMS {}", resultModel, _computationResults);
    _computationResults.send (getFudgeContext ().toByteArray (getFudgeSerializationContext ().objectToFudgeMsg (resultModel)));
  }

  @Override
  public void deltaResultAvailable(ViewDeltaResultModel deltaModel) {
    s_logger.info ("Write {} to JMS {}", deltaModel, _deltaResults);
    _deltaResults.send (getFudgeContext ().toByteArray (getFudgeSerializationContext ().objectToFudgeMsg (deltaModel)));
  }
  
  protected String getTopicName (final ViewClient viewClient, final String suffix) {
    return getViewProcessor ().getJmsTopicPrefix () + "-" + viewClient.getName () + "-" + suffix;
  }
  
  public synchronized String getComputationResultChannel (final ViewClient viewClient) {
    if (_computationResults == null) {
      final String topic = getTopicName (viewClient, "computation");
      s_logger.info ("Set up JMS {}", topic);
      _computationResults = new JmsByteArrayMessageSender (topic, getViewProcessor ().getJmsTemplate ());
      viewClient.addComputationResultListener (this);
    }
    return _computationResults.getDestinationName ();
  }
  
  public synchronized String getDeltaResultChannel (final ViewClient viewClient) {
    if (_deltaResults == null) {
      final String topic = getTopicName (viewClient, "delta");
      s_logger.info ("Set up JMS {}", topic);
      _deltaResults = new JmsByteArrayMessageSender (topic, getViewProcessor ().getJmsTemplate ());
      viewClient.addDeltaResultListener (this);
    }
    return _deltaResults.getDestinationName ();
  }
  
}