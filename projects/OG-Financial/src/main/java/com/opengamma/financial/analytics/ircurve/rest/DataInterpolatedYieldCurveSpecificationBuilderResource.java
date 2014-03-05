/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecification;
import com.opengamma.financial.analytics.ircurve.InterpolatedYieldCurveSpecificationBuilder;
import com.opengamma.financial.analytics.ircurve.YieldCurveDefinition;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the yield curve source.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
@Path("yieldCurveSpecificationBuilder")
public class DataInterpolatedYieldCurveSpecificationBuilderResource extends AbstractDataResource {

  /**
   * The builder.
   */
  private final InterpolatedYieldCurveSpecificationBuilder _builder;

  /**
   * Creates the resource, exposing the underlying source over REST.
   * 
   * @param builder the underlying source, not null
   */
  public DataInterpolatedYieldCurveSpecificationBuilderResource(final InterpolatedYieldCurveSpecificationBuilder builder) {
    ArgumentChecker.notNull(builder, "builder");
    _builder = builder;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the builder.
   * 
   * @return the builder, not null
   */
  public InterpolatedYieldCurveSpecificationBuilder getInterpolatedYieldCurveSpecificationBuilder() {
    return _builder;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @POST
  @Path("builder/{date}/{version}")
  public Response buildCurve(@PathParam("date") String curveDateStr, @PathParam("version") String versionStr, YieldCurveDefinition definition) {
    final LocalDate curveDate = LocalDate.parse(curveDateStr);
    final VersionCorrection version = VersionCorrection.parse(versionStr);
    InterpolatedYieldCurveSpecification result = getInterpolatedYieldCurveSpecificationBuilder().buildCurve(curveDate, definition, version);
    return responseOkObject(result);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri the base URI, not null
   * @param curveDate the curve date, not null
   * @param version the configuration version, not null
   * @return the URI, not null
   */
  public static URI uriBuildCurve(URI baseUri, LocalDate curveDate, VersionCorrection version) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/builder/{date}/{version}");
    return bld.build(curveDate, version);
  }

}
