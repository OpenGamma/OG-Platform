/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.exchange.impl;

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
import com.opengamma.master.exchange.ExchangeDocument;
import com.opengamma.master.exchange.ExchangeMaster;
import com.opengamma.master.exchange.ExchangeSearchRequest;
import com.opengamma.master.exchange.ExchangeSearchResult;
import com.opengamma.transport.jaxrs.FudgeRest;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for exchanges.
 * <p>
 * The exchanges resource receives and processes RESTful calls to the exchange master.
 */
@Path("/exgMaster")
public class DataExchangesResource extends AbstractDataResource {

  /**
   * The exchange master.
   */
  private final ExchangeMaster _exgMaster;

  /**
   * Creates the resource, exposing the underlying master over REST.
   * 
   * @param exchangeMaster  the underlying exchange master, not null
   */
  public DataExchangesResource(final ExchangeMaster exchangeMaster) {
    ArgumentChecker.notNull(exchangeMaster, "exchangeMaster");
    _exgMaster = exchangeMaster;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the exchange master.
   * 
   * @return the exchange master, not null
   */
  public ExchangeMaster getExchangeMaster() {
    return _exgMaster;
  }

  //-------------------------------------------------------------------------
  @HEAD
  @Path("exchanges")
  public Response status() {
    // simple HEAD to quickly return, avoiding loading the whole database
    return Response.ok().build();
  }

  @GET
  @Path("exchanges")
  public Response search(@Context Providers providers, @QueryParam("msg") String msgBase64) {
    ExchangeSearchRequest request = decodeBean(ExchangeSearchRequest.class, providers, msgBase64);
    ExchangeSearchResult result = getExchangeMaster().search(request);
    return Response.ok(result).build();
  }

  @POST
  @Path("exchanges")
  @Consumes(FudgeRest.MEDIA)
  public Response add(@Context UriInfo uriInfo, ExchangeDocument request) {
    ExchangeDocument result = getExchangeMaster().add(request);
    URI createdUri = DataExchangeResource.uriVersion(uriInfo.getBaseUri(), result.getUniqueId());
    return Response.created(createdUri).entity(result).build();
  }

  //-------------------------------------------------------------------------
  @Path("exchanges/{exchangeId}")
  public DataExchangeResource findExchange(@PathParam("exchangeId") String idStr) {
    ObjectId id = ObjectId.parse(idStr);
    return new DataExchangeResource(this, id);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for all exchanges.
   * 
   * @param baseUri  the base URI, not null
   * @param searchMsg  the search message, may be null
   * @return the URI, not null
   */
  public static URI uri(URI baseUri, String searchMsg) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/exchanges");
    if (searchMsg != null) {
      bld.queryParam("msg", searchMsg);
    }
    return bld.build();
  }

}
