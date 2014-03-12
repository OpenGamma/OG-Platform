/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.threeten.bp.Instant;

import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinition;
import com.opengamma.financial.analytics.volatility.cube.VolatilityCubeDefinitionSource;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the volatility cube definition source.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
@Path("volatilityCubeDefinitionSource")
public class DataVolatilityCubeDefinitionSourceResource extends AbstractDataResource {

  /**
   * The source.
   */
  private final VolatilityCubeDefinitionSource _source;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param source  the underlying source, not null
   */
  public DataVolatilityCubeDefinitionSourceResource(final VolatilityCubeDefinitionSource source) {
    ArgumentChecker.notNull(source, "source");
    _source = source;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the source.
   *
   * @return the source, not null
   */
  public VolatilityCubeDefinitionSource getVolatilityCubeDefinitionSource() {
    return _source;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context final UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @GET
  @Path("definitions/searchSingle")
  public Response searchSingle(
      @QueryParam("versionAsOf") final String versionAsOfStr,
      @QueryParam("name") final String name) {
    if (versionAsOfStr != null) {
      final VersionCorrection versionCorrection = VersionCorrection.parse(versionAsOfStr, null);
      final VolatilityCubeDefinition<?, ?, ?> result = getVolatilityCubeDefinitionSource().getDefinition(name, versionCorrection);
      return responseOkObject(result);
    }
    final VolatilityCubeDefinition<?, ?, ?> result = getVolatilityCubeDefinitionSource().getDefinition(name);
    return responseOkObject(result);
  }

  /**
   * Builds a URI.
   *
   * @param baseUri  the base URI, not null
   * @param name  the name, not null
   * @param versionAsOf  the version to fetch, null means latest
   * @return the URI, not null
   */
  public static URI uriSearchSingle(final URI baseUri, final String name, final Instant versionAsOf) {
    final UriBuilder bld = UriBuilder.fromUri(baseUri).path("/definitions/searchSingle");
    bld.queryParam("name", name);
    if (versionAsOf != null) {
      bld.queryParam("versionAsOf", versionAsOf.toString());
    }
    return bld.build();
  }

}
