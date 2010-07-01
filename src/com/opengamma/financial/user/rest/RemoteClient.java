/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.view.server.EngineFudgeContextConfiguration;
import com.opengamma.financial.FinancialFudgeContextConfiguration;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.rest.RemoteManagablePositionMaster;
import com.opengamma.financial.security.ManagableSecurityMaster;
import com.opengamma.financial.security.rest.RemoteManagableSecurityMaster;
import com.opengamma.financial.view.ManagableViewDefinitionRepository;
import com.opengamma.financial.view.rest.RemoteManagableViewDefinitionRepository;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.GUIDGenerator;

/**
 * Provides access to a remote representation of a client
 */
public class RemoteClient {

  private final String _clientId;
  private final FudgeContext _fudgeContext;
  private final RestTarget _positionMasterTarget;
  private final RestTarget _securityMasterTarget;
  private final RestTarget _viewDefinitionRepositoryTarget;
  
  public RemoteClient(String clientId, FudgeContext fudgeContext, RestTarget baseTarget) {
    _clientId = clientId;
    _fudgeContext = fudgeContext;
    _positionMasterTarget = baseTarget.resolve(ClientResource.PORTFOLIOS_PATH);
    _securityMasterTarget = baseTarget.resolve(ClientResource.SECURITIES_PATH);
    _viewDefinitionRepositoryTarget = baseTarget.resolve(ClientResource.VIEW_DEFINITIONS_PATH);
  }
  
  public String getClientId() {
    return _clientId;
  }
  
  public ManagablePositionMaster getPositionMaster() {
    return new RemoteManagablePositionMaster(_fudgeContext, _positionMasterTarget);
  }
  
  public ManagableSecurityMaster getSecurityMaster() {
    return new RemoteManagableSecurityMaster(_fudgeContext, _securityMasterTarget);
  }
  
  public ManagableViewDefinitionRepository getViewDefinitionRepository() {
    return new RemoteManagableViewDefinitionRepository(_fudgeContext, _viewDefinitionRepositoryTarget);
  }
  
  /**
   * A hack to allow the Excel side to get hold of a RemoteClient without it having to be aware of the URI. Eventually
   * we will need a UserMaster to host users and their clients, and the entry point for Excel will be a
   * RemoteUserMaster.
   *
   * @param usersUri  uri as far as /users
   * @param username  the username
   * @return  a {@link RemoteClient} instance for the new client
   */
  public static RemoteClient forNewClient(RestTarget usersUri, String username) {
    FudgeContext fudgeContext = new FudgeContext();
    EngineFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    FinancialFudgeContextConfiguration.INSTANCE.configureFudgeContext(fudgeContext);
    // Just use a GUID for the client ID
    String clientId = GUIDGenerator.generate().toString();
    RestTarget uri = usersUri.resolveBase(username).resolveBase("clients").resolveBase(clientId);
    return new RemoteClient(clientId, fudgeContext, uri);
  }
  
}
