/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import javax.time.Instant;

import com.opengamma.engine.view.ViewResultModel;
import com.opengamma.engine.view.listener.JobResultReceivedCall;
import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.engine.view.ViewComputationResultModel;
import com.opengamma.engine.view.ViewDeltaResultModel;
import com.opengamma.engine.view.client.ViewClient;
import com.opengamma.engine.view.compilation.CompiledViewDefinition;
import com.opengamma.engine.view.execution.ViewCycleExecutionOptions;
import com.opengamma.engine.view.listener.CycleCompletedCall;
import com.opengamma.engine.view.listener.CycleExecutionFailedCall;
import com.opengamma.engine.view.listener.ProcessCompletedCall;
import com.opengamma.engine.view.listener.ProcessTerminatedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompilationFailedCall;
import com.opengamma.engine.view.listener.ViewDefinitionCompiledCall;
import com.opengamma.engine.view.listener.ViewResultListener;
import com.opengamma.financial.rest.AbstractJmsResultPublisher;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.util.jms.JmsConnector;

/**
 * Publishes {@code ViewClient} results over JMS.
 */
public class ViewClientJmsResultPublisher extends AbstractJmsResultPublisher implements ViewResultListener  {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(ViewClientJmsResultPublisher.class);

  /**
   * The view client.
   */
  private final ViewClient _viewClient;

  /**
   * Creates an instance.
   * 
   * @param viewClient  the view client, not null
   * @param fudgeContext  the Fudge context, not null
   * @param jmsConnector  the JMS connector, not null
   */
  public ViewClientJmsResultPublisher(ViewClient viewClient, FudgeContext fudgeContext, JmsConnector jmsConnector) {
    super(fudgeContext, jmsConnector);
    _viewClient = viewClient;
  }

  //-------------------------------------------------------------------------
  @Override
  protected void startListener() {
    s_logger.debug("Setting listener {} on view client {}'s results", this, _viewClient);
    _viewClient.setResultListener(this);
  }

  @Override
  protected void stopListener() {
    s_logger.debug("Removing listener {} on view client {}'s results", this, _viewClient);
    _viewClient.setResultListener(null);
  }

  //-------------------------------------------------------------------------
  @Override
  public UserPrincipal getUser() {
    return _viewClient.getUser();
  }

  @Override
  public void viewDefinitionCompiled(CompiledViewDefinition compiledViewDefinition, boolean hasMarketDataPermissions) {
    send(new ViewDefinitionCompiledCall(compiledViewDefinition, hasMarketDataPermissions));
  }  

  @Override
  public void viewDefinitionCompilationFailed(Instant valuationTime, Exception exception) {
    send(new ViewDefinitionCompilationFailedCall(valuationTime, exception));
  }

  @Override
  public void cycleCompleted(ViewComputationResultModel fullResult, ViewDeltaResultModel deltaResult) {
    send(new CycleCompletedCall(fullResult, deltaResult));
  }

  @Override
  public void cycleExecutionFailed(ViewCycleExecutionOptions executionOptions, Exception exception) {
    send(new CycleExecutionFailedCall(executionOptions, exception));
  }

  @Override
  public void jobResultReceived(ViewResultModel fullResult, ViewDeltaResultModel deltaResult) {
    send(new JobResultReceivedCall(fullResult, deltaResult));
  }

  @Override
  public void processCompleted() {
    send(new ProcessCompletedCall());
  }

  @Override
  public void processTerminated(boolean executionInterrupted) {
    send(new ProcessTerminatedCall(executionInterrupted));
  }

}
