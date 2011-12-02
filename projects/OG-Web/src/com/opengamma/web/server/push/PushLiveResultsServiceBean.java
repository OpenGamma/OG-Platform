/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import org.fudgemsg.FudgeContext;
import org.springframework.web.context.ServletContextAware;

import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;

/**
 * Spring helper for {@link PushLiveResultsService}.
 * TODO is this necessary any more?
 */
public class PushLiveResultsServiceBean implements ServletContextAware {
  
  private ServletContext _servletContext;
  private ViewProcessor _viewProcessor;
  private MarketDataSnapshotMaster _snapshotMaster;
  private UserPrincipal _user;
  private ExecutorService _executorService;
  private FudgeContext _fudgeContext;
  private PushLiveResultsService _liveResultsService;
  
  public PushLiveResultsServiceBean() {
  }
  
  public void setViewProcessor(ViewProcessor viewProcessor) {
    _viewProcessor = viewProcessor;
  }
  
  protected ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }
  
  public void setSnapshotMaster(MarketDataSnapshotMaster snapshotMaster) {
    _snapshotMaster = snapshotMaster;
  }
  
  public MarketDataSnapshotMaster getSnapshotMaster() {
    return _snapshotMaster;
  }
  
  protected UserPrincipal getUser() {
    return _user;
  }

  public void setUser(UserPrincipal user) {
    _user = user;
  }

  @Override
  public void setServletContext(final ServletContext servletContext) {
    _servletContext = servletContext;
  }
  
  public ExecutorService getExecutorService() {
    return _executorService;
  }
  
  public void setExecutorService(ExecutorService executorService) {
    _executorService = executorService;
  }
  
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  public void setFudgeContext(FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }
  
  public PushLiveResultsService getLiveResultsService() {
    return _liveResultsService;
  }
  
  public void afterPropertiesSet() {
    if (getViewProcessor() == null) {
      throw new IllegalStateException("View processor not set");
    }
    if (getFudgeContext() == null) {
      throw new IllegalArgumentException("Fudge context not set");
    }
    // TODO this is a hack at the moment; there should be a life cycle method on the bean from the container that we start and stop the service from ...
    _liveResultsService = createLiveResultsService();
  }
  
  public PushLiveResultsService createLiveResultsService() {
    return new PushLiveResultsService(getViewProcessor(), getUser(), getFudgeContext());
  }
 
}
