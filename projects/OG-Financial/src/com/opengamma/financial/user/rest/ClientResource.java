/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opengamma.financial.analytics.ircurve.rest.InterpolatedYieldCurveDefinitionMasterResource;
import com.opengamma.financial.marketdatasnapshot.rest.MarketDataSnapshotMasterResource;
import com.opengamma.financial.security.rest.SecurityMasterResource;
import com.opengamma.financial.user.UserInterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.user.UserManageableViewDefinitionRepository;
import com.opengamma.financial.user.UserPortfolioMaster;
import com.opengamma.financial.user.UserPositionMaster;
import com.opengamma.financial.user.UserSecurityMaster;
import com.opengamma.financial.user.UserSnapshotMaster;
import com.opengamma.financial.view.rest.DataManageableViewDefinitionRepositoryResource;
import com.opengamma.master.portfolio.impl.DataPortfoliosResource;
import com.opengamma.master.position.impl.DataPositionsResource;

/**
 * RESTful resource representing a user's client session.
 */
public class ClientResource {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ClientResource.class);

  /**
   * The path used to retrieve user portfolios.
   */
  public static final String PORTFOLIOS_PATH = "prtMaster"; // Note that the RemotePositionMaster is hard coded to assume this is always "prtMaster" from the URI given to it
  /**
   * The path used to retrieve user positions
   */
  public static final String POSITIONS_PATH = "posMaster"; // Note that the RemotePositionMaster is hard coded to assume this is always "posMaster" from the URI given to it
  /**
   * The path used to retrieve user securities
   */
  public static final String SECURITIES_PATH = "securities";
  /**
   * The path used to retrieve user view definitions
   */
  public static final String VIEW_DEFINITIONS_PATH = "viewDefinitions";
  /**
   * The path used to retrieve yield curve definitions
   */
  public static final String INTERPOLATED_YIELD_CURVE_DEFINITIONS_PATH = "interpolatedYieldCurveDefinitions";
  /**
   * The path used to retrieve user snapshots
   */
  public static final String MARKET_DATA_SNAPSHOTS_PATH = "snapshots";
  /**
   * The path used to signal a heartbeat if no actual transactions are being done
   */
  public static final String HEARTBEAT_PATH = "heartbeat";
  
  private DataPortfoliosResource _portfolioMaster;
  private DataPositionsResource _positionMaster;
  private SecurityMasterResource _securitiesResource;
  private DataManageableViewDefinitionRepositoryResource _viewDefinitionsResource;
  private InterpolatedYieldCurveDefinitionMasterResource _interpolatedYieldCurveDefinitionsResource;
  private MarketDataSnapshotMasterResource _snapshotMaster;
  private final String _clientName;
  private final String _userName;
  private final UserResourceData _data;

  /**
   * Contains the timestamp of the last time a resource was requested.
   */
  private volatile long _lastAccessed;

  public ClientResource(final String userName, final String clientName, final UserResourceData data) {
    _lastAccessed = System.currentTimeMillis();
    _clientName = clientName;
    _userName = userName;
    _data = data;
  }

  public FudgeContext getFudgeContext() {
    UsersResourceContext context = _data.getContext();
    return context.getFudgeContext();
  }
  
  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return _data.getUriInfo();
  }
  
  public String getClientName() {
    return _clientName;
  }

  public String getUserName() {
    return _userName;
  }


  public long getLastAccessed() {
    return _lastAccessed;
  }

  /*
   * Note that the methods below aren't synchronized. It does not matter if multiple objects get created if multiple threads
   * make the initial call concurrently. It does not matter which object remains in use or if different threads use different
   * objects. The references are only held to minimise the object creation overhead - the objects have no state.
   */

  @Path(PORTFOLIOS_PATH)
  public DataPortfoliosResource getPortfolios() {
    _lastAccessed = System.currentTimeMillis();
    if (_portfolioMaster == null) {
      s_logger.debug("Creating UserPositionMaster for {}/{}", getUserName(), getClientName());
      UsersResourceContext context = _data.getContext();
      _portfolioMaster = new DataPortfoliosResource(new UserPortfolioMaster(getUserName(), getClientName(), _data.getUserDataTracker(), context.getPortfolioMaster()));
    }
    return _portfolioMaster;
  }

  @Path(POSITIONS_PATH)
  public DataPositionsResource getPositions() {
    _lastAccessed = System.currentTimeMillis();
    if (_positionMaster == null) {
      s_logger.debug("Creating UserPositionMaster for {}/{}", getUserName(), getClientName());
      UsersResourceContext context = _data.getContext();
      _positionMaster = new DataPositionsResource(new UserPositionMaster(getUserName(), getClientName(), _data.getUserDataTracker(), context.getPositionMaster()));
    }
    return _positionMaster;
  }

  @Path(SECURITIES_PATH)
  public SecurityMasterResource getSecurities() {
    _lastAccessed = System.currentTimeMillis();
    if (_securitiesResource == null) {
      s_logger.debug("Creating UserSecurityMaster for {}/{}", getUserName(), getClientName());
      UsersResourceContext context = _data.getContext();
      _securitiesResource = new SecurityMasterResource(new UserSecurityMaster(getUserName(), getClientName(), _data.getUserDataTracker(), context.getSecurityMaster()),
          getFudgeContext());
    }
    return _securitiesResource;
  }

  @Path(VIEW_DEFINITIONS_PATH)
  public DataManageableViewDefinitionRepositoryResource getViewDefinitions() {
    _lastAccessed = System.currentTimeMillis();
    if (_viewDefinitionsResource == null) {
      s_logger.debug("Creating UserViewDefinitionRepository for {}/{}", getUserName(), getClientName());
      UsersResourceContext context = _data.getContext();
      _viewDefinitionsResource = new DataManageableViewDefinitionRepositoryResource(
          new UserManageableViewDefinitionRepository(getUserName(), getClientName(),
              _data.getUserDataTracker(), context.getViewDefinitionRepository()));
    }
    return _viewDefinitionsResource;
  }

  @Path(INTERPOLATED_YIELD_CURVE_DEFINITIONS_PATH)
  public InterpolatedYieldCurveDefinitionMasterResource getInterpolatedYieldCurveDefinitions() {
    _lastAccessed = System.currentTimeMillis();
    if (_interpolatedYieldCurveDefinitionsResource == null) {
      s_logger.debug("Creating UserYieldCurveDefinitionMaster for {}/{}", getUserName(), getClientName());
      UsersResourceContext context = _data.getContext();
      _interpolatedYieldCurveDefinitionsResource = new InterpolatedYieldCurveDefinitionMasterResource(new UserInterpolatedYieldCurveDefinitionMaster(getUserName(), getClientName(),
          _data.getUserDataTracker(), context.getInterpolatedYieldCurveDefinitionMaster()), getFudgeContext());
    }
    return _interpolatedYieldCurveDefinitionsResource;
  }

  @Path(MARKET_DATA_SNAPSHOTS_PATH)
  public MarketDataSnapshotMasterResource getSnapshots() {
    _lastAccessed = System.currentTimeMillis();
    if (_snapshotMaster == null) {
      s_logger.debug("Creating UserSnapshotMaster for {}/{}", getUserName(), getClientName());
      UsersResourceContext context = _data.getContext();
      UserSnapshotMaster userMaster = new UserSnapshotMaster(getUserName(), getClientName(), _data.getUserDataTracker(), context.getSnapshotMaster());
      _snapshotMaster = new MarketDataSnapshotMasterResource(userMaster, context.getFudgeContext());
    }
    return _snapshotMaster;
  }
  
  @POST
  @Path(HEARTBEAT_PATH)
  public void heartbeat() {
    s_logger.debug("Heartbeat received from {}/{}", getUserName(), getClientName());
    _lastAccessed = System.currentTimeMillis();
  }

}
