/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.config.impl;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsg;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.config.ConfigSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ReflectionUtils;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
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

  @SuppressWarnings("unchecked")
  private FudgeMsg configItemCollectionResult(final Collection<?> items) {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    final MutableFudgeMsg msg = serializer.newMessage();
    for (final ConfigItem<?> item : (Collection<ConfigItem<?>>) items) {
      serializer.addToMessageWithClassHeaders(msg, null, null, item, ConfigItem.class);
    }
    return msg;
  }

  private FudgeMsg configItemResult(final ConfigItem<?> item) {
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    return FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(item), item.getClass(), ConfigItem.class);
  }

  private FudgeMsg configValueResult(final Class<?> clazz, final Object value) {
    if (value == null) {
      return null;
    }
    final FudgeSerializer serializer = new FudgeSerializer(OpenGammaFudgeContext.getInstance());
    return FudgeSerializer.addClassHeader(serializer.objectToFudgeMsg(value), value.getClass(), clazz);
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("configs")
  public Response search(
    @QueryParam("type") final String typeStr,
    @QueryParam("versionCorrection") final String versionCorrectionStr,
    @QueryParam("name") final String name) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final VersionCorrection versionCorrection = (versionCorrectionStr != null) ? VersionCorrection.parse(versionCorrectionStr) : VersionCorrection.LATEST;
    if (name == null) {
      return responseOkObject(configItemCollectionResult(getConfigSource().getAll(type, versionCorrection)));
    } else {
      return responseOkObject(configItemCollectionResult(getConfigSource().get(type, name, versionCorrection)));
    }
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @param name the name, may be null
   * @param versionCorrection the version to fetch, null means latest
   * @param type the config type, may be null
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final String name, final VersionCorrection versionCorrection, final Class<?> type) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(type, "type");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs");
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
  @Path("configs/{uid}")
  public Response get(
    @PathParam("uid") final String uidStr) {
    final UniqueId uid = UniqueId.parse(uidStr);
    final ConfigItem<?> result = getConfigSource().get(uid);
    return responseOkObject(configItemResult(result));
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final UniqueId uniqueId) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs/{uid}");
    return bld.build(uniqueId);
  }

  @GET
  @Path("configs/{oid}/{versionCorrection}")
  public Response getByOidVersionCorrection(
    @PathParam("oid") final String idStr,
    @PathParam("versionCorrection") final String versionCorrectionStr) {
    final ObjectId objectId = ObjectId.parse(idStr);
    final VersionCorrection versionCorrection = VersionCorrection.parse(versionCorrectionStr);
    final ConfigItem<?> result = getConfigSource().get(objectId, versionCorrection);
    return responseOkObject(configItemResult(result));
  }


  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @param versionCorrection  the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(final URI baseUri, final ObjectId objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(objectId, "objectId");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs/{oid}/{versionCorrection}");
    return bld.build(objectId, VersionCorrection.of(versionCorrection));
  }

  @GET
  @Path("configSearches/single")
  public Response searchSingle(
    @QueryParam("type") final String typeStr,
    @QueryParam("versionCorrection") final String versionCorrectionStr,
    @QueryParam("name") final String name) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final VersionCorrection versionCorrection = (versionCorrectionStr != null) ? VersionCorrection.parse(versionCorrectionStr) : VersionCorrection.LATEST;
    return responseOkObject(configValueResult(type, getConfigSource().getSingle(type, name, versionCorrection)));
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
  public static URI uriSearchSingle(final URI baseUri, final String name, final VersionCorrection versionCorrection, final Class<?> type) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(name, "name");
    ArgumentChecker.notNull(type, "type");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configSearches/single");
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
    @QueryParam("type") final String typeStr,
    @QueryParam("versionCorrection") final String versionCorrectionStr) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final VersionCorrection versionCorrection = (versionCorrectionStr != null) ? VersionCorrection.parse(versionCorrectionStr) : VersionCorrection.LATEST;
    return responseOkObject(configItemCollectionResult(getConfigSource().getAll(type, versionCorrection)));
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
  public static URI uriSearch(final URI baseUri, final Class<?> type, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    ArgumentChecker.notNull(type, "type");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("configSearches");
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
    @QueryParam("type") final String typeStr,
    @QueryParam("versionCorrection") final String versionCorrectionStr) {
    final Class<?> type = ReflectionUtils.loadClass(typeStr);
    final VersionCorrection versionCorrection = (versionCorrectionStr != null) ? VersionCorrection.parse(versionCorrectionStr) : VersionCorrection.LATEST;
    return responseOkObject(configItemCollectionResult(getConfigSource().getAll(type, versionCorrection)));
  }

  public static <T> URI uriPut(final URI baseUri) {
    ArgumentChecker.notNull(baseUri, "baseUri");
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("put");
    return bld.build();
  }

}
