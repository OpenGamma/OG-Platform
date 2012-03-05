/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;

import org.cometd.Bayeux;
import org.fudgemsg.FudgeContext;

import com.opengamma.core.position.PositionSource;
import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.view.ViewProcessor;
import com.opengamma.financial.aggregation.PortfolioAggregationFunctions;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.livedata.UserPrincipal;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;

/**
 * Spring helper for {@link LiveResultsService}.
 */
public class LiveResultsServiceBean {

  private Bayeux _bayeux;
  private ViewProcessor _viewProcessor;
  private PositionSource _positionSource;
  private SecuritySource _securitySource;
  private PortfolioMaster _userPortfolioMaster;
  private PositionMaster _userPositionMaster;
  private ManageableViewDefinitionRepository _userViewDefinitionRepository;
  private PortfolioAggregationFunctions _portfolioAggregators;
  private MarketDataSnapshotMaster _snapshotMaster;
  private UserPrincipal _user;
  private ExecutorService _executorService;
  private FudgeContext _fudgeContext;
  private LiveResultsService _liveResultsService;

  public LiveResultsServiceBean() {
  }

  public void setViewProcessor(ViewProcessor viewProcessor) {
    _viewProcessor = viewProcessor;
  }
  
  protected ViewProcessor getViewProcessor() {
    return _viewProcessor;
  }
  
  public PositionSource getPositionSource() {
    return _positionSource;
  }
  
  public void setPositionSource(PositionSource positionSource) {
    _positionSource = positionSource;
  }
  
  public SecuritySource getSecuritySource() {
    return _securitySource;
  }
  
  public void setSecuritySource(SecuritySource securitySource) {
    _securitySource = securitySource;
  }
  
  public PortfolioMaster getUserPortfolioMaster() {
    return _userPortfolioMaster;
  }

  public void setUserPortfolioMaster(PortfolioMaster userPortfolioMaster) {
    _userPortfolioMaster = userPortfolioMaster;
  }
  
  public PositionMaster getUserPositionMaster() {
    return _userPositionMaster;
  }
  
  public void setUserPositionMaster(PositionMaster userPositionMaster) {
    _userPositionMaster = userPositionMaster;
  }

  public ManageableViewDefinitionRepository getUserViewDefinitionRepository() {
    return _userViewDefinitionRepository;
  }

  public void setUserViewDefinitionRepository(ManageableViewDefinitionRepository userViewDefinitionRepository) {
    _userViewDefinitionRepository = userViewDefinitionRepository;
  }

  public PortfolioAggregationFunctions getPortfolioAggregators() {
    return _portfolioAggregators;
  }

  public void setPortfolioAggregators(PortfolioAggregationFunctions portfolioAggregators) {
    _portfolioAggregators = portfolioAggregators;
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

  protected void setBayeux(final Bayeux bayeux) {
    _bayeux = bayeux;
  }
  
  protected Bayeux getBayeux() {
    return _bayeux;
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
  
  public LiveResultsService getLiveResultsService() {
    return _liveResultsService;
  }

  //-------------------------------------------------------------------------
  /**
   * Initialises the service.
   * 
   * @param servletContext  the servlet context, not null
   */
  public void init(ServletContext servletContext) {
    setBayeux((Bayeux) servletContext.getAttribute(Bayeux.ATTRIBUTE));
    init();
  }

  /**
   * Initializes the service.
   * 
   * @param bayeux  the bayeux instance, not null
   */
  public void init(Bayeux bayeux) {
    setBayeux(bayeux);
    init();
  }

  /**
   * Initializes the service, where the Bayeux instance must be set.
   */
  protected void init() {
    if (getViewProcessor() == null) {
      throw new IllegalStateException("View processor not set");
    }
    if (getBayeux() == null) {
      throw new IllegalStateException("Bayeux not set");
    }
    if (getFudgeContext() == null) {
      throw new IllegalArgumentException("Fudge context not set");
    }
    // TODO this is a hack at the moment; there should be a life cycle method on the bean from the container that we start and stop the service from ...
    _liveResultsService = createLiveResultsService();
  }

  protected LiveResultsService createLiveResultsService() {
    return new LiveResultsService(getBayeux(), getViewProcessor(), getPositionSource(), getSecuritySource(),
        getUserPortfolioMaster(), getUserPositionMaster(), getUserViewDefinitionRepository(), getSnapshotMaster(),
        getUser(), getExecutorService(), getFudgeContext(), getViewProcessor().getNamedMarketDataSpecificationRepository(),
        getPortfolioAggregators());
  }

}
