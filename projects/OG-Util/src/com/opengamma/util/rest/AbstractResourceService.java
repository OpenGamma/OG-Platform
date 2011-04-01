/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.util.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.util.ArgumentChecker;

/**
 * Abstract RESTful backend.
 * 
 * @param <Underlying> the underlying object exposed
 * @param <UnderlyingResource> the resource wrapper
 */
public abstract class AbstractResourceService<Underlying, UnderlyingResource> {

  /**
   * Default instance name.
   */
  public static final String DEFAULT_RESOURCE_NAME = "0";

  /**
   * Map of resource names to resource instances.
   */
  private final ConcurrentMap<String, UnderlyingResource> _resourceMap = new ConcurrentHashMap<String, UnderlyingResource>();

  /**
   * The Fudge context available to sub-classes for resource construction.
   */
  private final FudgeContext _fudgeContext;

  /**
   * Creates a new service.
   * 
   * @param fudgeContext the Fudge context to use, not {@code null}
   */
  protected AbstractResourceService(final FudgeContext fudgeContext) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    _fudgeContext = fudgeContext;
  }

  /**
   * Returns the Fudge context.
   * 
   * @return the Fudge context
   */
  public FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Returns the resource map.
   * 
   * @return the resource map
   */
  protected ConcurrentMap<String, UnderlyingResource> getResourceMap() {
    return _resourceMap;
  }

  /**
   * Adds a named resource to the resource map.
   * 
   * @param name the resource name, not {@code null}
   * @param resource the resource, not {@code null}
   */
  protected void addResource(final String name, final UnderlyingResource resource) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(resource, "resource");
    getResourceMap().put(name, resource);
  }

  /**
   * Creates a resource to handle calls to the given underlying instance.
   * 
   * @param underlying the underlying object to handle the REST calls, not {@code null}
   * @return a resource instance, not {@code null}
   */
  protected abstract UnderlyingResource createResource(Underlying underlying);

  /**
   * Wraps the named underlying in a resource using {@link #createResource} and adds it to the map.
   * 
   * @param name the resource name, not {@code null}
   * @param underlying the underlying, not {@code null}
   */
  protected void addUnderlying(final String name, final Underlying underlying) {
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(underlying, "underlying");
    addResource(name, createResource(underlying));
  }

  /**
   * Wraps the underlying in a resource using {@link #createResource} and adds it to the map with the default name.
   * 
   * @param underlying the underlying, not {@code null}
   */
  public void setUnderlying(final Underlying underlying) {
    ArgumentChecker.notNull(underlying, "underlying");
    addResource(DEFAULT_RESOURCE_NAME, createResource(underlying));
  }

  /**
   * Wraps the named underlyings in resources using {@link #createResource} and adds them to the map.
   * 
   * @param underlyings a map of name to underlying pairs, not {@code null}
   */
  public void setUnderlyingMap(Map<String, Underlying> underlyings) {
    ArgumentChecker.notNull(underlyings, "underlyings");
    final ConcurrentMap<String, UnderlyingResource> map = getResourceMap();
    map.clear();
    for (Map.Entry<String, Underlying> entry : underlyings.entrySet()) {
      addUnderlying(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Returns the named resource.
   * 
   * @param name the resource name
   * @return the resource, or {@code null} if not found
   */
  @Path("{name}")
  public UnderlyingResource findResource(@PathParam("name") String name) {
    return getResourceMap().get(name);
  }

  /**
   * Returns the set of resource names available.
   * 
   * @return a Fudge message containing the resource names
   */
  @GET
  public FudgeMsgEnvelope listResources() {
    final MutableFudgeMsg msg = getFudgeContext().newMessage();
    for (String key : getResourceMap().keySet()) {
      msg.add("name", key);
    }
    return new FudgeMsgEnvelope(msg);
  }

}
