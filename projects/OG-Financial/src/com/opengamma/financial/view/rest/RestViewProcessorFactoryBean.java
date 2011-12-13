/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.util.concurrent.ScheduledExecutorService;

import org.fudgemsg.FudgeContext;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.util.SingletonFactoryBean;
import com.opengamma.util.jms.JmsConnector;

/**
 * Spring helper for {@link DataViewProcessorsResource}.
 */
public class RestViewProcessorFactoryBean extends SingletonFactoryBean<DataViewProcessorsResource> {

  private ViewProcessor _viewProcessor;
  private JmsConnector _jmsConnector;
  private FudgeContext _fudgeContext;
  private ScheduledExecutorService _scheduler;

  public ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }

  public void setViewProcessor(ViewProcessor viewProcessor) {
    _viewProcessor = viewProcessor;
  }

  public JmsConnector getJmsConnector() {
    return _jmsConnector;
  }

  public void setJmsConnector(JmsConnector jmsConnector) {
    _jmsConnector = jmsConnector;
  }
  
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  public ScheduledExecutorService getScheduler() {
    return _scheduler;
  }
  
  public void setExecutorService(ScheduledExecutorService scheduler) {
    _scheduler = scheduler;
  }

  //-------------------------------------------------------------------------
  @Override
  protected DataViewProcessorsResource createObject() {
    if (_viewProcessor == null) {
      throw new OpenGammaRuntimeException("The viewProcessor property must be set");
    }
    if (_jmsConnector == null) {
      throw new OpenGammaRuntimeException("The connectionFactory property must be set");
    }
    if (_scheduler == null) {
      throw new OpenGammaRuntimeException("The scheduler property must be set");
    }
    
    return new DataViewProcessorsResource(getViewProcessor(), getJmsConnector(), getFudgeContext(), getScheduler());
  }

}
