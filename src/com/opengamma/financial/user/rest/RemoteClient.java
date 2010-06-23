/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.financial.position.ManagablePositionMaster;
import com.opengamma.financial.position.rest.RemoteManagablePositionMaster;
import com.opengamma.transport.jaxrs.RestTarget;

/**
 * Provides access to a remote representation of a client
 */
public class RemoteClient {

  private final FudgeContext _fudgeContext;
  private final RestTarget _positionMasterTarget;
  
  public RemoteClient(FudgeContext fudgeContext, RestTarget baseTarget) {
    _fudgeContext = fudgeContext;
    _positionMasterTarget = baseTarget.resolve(ClientResource.PORTFOLIOS_PATH);
  }
  
  public ManagablePositionMaster getPositionMaster() {
    return new RemoteManagablePositionMaster(_fudgeContext, _positionMasterTarget);
  }
  
}
