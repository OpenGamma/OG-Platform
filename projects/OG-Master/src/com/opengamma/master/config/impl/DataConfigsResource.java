/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.config.impl;

import java.net.URI;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.Providers;

import com.opengamma.id.ObjectId;
import com.opengamma.master.config.ConfigDocument;
import com.opengamma.master.config.ConfigMaster;
import com.opengamma.master.config.ConfigMetaDataRequest;
import com.opengamma.master.config.ConfigMetaDataResult;
import com.opengamma.master.config.ConfigSearchRequest;
import com.opengamma.master.config.ConfigSearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for configs.
 * <p>
 * The configs resource receives and processes RESTful calls to the config master.
 */
@Path("/cfgMaster")
public class DataConfigsResource extends AbstractDataResource {

  /**
   * The config master.
   */
  private final ConfigMaster _cfgMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param configMaster  the underlying config master, not null
   */
  public DataConfigsResource(final ConfigMaster configMaster) {
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
  @Path("metaData")
  public Response metaData(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    ConfigMetaDataRequest request = new ConfigMetaDataRequest();
    if (msgBase64 != null) {
      request = decodeBean(ConfigMetaDataRequest.class, providers, msgBase64);
    }
    ConfigMetaDataResult result = getConfigMaster().metaData(request);
    return Response.ok(result).build();
  }

  @HEAD
  @Path("configs")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return Response.ok().build();
  }

  @GET
  @Path("configs")
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    ConfigSearchRequest<?> request = decodeBean(ConfigSearchRequest.class, providers, msgBase64);
    ConfigSearchResult<?> result = getConfigMaster().search(request);
    return Response.ok(result).build();
  }

  @SuppressWarnings({"rawtypes", "unchecked" })  // necessary to stop Jersey issuing warnings due to <?>
  @POST
  @Path("configs")
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, ConfigDocument request) {
    ConfigDocument<?> result = getConfigMaster().add(request);
    URI createdUri = DataConfigResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId(), result.getType());
    return Response.created(createdUri).entity(result).build();
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
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uriMetaData(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/metaData");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

  /**
   * Builds a URI for all configs.
   * 
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/configs");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
