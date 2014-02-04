/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.auth.master.portfolio.rest;

import com.opengamma.auth.AuthorisationException;
import com.opengamma.auth.master.portfolio.PortfolioCapability;
import com.opengamma.auth.master.portfolio.SecurePortfolioMaster;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.portfolio.PortfolioDocument;
import com.opengamma.master.portfolio.PortfolioHistoryRequest;
import com.opengamma.master.portfolio.PortfolioHistoryResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.List;

/**
 * RESTful resource for a portfolio.
 */
public class DataSecurePortfolioResource
    extends AbstractDataResource {

  /**
   * The portfolios resource.
   */
  private final DataSecurePortfolioMasterResource _portfoliosResource;
  /**
   * The identifier specified in the URI.
   */
  private ObjectId _urlResourceId;

  /**
   * Creates dummy resource for the purpose of url resolution.
   */
  public DataSecurePortfolioResource() {
    _portfoliosResource = null;
  }

  /**
   * Creates the resource.
   *
   * @param portfoliosResource the parent resource, not null
   * @param portfolioId        the portfolio unique identifier, not null
   */
  public DataSecurePortfolioResource(final DataSecurePortfolioMasterResource portfoliosResource, final ObjectId portfolioId) {
    ArgumentChecker.notNull(portfoliosResource, "portfoliosResource");
    ArgumentChecker.notNull(portfolioId, "portfolio");
    _portfoliosResource = portfoliosResource;
    _urlResourceId = portfolioId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the portfolios resource.
   *
   * @return the portfolios resource, not null
   */
  public DataSecurePortfolioMasterResource getPortfoliosResource() {
    return _portfoliosResource;
  }

  /**
   * Gets the portfolio identifier from the URL.
   *
   * @return the unique identifier, not null
   */
  public ObjectId getUrlId() {
    return _urlResourceId;
  }

  //-------------------------------------------------------------------------

  /**
   * Gets the portfolio master.
   *
   * @return the portfolio master, not null
   */
  public SecurePortfolioMaster getMaster() {
    return getPortfoliosResource().getPortfolioMaster();
  }

  @GET
  @Path("versions")
  public Response history(@HeaderParam("Capability") String portfolioCapabilityStr, @Context UriInfo uriInfo) {
    PortfolioHistoryRequest request = RestUtils.decodeQueryParams(uriInfo, PortfolioHistoryRequest.class);
    if (getUrlId().equals(request.getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectId does not match URI");
    }
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    PortfolioHistoryResult result = getMaster().history(portfolioCapability, request);
    return responseOkFudge(result);
  }

  protected String getResourceName() {
    return "portfolios";
  }

  //===================== ROUTING HELPERS ==============================================================================

  @GET
  @Produces(FudgeRest.MEDIA)
  public Response get(@HeaderParam("Capability") String portfolioCapabilityStr, @QueryParam("versionAsOf") String versionAsOf, @QueryParam("correctedTo") String correctedTo, @Context Request rq) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    PortfolioDocument result = getMaster().get(portfolioCapability, getUrlId(), vc);
    return responseOkFudge(result);
  }

  @POST
  public Response update(@HeaderParam("Capability") String portfolioCapabilityStr, @Context UriInfo uriInfo, PortfolioDocument request) {
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    if (getUrlId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectIdentifiable does not match URI");
    }
    if (portfolioCapability == null) {
      throw new AuthorisationException("Capability must be provided with api call");
    }
    PortfolioDocument result = getMaster().update(portfolioCapability, request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedFudge(uri, result);
  }

  @DELETE
  public void remove(@HeaderParam("Capability") String portfolioCapabilityStr) {
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    getMaster().remove(portfolioCapability, getUrlId().atLatestVersion());
  }


  @GET
  @Path("versions/{versionId}")
  public Response getVersioned(@HeaderParam("Capability") String portfolioCapabilityStr, @PathParam("versionId") String versionId) {
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    UniqueId uniqueId = getUrlId().atVersion(versionId);
    PortfolioDocument result = getMaster().get(portfolioCapability, uniqueId);
    return responseOkFudge(result);
  }


  @PUT
  @Path("versions/{versionId}")
  public Response replaceVersion(@HeaderParam("Capability") String portfolioCapabilityStr, @PathParam("versionId") String versionId, List<PortfolioDocument> replacementDocuments) {
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    UniqueId uniqueId = getUrlId().atVersion(versionId);

    List<UniqueId> result = getMaster().replaceVersion(portfolioCapability, uniqueId, replacementDocuments);
    return responseOkFudge(result);
  }

  @PUT
  public Response replaceVersions(@HeaderParam("Capability") String portfolioCapabilityStr, List<PortfolioDocument> replacementDocuments) {
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    ObjectId objectId = getUrlId();
    List<UniqueId> result = getMaster().replaceVersions(portfolioCapability, objectId, replacementDocuments);
    return responseOkFudge(result);
  }

  @PUT
  @Path("all")
  public Response replaceAllVersions(@HeaderParam("Capability") String portfolioCapabilityStr, List<PortfolioDocument> replacementDocuments) {
    ObjectId objectId = getUrlId();
    PortfolioCapability portfolioCapability = RestUtils.decodeBase64(PortfolioCapability.class, portfolioCapabilityStr);
    List<UniqueId> result = getMaster().replaceAllVersions(portfolioCapability, objectId, replacementDocuments);
    return responseOkFudge(result);
  }

  //====================================================================================================================

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri            the base URI, not null
   * @param objectIdentifiable the object identifier, not null
   * @param vc                 the version-correction locator, null for latest
   * @return the URI, not null
   */
  public URI uri(URI baseUri, ObjectIdentifiable objectIdentifiable, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectIdentifiable.getObjectId());
  }

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId the object identifier, not null
   * @param vc       the version-correction locator, null for latest
   * @return the URI, not null
   */
  // TODO replace URI with something better
  public URI uriAll(URI baseUri, ObjectIdentifiable objectId, VersionCorrection vc) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}/all");
    if (vc != null) {
      bld.queryParam("versionAsOf", vc.getVersionAsOfString());
      bld.queryParam("correctedTo", vc.getCorrectedToString());
    }
    return bld.build(objectId.getObjectId());
  }


  /**
   * Builds a URI for the versions of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectId the object identifier, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public URI uriVersions(URI baseUri, ObjectIdentifiable objectId, Object request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}/versions");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build(objectId.getObjectId());
  }

  /**
   * Builds a URI for a specific version of the resource.
   *
   * @param baseUri  the base URI, not null
   * @param uniqueId the unique identifier, not null
   * @return the URI, not null
   */
  public URI uriVersion(URI baseUri, UniqueId uniqueId) {
    if (uniqueId.isLatest()) {
      return uri(baseUri, uniqueId, null);
    }
    return UriBuilder.fromUri(baseUri).path("/" + getResourceName() + "/{id}/versions/{versionId}")
        .build(uniqueId.toLatest(), uniqueId.getVersion());
  }


}
