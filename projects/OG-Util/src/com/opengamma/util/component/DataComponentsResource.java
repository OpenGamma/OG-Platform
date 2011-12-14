/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.util.component;

import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Providers;

import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for exchanges.
 * <p>
 * The exchanges resource receives and processes RESTful calls to the exchange master.
 */
@Path("/data/components")
public class DataComponentsResource extends AbstractDataResource {

  /**
   * The repository.
   */
  private final ComponentRepository _repo;

  /**
   * Creates the resource around the repository.
   * 
   * @param repo  the component repository, not null
   */
  public DataComponentsResource(final ComponentRepository repo) {
    ArgumentChecker.notNull(repo, "repo");
    _repo = repo;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the repository.
   * 
   * @return the repository, not null
   */
  public ComponentRepository getComponentRepository() {
    return _repo;
  }

  //-------------------------------------------------------------------------
  @HEAD
  public Response status() {
    // simple GET to quickly return as a ping
    return Response.ok().build();
  }

  @GET
  public Response search(@QueryParam("type") String type) {
    
    
    ExchangeSearchRequest request = decodeBean(ExchangeSearchRequest.class, providers, msgBase64);
    ExchangeSearchResult result = getExchangeMaster().search(request);
    return Response.ok(result).build();
  }

}
