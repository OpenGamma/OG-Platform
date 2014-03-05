/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.provider.livedata.impl;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.provider.livedata.LiveDataMetaDataProvider;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderRequest;
import com.opengamma.provider.livedata.LiveDataMetaDataProviderResult;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for the live data provider.
 * <p>
 * This resource receives and processes RESTful calls to the live data provider.
 */
@Path("liveDataProvider")
public class DataLiveDataMetaDataProviderResource extends AbstractDataResource {

  /**
   * The provider.
   */
  private final LiveDataMetaDataProvider _liveDataProvider;

  /**
   * Creates the resource, exposing the underlying provider over REST.
   * 
   * @param liveDataProvider  the underlying provider, not null
   */
  public DataLiveDataMetaDataProviderResource(final LiveDataMetaDataProvider liveDataProvider) {
    ArgumentChecker.notNull(liveDataProvider, "liveDataProvider");
    _liveDataProvider = liveDataProvider;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the live data provider.
   * 
   * @return the live data provider, not null
   */
  public LiveDataMetaDataProvider getLiveDataProvider() {
    return _liveDataProvider;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @HEAD
  @Path("metaData")
  public Response status() {
    // simple HEAD to quickly return
    return responseOk();
  }

  @GET
  @Path("metaData")
  public Response getMetaData(@Context UriInfo uriInfo) {
    LiveDataMetaDataProviderRequest request = RestUtils.decodeQueryParams(uriInfo, LiveDataMetaDataProviderRequest.class);
    LiveDataMetaDataProviderResult result = getLiveDataProvider().metaData(request);
    return responseOkObject(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param request  the request, not null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, LiveDataMetaDataProviderRequest request) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("metaData");
    if (request != null) {
      RestUtils.encodeQueryParams(bld, request);
    }
    return bld.build();
  }

}
