/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.net.URI;
import java.util.Collection;

import javax.time.Instant;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for configuration.
 * <p>
 * This resource receives and processes RESTful calls to the configuration source.
 */
@Path("configSource")
public class DataConfigSourceResource extends AbstractDataResource {

  /**
   * The config source.
   */
  private final ConfigSource _exgSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param configSource  the underlying config source, not null
   */
  public DataConfigSourceResource(final ConfigSource configSource) {
    ArgumentChecker.notNull(configSource, "configSource");
    _exgSource = configSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the configuration source.
   * 
   * @return the configuration source, not null
   */
  public ConfigSource getConfigSource() {
    return _exgSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("configs")
  public Response search(
      @QueryParam("type") String typeStr,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo,
      @QueryParam("name") String name) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    Collection<?> result = getConfigSource().getConfigs(type, name, vc);
    return responseOkFudge(FudgeListWrapper.of(result));
  }

  @GET
  @Path("configs/{configId}")
  public Response get(
      @PathParam("configId") String idStr,
      @QueryParam("type") String typeStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      Object result = getConfigSource().getConfig(type, objectId.atVersion(version));
      return responseOkFudge(result);
    } else {
      VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
      Object result = getConfigSource().getConfig(type, objectId, vc);
      return responseOkFudge(result);
    }
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param vc  the version-correction, null means latest
   * @param name  the name, may be null
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri, VersionCorrection vc, String name, Class<?> type) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    if (type != null) {
      bld.queryParam("type", type.getName());
    }
    bld.queryParam("name", name);
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId, Class<?> type) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs/{configId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    if (type != null) {
      bld.queryParam("type", type.getName());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @param vc  the version-correction, null means latest
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId, VersionCorrection vc, Class<?> type) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs/{configId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    if (type != null) {
      bld.queryParam("type", type.getName());
    }
    return bld.build(objectId);
  }

  // deprecated
  //-------------------------------------------------------------------------
  @GET
  @Path("configSearches/single")
  public Response searchSingle(
      @QueryParam("type") String typeStr,
      @QueryParam("versionAsOf") String versionAsOfStr,
      @QueryParam("name") String name) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    if (versionAsOfStr != null) {
      final Instant versionAsOf = VersionCorrection.parse(versionAsOfStr, null).getVersionAsOf();
      Object result = getConfigSource().getByName(type, name, versionAsOf);
      return responseOkFudge(result);
    } else {
      Object result = getConfigSource().getLatestByName(type, name);
      return responseOkFudge(result);
    }
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param name  the name, may be null
   * @param versionAsOf  the version to fetch, null means latest
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, String name, Instant versionAsOf, Class<?> type) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configSearches/single");
    bld.queryParam("name", name);
    if (type != null) {
      bld.queryParam("type", type.getName());
    }
    if (versionAsOf != null) {
      bld.queryParam("versionAsOf", versionAsOf.toString());
    }
    return bld.build();
  }

}
