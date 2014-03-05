/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master;

import java.net.URI;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * Abstract base class for RESTful resources.
 * 
 * @param <D>  the type of the document
 */
public abstract class AbstractDocumentDataResource<D extends AbstractDocument> extends AbstractDataResource {

  protected abstract AbstractMaster<D> getMaster();

  protected abstract String getResourceName();

  protected abstract ObjectId getUrlId();

  //===================== ROUTING HELPERS ==============================================================================
  
  // @GET
  protected Response get(/*@QueryParam("versionAsOf")*/ String versionAsOf, /*@QueryParam("correctedTo")*/ String correctedTo) {
    VersionCorrection vc = VersionCorrection.parse(versionAsOf, correctedTo);
    D result = getMaster().get(getUrlId(), vc);
    return responseOkObject(result);
  }

  // @POST
  protected Response update(/*@Context*/ UriInfo uriInfo, D request) {
    if (getUrlId().equals(request.getUniqueId().getObjectId()) == false) {
      throw new IllegalArgumentException("Document objectIdentifiable does not match URI");
    }
    D result = getMaster().update(request);
    URI uri = uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(uri, result);
  }

  // @DELETE
  protected void remove() {
    getMaster().remove(getUrlId().atLatestVersion());
  }


  // @GET
  // @Path("versions/{versionId}")
  protected Response getVersioned(/*@PathParam("versionId")*/ String versionId) {
    UniqueId uniqueId = getUrlId().atVersion(versionId);
    D result = getMaster().get(uniqueId);
    return responseOkObject(result);
  }


  // @PUT
  // @Path("versions/{versionId}")
  protected Response replaceVersion(/*@PathParam("versionId")*/ String versionId, List<D> replacementDocuments) {
    UniqueId uniqueId = getUrlId().atVersion(versionId);

    List<UniqueId> result = getMaster().replaceVersion(uniqueId, replacementDocuments);
    return responseOkObject(result);
  }

  // @PUT
  protected Response replaceVersions(List<D> replacementDocuments) {
    ObjectId objectId = getUrlId();
    List<UniqueId> result = getMaster().replaceVersions(objectId, replacementDocuments);
    return responseOkObject(result);
  }

  // @PUT
  // @Path("all")
  protected Response replaceAllVersions(List<D> replacementDocuments) {
    ObjectId objectId = getUrlId();
    List<UniqueId> result = getMaster().replaceAllVersions(objectId, replacementDocuments);
    return responseOkObject(result);
  }
  
  //====================================================================================================================

  /**
   * Builds a URI for the resource.
   *
   * @param baseUri  the base URI, not null
   * @param objectIdentifiable  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
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
   * @param objectId  the object identifier, not null
   * @param vc  the version-correction locator, null for latest
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
   * @param objectId  the object identifier, not null
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
   * @param uniqueId  the unique identifier, not null
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
