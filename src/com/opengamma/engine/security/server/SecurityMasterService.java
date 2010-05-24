/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

import static com.opengamma.engine.security.server.SecurityMasterServiceNames.DEFAULT_SECURITYMASTER_NAME;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;

import com.opengamma.engine.security.RemoteSecurityMaster;
import com.opengamma.engine.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful backend for {@link RemoteSecurityMaster}.
 */
@Path ("securityMaster")
public class SecurityMasterService {
  
  private final ConcurrentMap<String, SecurityMasterResource> _securityMasterMap = new ConcurrentHashMap<String, SecurityMasterResource>();
  
  private final FudgeContext _fudgeContext;
  
  public SecurityMasterService() {
    this(FudgeContext.GLOBAL_DEFAULT);
  }
  
  public SecurityMasterService(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "Fudge context");
    _fudgeContext = fudgeContext;
  }
  
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }
  
  protected ConcurrentMap<String, SecurityMasterResource> getSecurityMasterMap() {
    return _securityMasterMap;
  }
  
  protected void addSecurityMaster(final String name, final SecurityMasterResource securityMasterResource) {
    getSecurityMasterMap().put(name, securityMasterResource);
  }
  
  protected void addSecurityMaster(final String name, final SecurityMaster securityMaster) {
    addSecurityMaster(name, new SecurityMasterResource(getFudgeContext(), securityMaster));
  }
  
  public void setSecurityMaster(final SecurityMaster securityMaster) {
    addSecurityMaster(DEFAULT_SECURITYMASTER_NAME, securityMaster);
  }
  
  public void setSecurityMasterMap(Map<String, SecurityMaster> securityMasters) {
    final ConcurrentMap<String, SecurityMasterResource> map = getSecurityMasterMap();
    map.clear();
    for (Map.Entry<String, SecurityMaster> entry : securityMasters.entrySet()) {
      addSecurityMaster(entry.getKey(), entry.getValue());
    }
  }
  @Path ("{masterName}")
  public SecurityMasterResource findSecurityMaster(@PathParam ("masterName") String masterName) {
    return getSecurityMasterMap().get(masterName);
  }
  
}
