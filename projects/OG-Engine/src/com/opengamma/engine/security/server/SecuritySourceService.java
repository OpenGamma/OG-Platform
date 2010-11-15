/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.engine.security.server;

import static com.opengamma.engine.security.server.SecuritySourceServiceNames.DEFAULT_SECURITYSOURCE_NAME;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;

import com.opengamma.core.security.SecuritySource;
import com.opengamma.engine.security.RemoteSecuritySource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudge.OpenGammaFudgeContext;

/**
 * RESTful backend for {@link RemoteSecuritySource}.
 */
@Path("securitySource")
public class SecuritySourceService {

  /**
   * Map of security resources by name.
   */
  private final ConcurrentMap<String, SecuritySourceResource> _securityResourceMap = new ConcurrentHashMap<String, SecuritySourceResource>();
  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates an instance using the default Fudge context.
   */
  public SecuritySourceService() {
    this(OpenGammaFudgeContext.getInstance());
  }

  /**
   * Creates an instance using the specified Fudge context.
   * @param fudgeContext  the Fudge context, not null
   */
  public SecuritySourceService(FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
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
  protected ConcurrentMap<String, SecuritySourceResource> getSecurityResourceMap() {
    return _securityResourceMap;
  }

  //-------------------------------------------------------------------------
  /**
   * Adds a security resource by name.
   * @param name  the name, not null
   * @param resource  the resource, not null
   */
  protected void addSecuritySource(final String name, final SecuritySourceResource resource) {
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
    addSecuritySource(name, new SecuritySourceResource(getFudgeContext(), source));
  }

  /**
   * Adds a security source using the default name.
   * @param source  the source, not null
   */
  public void setSecuritySource(final SecuritySource source) {
    addSecuritySource(DEFAULT_SECURITYSOURCE_NAME, source);
  }

  /**
   * Adds a map of security sources.
   * @param sources  the source map, not null
   */
  public void setSecuritySourceMap(Map<String, SecuritySource> sources) {
    final ConcurrentMap<String, SecuritySourceResource> map = getSecurityResourceMap();
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
  public SecuritySourceResource findSecuritySource(@PathParam ("name") String name) {
    return getSecurityResourceMap().get(name);
  }

}
