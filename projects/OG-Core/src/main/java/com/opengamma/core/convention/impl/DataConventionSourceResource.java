/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.convention.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;

import com.opengamma.core.convention.Convention;
import com.opengamma.core.convention.ConventionSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.ClassUtils;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for conventions.
 * <p>
 * The conventions resource receives and processes RESTful calls to the convention source.
 */
@Path("conventionSource")
public class DataConventionSourceResource extends AbstractDataResource {

  /**
   * The convention source.
   */
  private final ConventionSource _secSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param conventionSource  the underlying convention source, not null
   */
  public DataConventionSourceResource(final ConventionSource conventionSource) {
    ArgumentChecker.notNull(conventionSource, "conventionSource");
    _secSource = conventionSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the convention source.
   * 
   * @return the convention source, not null
   */
  public ConventionSource getConventionSource() {
    return _secSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("conventions")
  public Response search(
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo,
      @QueryParam("id") List<String> externalIdStrs) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    Collection<? extends Convention> result = getConventionSource().get(bundle, vc);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  @GET
  @Path("conventions/{conventionId}")
  public Response get(
      @PathParam("conventionId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final Convention result = getConventionSource().get(objectId.atVersion(version));
      return responseOkObject(result);
    } else {
      final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
      Convention result = getConventionSource().get(objectId, vc);
      return responseOkObject(result);
    }
  }

  @GET
  @Path("conventionSearches/bulk")
  public Response getBulk(
      @QueryParam("id") List<String> uniqueIdStrs) {
    final List<UniqueId> uids = IdUtils.parseUniqueIds(uniqueIdStrs);
    Map<UniqueId, Convention> result = getConventionSource().get(uids);
    return responseOkObject(FudgeListWrapper.of(result.values()));
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param vc  the version-correction, null means latest
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri, VersionCorrection vc, ExternalIdBundle bundle) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventions");
    if (vc != null) {
      bld.queryParam("versionAsof", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventions/{conventionId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param objectId  the object identifier, may be null
   * @param vc  the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventions/{conventionId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param uniqueIds  the unique identifiers, may be null
   * @return the URI, not null
   */
  public static URI uriBulk(URI baseUri, Iterable<UniqueId> uniqueIds) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventionSearches/bulk");
    bld.queryParam("id", IdUtils.toStringList(uniqueIds).toArray());
    return bld.build();
  }

  // deprecated
  //-------------------------------------------------------------------------
  @GET
  @Path("conventionSearches/list")
  public Response searchList(@QueryParam("id") List<String> externalIdStrs) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    @SuppressWarnings("deprecation")
    Collection<? extends Convention> result = getConventionSource().get(bundle);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  @GET
  @Path("conventionSearches/single")
  public Response searchSingle(
      @QueryParam("id") List<String> externalIdStrs,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo,
      @QueryParam("type") String typeStr) {
    
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    if (typeStr != null) {
      Class<? extends Convention> type = ClassUtils.loadClassRuntime(typeStr, Convention.class);
      Convention result = getConventionSource().getSingle(bundle, vc, type);
      return responseOkObject(result);
    } else {
      Convention result = getConventionSource().getSingle(bundle, vc);
      return responseOkObject(result);
    }
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearchList(URI baseUri, ExternalIdBundle bundle) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventionSearches/list");
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param bundle  the bundle, may be null
   * @param vc  the version-correction, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, ExternalIdBundle bundle, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventionSearches/single");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param bundle  the bundle, may be null
   * @param vc  the version-correction, may be null
   * @param type  the required type, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, ExternalIdBundle bundle, VersionCorrection vc, Class<?> type) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("conventionSearches/single");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    if (type != null) {
      bld.queryParam("type", type.getName());
    }
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  //-------------------------------------------------------------------------
  /**
   * For debugging purposes only.
   * 
   * @return some debug information about the state of this resource object
   */
  @GET
  @Path("debugInfo")
  public FudgeMsgEnvelope getDebugInfo() {
    final MutableFudgeMsg message = OpenGammaFudgeContext.getInstance().newMessage();
    message.add("fudgeContext", OpenGammaFudgeContext.getInstance().toString());
    message.add("conventionSource", getConventionSource().toString());
    return new FudgeMsgEnvelope(message);
  }

}
