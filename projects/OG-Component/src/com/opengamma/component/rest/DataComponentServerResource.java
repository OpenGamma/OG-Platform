/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.component.rest;

import java.net.URI;
import java.util.Comparator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;
import com.opengamma.component.ComponentInfo;
import com.opengamma.component.ComponentServer;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.web.FreemarkerOutputter;

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
    return responseOk();
  }

  @GET
  public Response getComponentInfos() {
    ComponentServer server = new ComponentServer(URI.create("components"));
    server.getComponentInfos().addAll(_remoteComponents);
    for (RestComponent component : _localComponents) {
      server.getComponentInfos().add(component.getInfo());
    }
    return responseOkFudge(server);
  }

  @GET
  @Produces(value = MediaType.TEXT_HTML)
  public String getComponentInfosHtml(@Context ServletContext servletContext, @Context UriInfo uriInfo) {
    ComponentServer server = new ComponentServer(URI.create("components"));
    server.getComponentInfos().addAll(_remoteComponents);
    for (RestComponent component : _localComponents) {
      server.getComponentInfos().add(component.getInfo());
    }
    server.setUri(uriInfo.getBaseUri());
    Comparator<Class<?>> order = new Comparator<Class<?>>() {
      @Override
      public int compare(Class<?> cls1, Class<?> cls2) {
        return cls1.getSimpleName().compareTo(cls2.getSimpleName());
      }
    };
    Multimap<Class<?>, ComponentInfo> byType = TreeMultimap.create(order, Ordering.arbitrary());
    for (ComponentInfo info : server.getComponentInfos()) {
      byType.put(info.getType(), info);
    }
    FreemarkerOutputter freemarker = new FreemarkerOutputter(servletContext);
    FlexiBean data = freemarker.createRootData();
    data.put("componentServer", server);
    data.put("infosByType", byType);
    return freemarker.build("data/componentserver.ftl", data);
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
