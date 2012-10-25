/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.core.exchange.impl.SimpleExchange;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;
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
    if (name == null) {
      Collection<?> result = getConfigSource().getAll(type, vc);
      return responseOkFudge(result);
    } else {
      Collection<?> result = Collections.singleton(getConfigSource().get(type, name, vc));
      return responseOkFudge(result);
    }
  }

  @GET
  @Path("configs/{uid}")
  public Response get(
    @PathParam("uid") String uidStr) {
    final UniqueId uid = UniqueId.parse(uidStr);
    SimpleExchange result = getConfigSource().getConfig(SimpleExchange.class, uid);
    return responseOkFudge(result);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null   
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs/{uid}");
    return bld.build(uniqueId);
  }

  @GET
  @Path("configs/{oid}/{versionCorrection}")
  public Response getByOidVersionCorrection(
    @PathParam("oid") String idStr,
    @PathParam("versionCorrection") String versionCorrectionStr) {

    final ObjectId objectId = ObjectId.parse(idStr);
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionCorrectionStr);
    SimpleExchange exchange = getConfigSource().getConfig(SimpleExchange.class, objectId, versionCorrection);
    return responseOkFudge(exchange);
  }


  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @param versionCorrection  the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(objectId, "objectId");
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs/{oid}/{versionCorrection}");
    versionCorrection = versionCorrection != null ? versionCorrection : VersionCorrection.LATEST;
    return bld.build(objectId, versionCorrection);
  }

  @GET
  @Path("configSearches/single")
  public Response searchSingle(
    @QueryParam("type") String typeStr,
    @QueryParam("versionCorrection") String versionCorrectionStr,
    @QueryParam("name") String name) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    if (versionCorrectionStr != null) {
      final VersionCorrection versionCorrection = VersionCorrection.parse(versionCorrectionStr);
      ConfigItem<?> result = getConfigSource().get(type, name, versionCorrection);
      return responseOkFudge(result);
    } else {
      ConfigItem<?> result = getConfigSource().get(type, name, VersionCorrection.LATEST);
      return responseOkFudge(result);
    }
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param name  the name, may be null
   * @param versionCorrection  the version to fetch, null means latest
   * @param type  the config type, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, String name, VersionCorrection versionCorrection, Class<?> type) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(type, "type");
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configSearches/single");
    bld.queryParam("name", name);
    bld.queryParam("type", type.getName());

    if (versionCorrection != null) {
      bld.queryParam("versionCorrection", versionCorrection.toString());
    } else {
      bld.queryParam("versionCorrection", VersionCorrection.LATEST.toString());
    }
    return bld.build();
  }

  @GET
  @Path("configSearches")
  public Response search(
    @QueryParam("type") String typeStr,
    @QueryParam("versionCorrection") String versionCorrectionStr) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    if (versionCorrectionStr != null) {
      final VersionCorrection versionCorrection = VersionCorrection.parse(versionCorrectionStr);
      Collection<? extends ConfigItem<?>> result = getConfigSource().getAll(type, versionCorrection);
      return responseOkFudge(result);
    } else {
      Collection<? extends ConfigItem<?>> result = getConfigSource().getAll(type, VersionCorrection.LATEST);
      return responseOkFudge(result);
    }
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param type  the config type, may be null
   * @param versionCorrection  the version to fetch, null means latest
   *
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri, Class<?> type, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(type, "type");
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configSearches");
    bld.queryParam("type", type.getName());

    if (versionCorrection != null) {
      bld.queryParam("versionCorrection", versionCorrection.toString());
    } else {
      bld.queryParam("versionCorrection", VersionCorrection.LATEST.toString());
    }
    return bld.build();
  }

  // TODO: put is not a RESTful URI!
  @PUT
  @Path("put")
  public Response put(
    @QueryParam("type") String typeStr,
    @QueryParam("versionCorrection") String versionCorrectionStr) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    if (versionCorrectionStr != null) {
      final VersionCorrection versionCorrection = VersionCorrection.parse(versionCorrectionStr);
      Collection<? extends ConfigItem<?>> result = getConfigSource().getAll(type, versionCorrection);
      return responseOkFudge(result);
    } else {
      Collection<? extends ConfigItem<?>> result = getConfigSource().getAll(type, VersionCorrection.LATEST);
      return responseOkFudge(result);
    }
  }

  public static <T> URI uriPut(URI baseUri) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("put");
    return bld.build();
  }

}
