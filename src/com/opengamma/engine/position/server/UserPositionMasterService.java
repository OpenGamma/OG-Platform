/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.position.server;

import static com.opengamma.engine.position.server.UserPositionMasterServiceNames.DEFAULT_USER_POSITION_MASTER_NAME;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.position.RemoteUserPositionMaster;
import com.opengamma.engine.position.UserPositionMaster;
import com.opengamma.engine.view.server.EngineFudgeContextConfiguration;

/**
 * RESTful back-end for {@link RemoteUserPositionMaster}.
 */
@Path("userPositionMaster")
public class UserPositionMasterService {
   
  private final ConcurrentMap<String, UserPositionMasterResource> _userPositionMasterMap = new ConcurrentHashMap<String, UserPositionMasterResource>();
  private final FudgeContext _fudgeContext;
  
  public UserPositionMasterService() {
    _fudgeContext = new FudgeContext();
    EngineFudgeContextConfiguration.INSTANCE.configureFudgeContext(_fudgeContext);
  }
  
  public void setUserPositionMaster(UserPositionMaster userPositionMaster) {
    addUserPositionMaster(DEFAULT_USER_POSITION_MASTER_NAME, userPositionMaster);
  }
  
  private void addUserPositionMaster(String name, UserPositionMaster userPositionMaster) {
    _userPositionMasterMap.put(name, new UserPositionMasterResource(_fudgeContext, userPositionMaster));
  }
  
  @Path("{masterName}")
  public UserPositionMasterResource getPositionMaster(@PathParam("masterName") String masterName) {
    return _userPositionMasterMap.get(masterName);
  }
}
