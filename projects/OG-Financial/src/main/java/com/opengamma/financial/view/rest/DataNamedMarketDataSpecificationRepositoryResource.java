/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.view.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for {@link NamedMarketDataSpecificationRepository}
 */
public class DataNamedMarketDataSpecificationRepositoryResource extends AbstractDataResource {

  private static final String PATH_NAMES = "names";
  private static final String PATH_SPECIFICATION = "specification";

  private final NamedMarketDataSpecificationRepository _repository;

  public DataNamedMarketDataSpecificationRepositoryResource(final NamedMarketDataSpecificationRepository namedMarketDataSpecificationRepository) {
    _repository = namedMarketDataSpecificationRepository;
  }

  @GET
  @Path(PATH_NAMES)
  public Response getProviderNames() {
    return responseOkObject(getNamedMarketDataSpecificationRepository().getNames());
  }

  @GET
  @Path(PATH_SPECIFICATION + "/{name}")
  public Response getSpecification(@PathParam("name") final String name) {
    return responseOkObject(getNamedMarketDataSpecificationRepository().getSpecification(name));
  }

  //-------------------------------------------------------------------------
  public static URI uriNames(final URI baseUri) {
    return UriBuilder.fromUri(baseUri).path(PATH_NAMES).build();
  }

  public static URI uriSpecification(final URI baseUri, final String name) {
    return UriBuilder.fromUri(baseUri).path(PATH_SPECIFICATION).path(name).build();
  }

  //-------------------------------------------------------------------------
  private NamedMarketDataSpecificationRepository getNamedMarketDataSpecificationRepository() {
    return _repository;
  }

}
