/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.user.ClientTracker;
import com.opengamma.financial.user.DummyTracker;
import com.opengamma.financial.user.UserDataTracker;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;

/**
 * Context/configuration for the objects to pass around.
 */
public class UsersResourceContext {

  private FudgeContext _fudgeContext;
  private UserDataTracker _dataTracker;
  private ClientTracker _clientTracker;
  private PortfolioMaster _userPortfolioMaster;
  private PositionMaster _userPositionMaster;
  private SecurityMaster _userSecurityMaster;
  private ManageableViewDefinitionRepository _userViewDefinitionRepository;
  private InterpolatedYieldCurveDefinitionMaster _userInterpolatedYieldCurveDefinitionMaster;
  private MarketDataSnapshotMaster _userSnapshotMaster;

  public UsersResourceContext() {
    final DummyTracker tracker = new DummyTracker();
    setDataTracker(tracker);
    setClientTracker(tracker);
  }
  
  public void setDataTracker(UserDataTracker dataTracker) {
    _dataTracker = dataTracker;
  }

  public UserDataTracker getDataTracker() {
    return _dataTracker;
  }

  public void setClientTracker(ClientTracker clientTracker) {
    _clientTracker = clientTracker;
  }

  public ClientTracker getClientTracker() {
    return _clientTracker;
  }

  public void setUserPortfolioMaster(PortfolioMaster portfolioMaster) {
    _userPortfolioMaster = portfolioMaster;
  }
  
  public PortfolioMaster getPortfolioMaster() {
    return _userPortfolioMaster;
  }

  public void setUserPositionMaster(PositionMaster positionMaster) {
    _userPositionMaster = positionMaster;
  }
  
  public PositionMaster getPositionMaster() {
    return _userPositionMaster;
  }

  public void setUserInterpolatedYieldCurveDefinitionMaster(final InterpolatedYieldCurveDefinitionMaster userInterpolatedYieldCurveDefinitionMaster) {
    _userInterpolatedYieldCurveDefinitionMaster = userInterpolatedYieldCurveDefinitionMaster;
  }

  public InterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster() {
    return _userInterpolatedYieldCurveDefinitionMaster;
  }

  public void setUserSecurityMaster(SecurityMaster securityMaster) {
    _userSecurityMaster = securityMaster;
  }

  public SecurityMaster getSecurityMaster() {
    return _userSecurityMaster;
  }

  public void setUserViewDefinitionRepository(ManageableViewDefinitionRepository viewDefinitionRepository) {
    _userViewDefinitionRepository = viewDefinitionRepository;
  }

  public ManageableViewDefinitionRepository getViewDefinitionRepository() {
    return _userViewDefinitionRepository;
  }
  
  public void setUserSnapshotMaster(MarketDataSnapshotMaster snapshotMaster) {
    _userSnapshotMaster = snapshotMaster;
  }
  
  public MarketDataSnapshotMaster getSnapshotMaster() {
    return _userSnapshotMaster;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

}
