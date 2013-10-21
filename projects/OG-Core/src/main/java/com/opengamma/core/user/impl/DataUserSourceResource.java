/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.user.impl;

import java.net.URI;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.user.OGUser;
import com.opengamma.core.user.UserSource;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.fudgemsg.FudgeListWrapper;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for users.
 * <p>
 * The users resource receives and processes RESTful calls to the user source.
 */
@Path("userSource")
public class DataUserSourceResource extends AbstractDataResource {

  /**
   * The user source.
   */
  private final UserSource _exgSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param userSource  the underlying user source, not null
   */
  public DataUserSourceResource(final UserSource userSource) {
    ArgumentChecker.notNull(userSource, "userSource");
    _exgSource = userSource;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the user source.
   * 
   * @return the user source, not null
   */
  public UserSource getUserSource() {
    return _exgSource;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("users")
  public Response search(
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo,
      @QueryParam("id") List<String> externalIdStrs) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    final ExternalIdBundle bundle = ExternalIdBundle.parse(externalIdStrs);
    Collection<? extends OGUser> result = getUserSource().getUsers(bundle, vc);
    return responseOkFudge(FudgeListWrapper.of(result));
  }

  @GET
  @Path("users/{userId}")
  public Response get(
      @PathParam("userId") String idStr,
      @QueryParam("version") String version,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      final OGUser result = getUserSource().get(objectId.atVersion(version));
      return responseOkFudge(result);
    } else {
      final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
      OGUser result = getUserSource().get(objectId, vc);
      return responseOkFudge(result);
    }
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("users");
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
   * @param uniqueId  the unique identifier, may be null
   * @return the URI, not null
   */
  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("users/{userId}");
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("users/{userId}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("userSearches/userid")
  public Response searchUserId(
      @QueryParam("userId") String userid,
      @QueryParam("versionAsOf") String versionAsOf,
      @QueryParam("correctedTo") String correctedTo) {
    final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    OGUser result = getUserSource().getUser(userid, vc);
    return responseOkFudge(result);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param userId  the user id, may be null
   * @param vc  the version-correction, null means latest
   * @return the URI, not null
   */
  public static URI uriSearchUserId(URI baseUri, String userId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("userSearches/userid");
    bld.queryParam("userId", userId);
    bld.queryParam("versionAsOf", vc.getVersionAsOfString());
    bld.queryParam("correctedTo", vc.getCorrectedToString());
    return bld.build();
  }

}
