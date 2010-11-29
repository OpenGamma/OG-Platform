/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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
import com.opengamma.financial.position.master.rest.DataPortfolioTreesResource;
import com.opengamma.financial.position.master.rest.DataPositionsResource;
import com.opengamma.financial.security.rest.SecurityMasterResource;
import com.opengamma.financial.user.UserInterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.user.UserManageableViewDefinitionRepository;
import com.opengamma.financial.user.UserPositionMaster;
import com.opengamma.financial.user.UserSecurityMaster;
import com.opengamma.financial.view.rest.ViewDefinitionsResource;
import com.opengamma.master.position.PositionMaster;

/**
 * RESTful resource representing a user's client session.
 */
public class ClientResource {
  
  private static final Logger s_logger = LoggerFactory.getLogger(ClientResource.class);

  /**
   * The path used to retrieve user portfolios
   */
  public static final String PORTFOLIOS_PATH = "portfoliotrees";
  /**
   * The path used to retrieve user positions
   */
  public static final String POSITIONS_PATH = "positions";
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
   * The path used to signal a heartbeat if no actual transactions are being done
   */
  public static final String HEARTBEAT_PATH = "heartbeat";
  
  private final ClientsResource _clientsResource;
  private final String _clientName;
  private final UsersResourceContext _usersResourceContext;
  
  private PositionMaster _positionMaster;
  private SecurityMasterResource _securitiesResource;
  private ViewDefinitionsResource _viewDefinitionsResource;
  private InterpolatedYieldCurveDefinitionMasterResource _interpolatedYieldCurveDefinitionsResource;

  /**
   * Contains the timestamp of the last time a resource was requested.
   */
  private volatile long _lastAccessed;

  public ClientResource(ClientsResource clientsResource, String clientName, UsersResourceContext context) {
    _clientsResource = clientsResource;
    _clientName = clientName;
    _usersResourceContext = context;
    _lastAccessed = System.currentTimeMillis();
  }

  public FudgeContext getFudgeContext() {
    return _usersResourceContext.getFudgeContext();
  }

  /**
   * Gets the URI info.
   * @return the uri info, not null
   */
  public UriInfo getUriInfo() {
    return getClientsResource().getUriInfo();
  }
  
  public String getClientName() {
    return _clientName;
  }

  public String getUserName() {
    return getClientsResource().getUserResource().getUserName();
  }

  public ClientsResource getClientsResource() {
    return _clientsResource;
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
  public DataPortfolioTreesResource getPortfolios() {
    _lastAccessed = System.currentTimeMillis();
    if (_positionMaster == null) {
      s_logger.debug("Creating UserPositionMaster for {}/{}", getUserName(), getClientName());
      _positionMaster = new UserPositionMaster(getUserName(), getClientName(), _usersResourceContext.getDataTracker(), _usersResourceContext.getPositionMaster());
    }
    return new DataPortfolioTreesResource(_positionMaster);
  }
  
  @Path(POSITIONS_PATH)
  public DataPositionsResource getPositions() {
    _lastAccessed = System.currentTimeMillis();
    if (_positionMaster == null) {
      s_logger.debug("Creating UserPositionMaster for {}/{}", getUserName(), getClientName());
      _positionMaster = new UserPositionMaster(getUserName(), getClientName(), _usersResourceContext.getDataTracker(), _usersResourceContext.getPositionMaster());
    }
    return new DataPositionsResource(_positionMaster);
  }
  
  @Path(SECURITIES_PATH)
  public SecurityMasterResource getSecurities() {
    _lastAccessed = System.currentTimeMillis();
    if (_securitiesResource == null) {
      s_logger.debug("Creating UserSecurityMaster for {}/{}", getUserName(), getClientName());
      _securitiesResource = new SecurityMasterResource(new UserSecurityMaster(getUserName(), getClientName(), _usersResourceContext.getDataTracker(), _usersResourceContext.getSecurityMaster()),
          getFudgeContext());
    }
    return _securitiesResource;
  }
  
  @Path(VIEW_DEFINITIONS_PATH)
  public ViewDefinitionsResource getViewDefinitions() {
    _lastAccessed = System.currentTimeMillis();
    if (_viewDefinitionsResource == null) {
      s_logger.debug("Creating UserViewDefinitionRepository for {}/{}", getUserName(), getClientName());
      _viewDefinitionsResource = new ViewDefinitionsResource(new UserManageableViewDefinitionRepository(getUserName(), getClientName(), _usersResourceContext.getDataTracker(), _usersResourceContext
          .getViewDefinitionRepository()),
          getFudgeContext());
    }
    return _viewDefinitionsResource;
  }
  
  @Path(INTERPOLATED_YIELD_CURVE_DEFINITIONS_PATH)
  public InterpolatedYieldCurveDefinitionMasterResource getInterpolatedYieldCurveDefinitions() {
    _lastAccessed = System.currentTimeMillis();
    if (_interpolatedYieldCurveDefinitionsResource == null) {
      s_logger.debug("Creating UserYieldCurveDefinitionMaster for {}/{}", getUserName(), getClientName());
      _interpolatedYieldCurveDefinitionsResource = new InterpolatedYieldCurveDefinitionMasterResource(new UserInterpolatedYieldCurveDefinitionMaster(getUserName(), getClientName(),
          _usersResourceContext.getDataTracker(), _usersResourceContext.getInterpolatedYieldCurveDefinitionMaster()), getFudgeContext());
    }
    return _interpolatedYieldCurveDefinitionsResource;
  }

  @POST
  @Path(HEARTBEAT_PATH)
  public void heartbeat() {
    s_logger.debug("Heartbeat received from {}/{}", getUserName(), getClientName());
    _lastAccessed = System.currentTimeMillis();
  }

}
