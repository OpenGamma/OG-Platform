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
  // TODO 2010-05-14 Andrew -- needs to give up if there are no views using it any more (i.e. we're only referenced by the map in ViewProcessorResource) and release the JMS topics
  // TODO 2010-05-14 Andrew -- needs to detect errors with the JMS and do a graceful recovery (e.g. unregister itself; the client will recall the REST api and cause re-registration if it wants it)
  
  private static final Logger s_logger = LoggerFactory.getLogger(ResultListener.class);
  
  private ViewProcessorResource _viewProcessor;
  private JmsByteArrayMessageSender _computationResults;
  private JmsByteArrayMessageSender _deltaResults;
  
  public ResultListener(final ViewProcessorResource viewProcessor) {
    _viewProcessor = viewProcessor;
  }
  
  protected ViewProcessorResource getViewProcessor() {
    return _viewProcessor;
  }
  
  protected FudgeContext getFudgeContext() {
    return getViewProcessor().getFudgeContext();
  }
  
  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }
  
  @Override
  public void computationResultAvailable(ViewComputationResultModel resultModel) {
    s_logger.info("Write {} to JMS {}", resultModel, _computationResults);
    final byte[] fudgeMsg = getFudgeContext().toByteArray(getFudgeSerializationContext().objectToFudgeMsg(resultModel));
    s_logger.debug("Writing {} bytes data", fudgeMsg.length);
    _computationResults.send(fudgeMsg);
  }

  @Override
  public void deltaResultAvailable(ViewDeltaResultModel deltaModel) {
    s_logger.info("Write {} to JMS {}", deltaModel, _deltaResults);
    final byte[] fudgeMsg = getFudgeContext().toByteArray(getFudgeSerializationContext().objectToFudgeMsg(deltaModel));
    s_logger.debug("Writing {} bytes data", fudgeMsg.length);
    _deltaResults.send(fudgeMsg);
  }
  
  protected String getTopicName(final ViewClient viewClient, final String suffix) {
    final String topicName = getViewProcessor().getJmsTopicPrefix() + "-" + viewClient.getName() + "-" + suffix;
    return topicName;
  }
  
  public synchronized String getComputationResultChannel(final ViewClient viewClient) {
    if (_computationResults == null) {
      final String topic = getTopicName(viewClient, "computation");
      s_logger.info("Set up JMS {}", topic);
      _computationResults = new JmsByteArrayMessageSender(topic, getViewProcessor().getJmsTemplate());
    }
    s_logger.debug("Adding listener {} to view client {}'s computation result", this, viewClient);
    viewClient.addComputationResultListener(this);
    return _computationResults.getDestinationName();
  }
  
  public synchronized String getDeltaResultChannel(final ViewClient viewClient) {
    if (_deltaResults == null) {
      final String topic = getTopicName(viewClient, "delta");
      s_logger.info("Set up JMS {}", topic);
      _deltaResults = new JmsByteArrayMessageSender(topic, getViewProcessor().getJmsTemplate());
    }
    s_logger.debug("Adding listener {} to view client {}'s computation result", this, viewClient);
    viewClient.addDeltaResultListener(this);
    return _deltaResults.getDestinationName();
  }
  
  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder();
    sb.append("ResultListener");
    if (_deltaResults != null) {
      sb.append(" delta:").append(_deltaResults.getDestinationName());
    }
    if (_computationResults != null) {
      sb.append(" computation:").append(_computationResults.getDestinationName());
    }
    return sb.toString();
  }
  
}
