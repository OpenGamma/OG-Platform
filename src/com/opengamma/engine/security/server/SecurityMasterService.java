/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
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

import org.apache.commons.lang.Validate;
import org.fudgemsg.FudgeContext;

import com.opengamma.engine.security.RemoteSecuritySource;
import com.opengamma.engine.security.SecuritySource;
import com.opengamma.util.ArgumentChecker;

/**
 * RESTful backend for {@link RemoteSecuritySource}.
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
    this(FudgeContext.GLOBAL_DEFAULT);
  }

  /**
   * Creates an instance using the specified Fudge context.
   * @param fudgeContext  the Fudge context, not null
   */
  public SecurityMasterService(FudgeContext fudgeContext) {
    Validate.notNull(fudgeContext, "FudgeContext must not be null");
    _fudgeContext = fudgeContext;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * @return the Fudge context, not null
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the map of security sources.
   * @return the map, unmodifiable, not null
   */
  protected ConcurrentMap<String, SecurityMasterResource> getSecurityResourceMap() {
    return _securityResourceMap;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a security resource by name.
   * @param name  the name, not null
   * @param resource  the resource, not null
   */
  protected void addSecuritySource(final String name, final SecurityMasterResource resource) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(resource, "resource");
    getSecurityResourceMap().put(name, resource);
  }

  /**
   * Adds a security source by name.
   * @param name  the name, not null
   * @param source  the source, not null
   */
  protected void addSecuritySource(final String name, final SecuritySource source) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(source, "source");
    addSecuritySource(name, new SecurityMasterResource(getFudgeContext(), source));
  }

  /**
   * Adds a security source using the default name.
   * @param source  the source, not null
   */
  public void setSecuritySource(final SecuritySource source) {
    addSecuritySource(DEFAULT_SECURITYMASTER_NAME, source);
  }

  /**
   * Adds a map of security sources.
   * @param sources  the source map, not null
   */
  public void setSecuritySourceMap(Map<String, SecuritySource> sources) {
    final ConcurrentMap<String, SecurityMasterResource> map = getSecurityResourceMap();
    map.clear();
    for (Map.Entry<String, SecuritySource> entry : sources.entrySet()) {
      addSecuritySource(entry.getKey(), entry.getValue());
    }
  }

  //-------------------------------------------------------------------------
  /**
   * RESTful method to find a security source by name.
   * @param name  the name from the URI, not null
   * @return the resource, null if not found
   */
  @Path ("{name}")
  public SecurityMasterResource findSecuritySource(@PathParam ("name") String name) {
    return getSecurityResourceMap().get(name);
  }

}
