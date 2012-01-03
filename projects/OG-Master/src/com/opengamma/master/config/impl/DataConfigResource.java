/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;

import javax.time.Instant;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigHistoryRequest;
import com.opengamma.master.config.ConfigHistoryResult;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.time.DateUtils;

/**
 * RESTful resource for a config.
 */
@Path("/exgMaster/configs/{configId}")
public class DataConfigResource extends AbstractDataResource {

  /**
   * The configs resource.
   */
  private final DataConfigsResource _configsResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates the resource.
   * 
   * @param configsResource  the parent resource, not null
   * @param configId  the config unique identifier, not null
   */
  public DataConfigResource(final DataConfigsResource configsResource, final ObjectId configId) {
    ArgumentChecker.notNull(configsResource, "configsResource");
    ArgumentChecker.notNull(configId, "config");
    _configsResource = configsResource;
    _urlResourceId = configId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the configs resource.
   * 
   * @return the configs resource, not null
   */
  public DataConfigsResource getConfigsResource() {
    return _configsResource;
  }

  /**
   * Gets the config identifier from the URL.
   * 
   * @return the unique identifier, not null
   */
  public ObjectId getUrlConfigId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config master.
   * 
   * @return the config master, not null
   */
  public ConfigMaster getConfigMaster() {
    return getConfigsResource().getConfigMaster();
  }

  //-------------------------------------------------------------------------
  @GET
  public Response get(@QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo, @QueryParam("type") String typeStr) {
    Instant v = (versionAsOf != null ? DateUtils.parseInstant(versionAsOf) : null);
    Instant c = (correctedTo != null ? DateUtils.parseInstant(correctedTo) : null);
    if (typeStr != null) {
      Class<?> type = ReflectionUtils.loadClass(typeStr);
      ConfigDocument<?> result = getConfigMaster().get(getUrlConfigId(), VersionCorrection.of(v, c), type);
      return Response.ok(result).build();
    } else {
      ConfigDocument<?> result = getConfigMaster().get(getUrlConfigId(), VersionCorrection.of(v, c), null);
      return Response.ok(result).build();
    }
  }

  @SuppressWarnings({"rawtypes", "unchecked" })  // necessary to stop Jersey issuing warnings due to <?>
  @PUT
  @Consumes(FudgeRest.MEDIA)
  public Response put(ConfigDocument request) {
    if (getUrlConfigId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ConfigDocument<?> result = getConfigMaster().update(request);
    return Response.ok(result).build();
  }

  @DELETE
  @Consumes(FudgeRest.MEDIA)
  public Response delete() {
    getConfigMaster().remove(getUrlConfigId().atLatestVersion());
    return Response.noContent().build();
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("versions")
  public Response history(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    ConfigHistoryRequest<?> request = decodeBean(ConfigHistoryRequest.class, providers, msgBase64);
    if (getUrlConfigId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    ConfigHistoryResult<?> result = getConfigMaster().history(request);
    return Response.ok(result).build();
  }

  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@PathParam("versionId") String versionId, @QueryParam("type") String typeStr) {
    if (typeStr != null) {
      Class<?> type = ReflectionUtils.loadClass(typeStr);
      ConfigDocument<?> result = getConfigMaster().get(getUrlConfigId().atVersion(versionId), type);
      return Response.ok(result).build();
    } else {
      ConfigDocument<?> result = getConfigMaster().get(getUrlConfigId().atVersion(versionId));
      return Response.ok(result).build();
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param versionCorrection  the version-correction locator, null for latest
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, ObjectIdentifiable objectId, VersionCorrection versionCorrection, Class<?> type) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/configs/{configId}");
    if (versionCorrection != null && versionCorrection.getVersionAsOf() != null) {
      b.queryParam("versionAsOf", versionCorrection.getVersionAsOf());
    }
    if (versionCorrection != null && versionCorrection.getCorrectedTo() != null) {
      b.queryParam("correctedTo", versionCorrection.getCorrectedTo());
    }
    if (type != null) {
      b.queryParam("type", type.getName());
    }
    return b.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for the versions of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the resource identifier, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriVersions(URI baseUri, ObjectIdentifiable objectId, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs/{configId}/versions");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the resource unique identifier, not null
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  public static URI uriVersion(URI baseUri, UniqueId uniqueId, Class<?> type) {
    UriBuilder b = UriBuilder.fromUri(baseUri).path("/configs/{configId}/versions/{versionId}");
    if (type != null) {
      b.queryParam("type", type.getName());
    }
    return b.build(uniqueId.toLatest(), uniqueId.getVersion());
  }

}
