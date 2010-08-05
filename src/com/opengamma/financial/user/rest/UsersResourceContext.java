/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.user.rest;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.position.DelegatingPositionSource;

/**
 * Context/configuration for the objects to pass around.
 */
public class UsersResourceContext {

  private FudgeContext _fudgeContext;
  private DelegatingPositionSource _delegatingPositionSource;

  // [FIN-124] The user SecuritySource is done wrongly throughout

  public UsersResourceContext() {
  }

  public void setDelegatingPositionSource(final DelegatingPositionSource delegatingPositionSource) {
    _delegatingPositionSource = delegatingPositionSource;
  }

  public DelegatingPositionSource getDelegatingPositionSource() {
    return _delegatingPositionSource;
  }

  public void setFudgeContext(final FudgeContext fudgeContext) {
    _fudgeContext = fudgeContext;
  }

  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

}
