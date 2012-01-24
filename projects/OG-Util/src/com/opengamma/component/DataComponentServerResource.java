/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component;

import java.net.URI;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.google.common.collect.ImmutableList;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for exposing managed components.
 * <p>
 * This resource receives and processes RESTful calls to all managed components.
 */
@Path("components")
public class DataComponentServerResource extends AbstractDataResource {

  /**
   * The local components.
   */
  private final List<RestComponent> _localComponents;
  /**
   * The remote components.
   */
  private final List<ComponentInfo> _remoteComponents;

  /**
   * Creates the resource.
   * 
   * @param localComponents  the managed components, not null
   * @param remoteComponents  the republished remote components, not null
   */
  public DataComponentServerResource(final Iterable<RestComponent> localComponents, final Iterable<ComponentInfo> remoteComponents) {
    ArgumentChecker.notNull(localComponents, "localComponents");
    _localComponents = ImmutableList.copyOf(localComponents);
    _remoteComponents = ImmutableList.copyOf(remoteComponents);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the components.
   * 
   * @return the components, not null
   */
  public List<RestComponent> getComponents() {
    return _localComponents;
  }

  //-------------------------------------------------------------------------
  @HEAD
  public Response status() {
    // simple GET to quickly return as a ping
    return Response.ok().build();
  }

  @GET
  public Response getComponentInfos() {
    ComponentServer server = new ComponentServer();
    server.getComponentInfos().addAll(_remoteComponents);
    for (RestComponent component : _localComponents) {
      server.getComponentInfos().add(component.getInfo());
    }
    server.setUri(URI.create("components"));
    return Response.ok(server).build();
  }

  @Path("{type}/{classifier}")
  public Object findComponent(@PathParam("type") String type, @PathParam("classifier") String classifier) {
    for (RestComponent component : _localComponents) {
      if (component.getInfo().getType().getSimpleName().equalsIgnoreCase(type) && component.getInfo().getClassifier().equalsIgnoreCase(classifier)) {
        return component.getInstance();
      }
    }
    return null;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI to fetch all components.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("components");
    return bld.build();
  }

  /**
   * Builds a URI for a single component.
   * 
   * @param baseUri  the base URI, not null
   * @param info  the component info, not null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ComponentInfo info) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("components/{type}/{classifier}");
    return bld.build(info.getType().getSimpleName(), info.getClassifier());
  }

}
