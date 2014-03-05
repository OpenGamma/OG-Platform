/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.legalentity.impl;

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

import com.opengamma.core.legalentity.LegalEntity;
import com.opengamma.core.legalentity.LegalEntitySource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.IdUtils;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.fudgemsg.OpenGammaFudgeContext;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for legal entities.
 * <p/>
 * The legal entities resource receives and processes RESTful calls to the legal entity source.
 */
@Path("legalentitySource")
public class DataLegalEntitySourceResource extends AbstractDataResource {

  /**
   * The legal entity source.
   */
  private final LegalEntitySource _secSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param legalentitySource the underlying legal entity source, not null
   */
  public DataLegalEntitySourceResource(final LegalEntitySource legalentitySource) {
    ArgumentChecker.notNull(legalentitySource, "legalentitySource");
    _secSource = legalentitySource;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the legal entity source.
   *
   * @return the legal entity source, not null
   */
  public LegalEntitySource getLegalEntitySource() {
    return _secSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("legal entities")
  public Response search(
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo,
      @QueryParam("id") List<String> externalIdStrs) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    Collection<? extends LegalEntity> result = getLegalEntitySource().get(bundle, vc);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  @GET
  @Path("legal entities/{legalentityId}")
  public Response get(
      @PathParam("legalentityId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final LegalEntity result = getLegalEntitySource().get(objectId.atVersion(version));
      return responseOkObject(result);
    } else {
      final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
      LegalEntity result = getLegalEntitySource().get(objectId, vc);
      return responseOkObject(result);
    }
  }

  @GET
  @Path("legalentitySearches/bulk")
  public Response getBulk(
      @QueryParam("id") List<String> uniqueIdStrs) {
    final List<UniqueId> uids = IdUtils.parseUniqueIds(uniqueIdStrs);
    Map<UniqueId, LegalEntity> result = getLegalEntitySource().get(uids);
    return responseOkObject(FudgeListWrapper.of(result.values()));
  }

  //-------------------------------------------------------------------------

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @param vc      the version-correction, null means latest
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri, VersionCorrection vc, ExternalIdBundle bundle) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legal entities");
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
   * @param uniqueId the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legal entities/{legalentityId}");
    if (uniqueId.getVersion() != null) {
      bld.queryParam("version", uniqueId.getVersion());
    }
    return bld.build(uniqueId.getObjectId());
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param objectId the object identifier, may be null
   * @param vc       the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, ObjectId objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legal entities/{legalentityId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri   the base URI, not null
   * @param uniqueIds the unique identifiers, may be null
   * @return the URI, not null
   */
  public static URI uriBulk(URI baseUri, Iterable<UniqueId> uniqueIds) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitySearches/bulk");
    bld.queryParam("id", IdUtils.toStringList(uniqueIds).toArray());
    return bld.build();
  }

  // deprecated
  //-------------------------------------------------------------------------
  @GET
  @Path("legalentitySearches/list")
  public Response searchList(@QueryParam("id") List<String> externalIdStrs) {
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    @SuppressWarnings("deprecation")
    Collection<? extends LegalEntity> result = getLegalEntitySource().get(bundle);
    return responseOkObject(FudgeListWrapper.of(result));
  }

  @GET
  @Path("legalentitySearches/single")
  public Response searchSingle(
      @QueryParam("id") List<String> externalIdStrs,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {

    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    LegalEntity result = getLegalEntitySource().getSingle(bundle, vc);
    return responseOkObject(result);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @param bundle  the bundle, may be null
   * @return the URI, not null
   */
  public static URI uriSearchList(URI baseUri, ExternalIdBundle bundle) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitySearches/list");
    bld.queryParam("id", bundle.toStringList().toArray());
    return bld.build();
  }

  /**
   * Builds a URI.
   *
   * @param baseUri the base URI, not null
   * @param bundle  the bundle, may be null
   * @param vc      the version-correction, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, ExternalIdBundle bundle, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitySearches/single");
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
   * @param baseUri the base URI, not null
   * @param bundle  the bundle, may be null
   * @param vc      the version-correction, may be null
   * @param type    the required type, may be null
   * @return the URI, not null
   */
  public static URI uriSearchSingle(URI baseUri, ExternalIdBundle bundle, VersionCorrection vc, Class<?> type) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("legalentitySearches/single");
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
    message.add("legalentitySource", getLegalEntitySource().toString());
    return new FudgeMsgEnvelope(message);
  }

}
