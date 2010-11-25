/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.analytics.ircurve.rest.RemoteInterpolatedYieldCurveDefinitionMaster;
import com.opengamma.financial.position.master.rest.RemotePositionMaster;
import com.opengamma.financial.security.rest.RemoteSecurityMaster;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
import com.opengamma.financial.view.rest.RemoteManagableViewDefinitionRepository;
import com.opengamma.master.position.PositionMaster;
import com.opengamma.master.security.SecurityMaster;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.GUIDGenerator;

/**
 * Provides access to a remote representation of a client
 */
public class RemoteClient {

  private final String _clientId;
  private final FudgeContext _fudgeContext;
  private final RestTarget _baseTarget;
  private final RestTarget _securityMasterTarget;
  private final RestTarget _viewDefinitionRepositoryTarget;
  private final RestTarget _interpolatedYieldCurveDefinitionMasterTarget;

  public RemoteClient(String clientId, FudgeContext fudgeContext, RestTarget baseTarget) {
    _clientId = clientId;
    _fudgeContext = fudgeContext;
    _baseTarget = baseTarget;
    _securityMasterTarget = baseTarget.resolveBase(ClientResource.SECURITIES_PATH);
    _viewDefinitionRepositoryTarget = baseTarget.resolveBase(ClientResource.VIEW_DEFINITIONS_PATH);
    _interpolatedYieldCurveDefinitionMasterTarget = baseTarget.resolveBase(ClientResource.INTERPOLATED_YIELD_CURVE_DEFINITIONS_PATH);
  }
  
  public String getClientId() {
    return _clientId;
  }
  
  public PositionMaster getPositionMaster() {
    return new RemotePositionMaster(_baseTarget.getURI());
  }

  public SecurityMaster getSecurityMaster() {
    return new RemoteSecurityMaster(_fudgeContext, _securityMasterTarget);
  }
  
  public ManageableViewDefinitionRepository getViewDefinitionRepository() {
    return new RemoteManagableViewDefinitionRepository(_fudgeContext, _viewDefinitionRepositoryTarget);
  }

  public InterpolatedYieldCurveDefinitionMaster getInterpolatedYieldCurveDefinitionMaster() {
    return new RemoteInterpolatedYieldCurveDefinitionMaster(_fudgeContext, _interpolatedYieldCurveDefinitionMasterTarget);
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
