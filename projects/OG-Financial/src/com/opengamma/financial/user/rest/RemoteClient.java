/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.portfolio.rest.RemotePortfolioMaster;
import com.opengamma.financial.position.rest.RemotePositionMaster;
import com.opengamma.financial.security.rest.RemoteSecurityMaster;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.rest.RemoteManagableViewDefinitionRepository;
import com.opengamma.master.portfolio.PortfolioMaster;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.GUIDGenerator;

/**
 * Provides access to a remote representation of a client
 */
public class RemoteClient {

  private final String _clientId;
  private final FudgeContext _fudgeContext;
  private final RestTarget _baseTarget;
  private PortfolioMaster _portfolioMaster;
  private PositionMaster _positionMaster;
  private SecurityMaster _securityMaster;
  private ManageableViewDefinitionRepository _viewDefinitionRepository;
  private InterpolatedYieldCurveDefinitionMaster _interpolatedYieldCurveDefinitionMaster;

  public RemoteClient(String clientId, FudgeContext fudgeContext, RestTarget baseTarget) {
    _clientId = clientId;
    _fudgeContext = fudgeContext;
    _baseTarget = baseTarget;
  }

  public String getClientId() {
    return _clientId;
  }

  public PortfolioMaster getPortfolioMaster() {
    if (_portfolioMaster == null) {
      _portfolioMaster = new RemotePortfolioMaster(_baseTarget.getURI());
    }
    return _portfolioMaster;
  }

  public PositionMaster getPositionMaster() {
    if (_positionMaster == null) {
      _positionMaster = new RemotePositionMaster(_baseTarget.getURI());
    }
    return _positionMaster;
  }

  public SecurityMaster getSecurityMaster() {
    if (_securityMaster == null) {
      _securityMaster = new RemoteSecurityMaster(_fudgeContext, _baseTarget.resolveBase(ClientResource.SECURITIES_PATH));
    }
    return _securityMaster;
  }

  public ManageableViewDefinitionRepository getViewDefinitionRepository() {
    if (_viewDefinitionRepository == null) {
      _viewDefinitionRepository = new RemoteManagableViewDefinitionRepository(_fudgeContext, _baseTarget.resolveBase(ClientResource.VIEW_DEFINITIONS_PATH));
    }
    return _viewDefinitionRepository;
  }

  public InterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster() {
    if (_interpolatedYieldCurveDefinitionMaster == null) {
      _interpolatedYieldCurveDefinitionMaster = new RemoteInterpolatedYieldCurveDefinitionMaster(_fudgeContext, _baseTarget.resolveBase(ClientResource.INTERPOLATED_YIELD_CURVE_DEFINITIONS_PATH));
    }
    return _interpolatedYieldCurveDefinitionMaster;
  }

  /**
   * Creates a heartbeat sender. If nothing has happened for a timeout duration, that would result in messages being sent to the server,
   * the heartbeat signal should be sent as a keep-alive.
   * 
   * @return a runnable sender. Each invocation of {@link Runnable#run} will send a heartbeat signal
   */
  public Runnable createHeartbeatSender() {
    final RestClient client = RestClient.getInstance(_fudgeContext, null);
    final RestTarget target = _baseTarget.resolve(ClientResource.HEARTBEAT_PATH);
    return new Runnable() {
      @Override
      public void run() {
        client.post(target);
      }
    };
  }

  /**
   * A hack to allow the Excel side to get hold of a RemoteClient without it having to be aware of the URI. Eventually
   * we will need a UserMaster to host users and their clients, and the entry point for Excel will be a
   * RemoteUserMaster.
   *
   * @param fudgeContext  the Fudge context
   * @param usersUri  uri as far as /users
   * @param username  the username
   * @return  a {@link RemoteClient} instance for the new client
   */
  public static RemoteClient forNewClient(FudgeContext fudgeContext, RestTarget usersUri, String username) {
    return forClient(fudgeContext, usersUri, username, GUIDGenerator.generate().toString());
  }

  public static RemoteClient forClient(FudgeContext fudgeContext, RestTarget usersUri, String username, String clientId) {
    RestTarget uri = usersUri.resolveBase(username).resolveBase("clients").resolveBase(clientId);
    return new RemoteClient(clientId, fudgeContext, uri);
  }

}
