/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.function.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import com.opengamma.engine.function.config.RepositoryConfiguration;
import com.opengamma.engine.function.config.RepositoryConfigurationSource;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for repository configuration.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
@Path("/repoConfigSource")
public class DataRepositoryConfigurationSourceResource extends AbstractDataResource {

  /**
   * The source.
   */
  private final RepositoryConfigurationSource _source;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param source  the underlying source, not null
   */
  public DataRepositoryConfigurationSourceResource(final RepositoryConfigurationSource source) {
    ArgumentChecker.notNull(source, "source");
    _source = source;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the source.
   * 
   * @return the source, not null
   */
  public RepositoryConfigurationSource getRepositoryConfigurationSource() {
    return _source;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("repoConfigs/all")
  public Response getAll() {
    RepositoryConfiguration result = getRepositoryConfigurationSource().getRepositoryConfiguration();
    return responseOkFudge(result);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @return the URI, not null
   */
  public static URI uriGetAll(URI baseUri) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/repoConfigs/all");
    return bld.build();
  }

}
