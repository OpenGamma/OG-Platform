/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.server;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.position.RemoteUserPositionMaster;
import com.opengamma.engine.position.UserPositionMaster;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful back-end for {@link RemoteUserPositionMaster}.
 */
@Path("userPositionMaster")
public class UserPositionMasterService {
  
  /**
   * The name used when a {@link UserPositionMaster} is added through
   * {@link #setUserPositionMaster(UserPositionMaster)}.
   */
  public static final String DEFAULT_USER_POSITION_MASTER_NAME = "0";
  
  private final ConcurrentMap<String, UserPositionMasterResource> _userPositionMasterMap = new ConcurrentHashMap<String, UserPositionMasterResource>();
  private final FudgeContext _fudgeContext;
  
  public UserPositionMasterService() {
    this(FudgeContext.GLOBAL_DEFAULT);
  }
  
  public UserPositionMasterService(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }
  
  public void setUserPositionMaster(UserPositionMaster<UniqueIdentifier> userPositionMaster) {
    addUserPositionMaster(DEFAULT_USER_POSITION_MASTER_NAME, userPositionMaster);
  }
  
  private void addUserPositionMaster(String name, UserPositionMaster<UniqueIdentifier> userPositionMaster) {
    _userPositionMasterMap.put(name, new UserPositionMasterResource(_fudgeContext, userPositionMaster));
  }
  
  @Path("{masterName}")
  public UserPositionMasterResource getPositionMaster(@PathParam("masterName") String masterName) {
    return _userPositionMasterMap.get(masterName);
  }
}
