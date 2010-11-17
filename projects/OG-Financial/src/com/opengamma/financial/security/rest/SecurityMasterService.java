/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.security.rest;

import static com.opengamma.financial.security.rest.SecurityMasterServiceNames.DEFAULT_SECURITYMASTER_NAME;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;

import com.opengamma.master.security.SecurityMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * RESTful backend for {@link RemoteSecurityMaster}.
 */
@Path("securityMaster")
public class SecurityMasterService {

  /**
   * Map of security resources by name.
   */
  private final ConcurrentMap<String, SecurityMasterResource> _securityResourceMap = new ConcurrentHashMap<String, SecurityMasterResource>();
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance using the default Fudge context.
   */
  public SecurityMasterService() {
    this(OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance using the specified Fudge context.
   * @param fudgeContext  the Fudge context, not null
   */
  public SecurityMasterService(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * @return the Fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the map of security masters.
   * @return the map, unmodifiable, not null
   */
  protected ConcurrentMap<String, SecurityMasterResource> getSecurityResourceMap() {
    return _securityResourceMap;
  }

  // -------------------------------------------------------------------------
  /**
   * Adds a security resource by name.
   * @param name  the name, not null
   * @param resource  the resource, not null
   */
  protected void addSecurityMaster(final String name, final SecurityMasterResource resource) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(resource, "resource");
    getSecurityResourceMap().put(name, resource);
  }

  /**
   * Adds a security master by name.
   * @param name  the name, not null
   * @param master  the master, not null
   */
  protected void addSecurityMaster(final String name, final SecurityMaster master) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(master, "master");
    addSecurityMaster(name, new SecurityMasterResource(master, getFudgeContext()));
  }

  /**
   * Adds a security master using the default name.
   * @param master  the master, not null
   */
  public void setSecurityMaster(final SecurityMaster master) {
    addSecurityMaster(DEFAULT_SECURITYMASTER_NAME, master);
  }

  /**
   * Adds a map of security masters.
   * @param masters  the master map, not null
   */
  public void setSecurityMasterMap(Map<String, SecurityMaster> masters) {
    final ConcurrentMap<String, SecurityMasterResource> map = getSecurityResourceMap();
    map.clear();
    for (Map.Entry<String, SecurityMaster> entry : masters.entrySet()) {
      addSecurityMaster(entry.getKey(), entry.getValue());
    }
  }

  // -------------------------------------------------------------------------
  /**
   * RESTful method to find a security master by name.
   * @param name  the name from the URI, not null
   * @return the resource, null if not found
   */
  @Path("{name}")
  public SecurityMasterResource findSecurityMaster(@PathParam("name") String name) {
    return getSecurityResourceMap().get(name);
  }

}
