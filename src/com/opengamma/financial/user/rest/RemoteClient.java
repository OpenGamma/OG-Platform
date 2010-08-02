/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.livedata.rest.RemoteUserLiveData;
import com.opengamma.financial.position.ManageablePositionMaster;
import com.opengamma.financial.position.rest.RemoteManagablePositionMaster;
import com.opengamma.financial.security.SecurityMaster;
import com.opengamma.financial.security.rest.RemoteSecurityMaster;
import com.opengamma.financial.view.ManageableViewDefinitionRepository;
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
  private final RestTarget _userLiveDataTarget;
  
  public RemoteClient(String clientId, FudgeContext fudgeContext, RestTarget baseTarget) {
    _clientId = clientId;
    _fudgeContext = fudgeContext;
    _positionMasterTarget = baseTarget.resolveBase(ClientResource.PORTFOLIOS_PATH);
    _securityMasterTarget = baseTarget.resolveBase(ClientResource.SECURITIES_PATH);
    _viewDefinitionRepositoryTarget = baseTarget.resolveBase(ClientResource.VIEW_DEFINITIONS_PATH);
    _userLiveDataTarget = baseTarget.resolveBase(ClientResource.LIVEDATA_PATH);
  }

  private RemoteClient(String clientId, FudgeContext fudgeContext, RestTarget baseTarget, RestTarget liveDataHack) {
    _clientId = clientId;
    _fudgeContext = fudgeContext;
    _positionMasterTarget = baseTarget.resolveBase(ClientResource.PORTFOLIOS_PATH);
    _securityMasterTarget = baseTarget.resolveBase(ClientResource.SECURITIES_PATH);
    _viewDefinitionRepositoryTarget = baseTarget.resolveBase(ClientResource.VIEW_DEFINITIONS_PATH);
    _userLiveDataTarget = liveDataHack;
  }
  
  public String getClientId() {
    return _clientId;
  }
  
  public ManageablePositionMaster getPositionMaster() {
    return new RemoteManagablePositionMaster(_fudgeContext, _positionMasterTarget);
  }

  public SecurityMaster getSecurityMaster() {
    return new RemoteSecurityMaster(_fudgeContext, _securityMasterTarget);
  }
  
  public ManageableViewDefinitionRepository getViewDefinitionRepository() {
    return new RemoteManagableViewDefinitionRepository(_fudgeContext, _viewDefinitionRepositoryTarget);
  }
  
  public RemoteUserLiveData getLiveData() {
    return new RemoteUserLiveData(_fudgeContext, _userLiveDataTarget);
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
  public static RemoteClient forNewClient(FudgeContext fudgeContext, RestTarget usersUri, String username) {
    return forClient(fudgeContext, usersUri, username, GUIDGenerator.generate().toString());
  }

  public static RemoteClient forClient(FudgeContext fudgeContext, RestTarget usersUri, String username, String clientId) {
    RestTarget uri = usersUri.resolveBase(username).resolveBase("clients").resolveBase(clientId);
    return new RemoteClient(clientId, fudgeContext, uri, usersUri.resolveBase("..").resolveBase(ClientResource.LIVEDATA_PATH));
  }
  
}
