/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.master.position.PositionMaster;

/**
 * Context/configuration for the objects to pass around.
 */
public class UsersResourceContext {

  private FudgeContext _fudgeContext;
  private PositionMaster _userPositionMaster;

  // [FIN-124] The user SecuritySource is done wrongly throughout

  public UsersResourceContext() {
  }
  
  public void setUserPositionMaster(PositionMaster positionMaster) {
    _userPositionMaster = positionMaster;
  }
  
  public PositionMaster getPositionMaster() {
    return _userPositionMaster;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

}
