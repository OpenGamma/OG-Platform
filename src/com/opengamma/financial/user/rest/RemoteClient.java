/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.livedata.rest.RemoteUserLiveData;
import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.rest.RemoteManagablePositionMaster;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.GUIDGenerator;

/**
 * Provides access to a remote representation of a client
 */
public class RemoteClient {

  private final FudgeContext _fudgeContext;
  private final RestTarget _positionMasterTarget;
  private final RestTarget _userLiveDataTarget;
  
  public RemoteClient(FudgeContext fudgeContext, RestTarget baseTarget) {
    _fudgeContext = fudgeContext;
    _positionMasterTarget = baseTarget.resolve(ClientResource.PORTFOLIOS_PATH);
    _userLiveDataTarget = baseTarget.resolveBase(ClientResource.LIVEDATA_PATH);
  }

  private RemoteClient(FudgeContext fudgeContext, RestTarget baseTarget, RestTarget liveDataHack) {
    _fudgeContext = fudgeContext;
    _positionMasterTarget = baseTarget.resolve(ClientResource.PORTFOLIOS_PATH);
    _userLiveDataTarget = liveDataHack;
  }
  
  public ManagablePositionMaster getPositionMaster() {
    return new RemoteManagablePositionMaster(_fudgeContext, _positionMasterTarget);
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
    // Just use a GUID for the client name
    String clientName = GUIDGenerator.generate().toString();
    RestTarget uri = usersUri.resolveBase(username).resolveBase("clients").resolveBase(clientName);
    return new RemoteClient(fudgeContext, uri, usersUri.resolveBase("..").resolveBase(ClientResource.LIVEDATA_PATH));
  }
  
}
