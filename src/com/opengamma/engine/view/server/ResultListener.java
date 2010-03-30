/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */

package com.opengamma.engine.view.server;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.engine.view.ComputationResultListener;
import com.opengamma.engine.view.DeltaComputationResultListener;
import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.transport.jms.JmsByteArrayMessageSender;

/* package */ class ResultListener implements ComputationResultListener, DeltaComputationResultListener {
  
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
    System.out.println ("Write " + resultModel + " to JMS topic " + _computationResults);
    _computationResults.send (getFudgeContext ().toByteArray (getFudgeSerializationContext ().objectToFudgeMsg (resultModel)));
  }

  @Override
  public void deltaResultAvailable(ViewDeltaResultModel deltaModel) {
    System.out.println ("Write " + deltaModel + " to JMS topic " + _deltaResults);
    _deltaResults.send (getFudgeContext ().toByteArray (getFudgeSerializationContext ().objectToFudgeMsg (deltaModel)));
  }
  
  protected String getTopicName (final ViewClient viewClient, final String suffix) {
    return getViewProcessor ().getJmsTopicPrefix () + "-" + viewClient.getName () + "-" + suffix;
  }
  
  public synchronized String getComputationResultChannel (final ViewClient viewClient) {
    if (_computationResults == null) {
      final String topic = getTopicName (viewClient, "computation");
      System.out.println ("Set up JMS topic " + topic);
      _computationResults = new JmsByteArrayMessageSender (topic, getViewProcessor ().getJmsTemplate ());
      viewClient.addComputationResultListener (this);
    }
    return _computationResults.getDestinationName ();
  }
  
  public synchronized String getDeltaResultChannel (final ViewClient viewClient) {
    if (_deltaResults == null) {
      final String topic = getTopicName (viewClient, "delta");
      System.out.println ("Set up JMS topic " + topic);
      _deltaResults = new JmsByteArrayMessageSender (topic, getViewProcessor ().getJmsTemplate ());
      viewClient.addDeltaResultListener (this);
    }
    return _deltaResults.getDestinationName ();
  }
  
}