/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.id.ObjectId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for configs.
 * <p>
 * The configs resource receives and processes RESTful calls to the config master.
 */
@Path("configMaster")
public class DataConfigMasterResource extends AbstractDataResource {

  /**
   * The config master.
   */
  private final ConfigMaster _cfgMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param configMaster  the underlying config master, not null
   */
  public DataConfigMasterResource(final ConfigMaster configMaster) {
    ArgumentChecker.notNull(configMaster, "configMaster");
    _cfgMaster = configMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the config master.
   * 
   * @return the config master, not null
   */
  public ConfigMaster getConfigMaster() {
    return _cfgMaster;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("configs")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return responseOk();
  }

  @GET
  @Path("metaData")
  public Response metaData(@Context UriInfo uriInfo) {
    ConfigMetaDataRequest request = RestUtils.decodeQueryParams(uriInfo, ConfigMetaDataRequest.class);
    ConfigMetaDataResult result = getConfigMaster().metaData(request);
    return responseOkObject(result);
  }

  @SuppressWarnings({"rawtypes", "unchecked" }) // necessary to stop Jersey issuing warnings due to <?>
  @POST
  @Path("configSearches")
  public Response search(ConfigSearchRequest request) {
    ConfigSearchResult<?> result = getConfigMaster().search(request);
    return responseOkObject(result);
  }

  @POST
  @Path("configs")
  public Response add(@Context UriInfo uriInfo, ConfigDocument request) {
    ConfigDocument result = getConfigMaster().add(request);
    URI createdUri = DataConfigResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return responseCreatedObject(createdUri, result);
  }

  //-------------------------------------------------------------------------
  @Path("configs/{configId}")
  public DataConfigResource findConfig(@PathParam("configId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataConfigResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for config meta-data.
   * 
   * @param baseUri  the base URI, not null
   * @param request  the request, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, ConfigMetaDataRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriSearch(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configSearches");
    return bld.build();
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriAdd(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("configs");
    return bld.build();
  }

}
