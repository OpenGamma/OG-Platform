/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.transport.jaxrs;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.fudgemsg.FudgeContext;

import com.opengamma.transport.EndPointDescriptionProvider;
import com.opengamma.util.SingletonFactoryBean;

/**
 * Spring factory bean for {@link RestTarget}
 */
public class RestTargetFactoryBean extends SingletonFactoryBean<RestTarget> {

  private static final ExecutorService DEFAULT_EXECUTOR = Executors.newCachedThreadPool();

  private FudgeContext _fudgeContext;
  private EndPointDescriptionProvider _endPointDescriptionProvider;
  private ExecutorService _executorService;
  
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  public EndPointDescriptionProvider getEndPointDescriptionProvider() {
    return _endPointDescriptionProvider;
  }
  
  public void setEndPointDescriptionProvider(EndPointDescriptionProvider provider) {
    _endPointDescriptionProvider = provider;
  }
  
  public ExecutorService getExecutorService() {
    return _executorService;
  }

  public void setExecutorService(final ExecutorService executorService) {
    _executorService = executorService;
  }

  @Override
  protected RestTarget createObject() {
    if (getFudgeContext() == null) {
      throw new IllegalArgumentException("fudgeContext must be set");
    }
    if (getEndPointDescriptionProvider() == null) {
      throw new IllegalArgumentException("endPointDescriptionProvider must be set");
    }
    ExecutorService executor = getExecutorService();
    if (executor == null) {
      executor = DEFAULT_EXECUTOR;
    }
    return new RestTarget(executor, getFudgeContext(), getEndPointDescriptionProvider());
  }

}
