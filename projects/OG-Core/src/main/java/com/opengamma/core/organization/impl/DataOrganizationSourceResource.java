/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.core.organization.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.core.organization.OrganizationSource;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for organizations.
 * <p>
 * The organizations resource receives and processes RESTful calls to the organization source.
 */
@Path("organizationSource")
public class DataOrganizationSourceResource extends AbstractDataResource {

  /** URI fragment. */
  public static final String GET_ORGANIZATIONS_URL = "organizations/{organizationId}";
  /** URI fragment. */
  public static final String SEARCH_ORGANIZATION_BY_TICKER = "organizationSearches/ticker";
  /** URI fragment. */
  public static final String SEARCH_ORGANIZATION_BY_RED_CODE = "organizationSearches/redCode";
  /** URI fragment. */
  public static final String TICKER_PARAM = "ticker";
  /** URI fragment. */
  public static final String RED_CODE_PARAM = "redCode";
  /** URI fragment. */
  public static final String ORGANIZATION_ID_PATH = "organizationId";
  /** URI fragment. */
  public static final String VERSION_PARAM = "version";
  /** URI fragment. */
  public static final String VERSION_AS_OF_PARAM = "versionAsOf";
  /** URI fragment. */
  public static final String CORRECTED_TO_PARAM = "correctedTo";

  /**
   * The security source.
   */
  private final OrganizationSource _orgSource;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param organizationSource  the underlying organization source, not null
   */
  public DataOrganizationSourceResource(final OrganizationSource organizationSource) {
    ArgumentChecker.notNull(organizationSource, "organizationSource");
    _orgSource = organizationSource;
  }

  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path(GET_ORGANIZATIONS_URL)
  public Response get(
      @PathParam(ORGANIZATION_ID_PATH) String idStr,
      @QueryParam(VERSION_PARAM) String version,
      @QueryParam(VERSION_AS_OF_PARAM) String versionAsOf,
      @QueryParam(CORRECTED_TO_PARAM) String correctedTo) {

    final ObjectId objectId = ObjectId.parse(idStr);
    if (version != null) {
      return responseOkFudge(_orgSource.get(objectId.atVersion(version)));
    } else {
      final VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
      return responseOkFudge(_orgSource.get(objectId, vc));
    }
  }

  @GET
  @Path(SEARCH_ORGANIZATION_BY_RED_CODE)
  public Response searchRedCode(@QueryParam(RED_CODE_PARAM) String redCode) {
    return responseOkFudge(redCode == null ? null : _orgSource.getOrganizationByRedCode(redCode));
  }

  @GET
  @Path(SEARCH_ORGANIZATION_BY_TICKER)
  public Response searchTicker(@QueryParam(TICKER_PARAM) String ticker) {
    return responseOkFudge(ticker == null ? null : _orgSource.getOrganizationByTicker(ticker));
  }

  public static URI uriGet(URI baseUri, UniqueId uniqueId) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path(GET_ORGANIZATIONS_URL);
    if (uniqueId.getVersion() != null) {
      bld.queryParam(VERSION_PARAM, uniqueId.getVersion());
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
    UriBuilder bld = UriBuilder.fromUri(baseUri).path(GET_ORGANIZATIONS_URL);
    if (vc != null) {
      bld.queryParam(VERSION_AS_OF_PARAM, vc.getVersionAsOfString());
      bld.queryParam(CORRECTED_TO_PARAM, vc.getCorrectedToString());
    }
    return bld.build(objectId);
  }

  public static URI uriSearchByRedCode(URI baseUri, String redCode) {
    return UriBuilder
        .fromUri(baseUri)
        .path(SEARCH_ORGANIZATION_BY_RED_CODE)
        .queryParam(RED_CODE_PARAM, redCode)
        .build();
  }

  public static URI uriSearchByTicker(URI baseUri, String ticker) {
    return UriBuilder
        .fromUri(baseUri)
        .path(SEARCH_ORGANIZATION_BY_TICKER)
        .queryParam(TICKER_PARAM, ticker)
        .build();
  }
}
