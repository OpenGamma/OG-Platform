/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.curve.rest;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDate;

import com.opengamma.financial.analytics.curve.CurveDefinition;
import com.opengamma.financial.analytics.curve.CurveSpecification;
import com.opengamma.financial.analytics.curve.credit.CurveSpecificationBuilder;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.rest.AbstractDataResource;

/**
 * RESTful resource for the yield curve source.
 * <p>
 * This resource receives and processes RESTful calls to the source.
 */
@Path("curveSpecificationBuilder")
public class DataCurveSpecificationBuilderResource extends AbstractDataResource {

  /**
   * The builder.
   */
  private final CurveSpecificationBuilder _builder;

  /**
   * Creates the resource, exposing the underlying source over REST.
   *
   * @param builder  the underlying source, not null
   */
  public DataCurveSpecificationBuilderResource(final CurveSpecificationBuilder builder) {
    ArgumentChecker.notNull(builder, "builder");
    _builder = builder;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the builder.
   * 
   * @return the builder, not null
   */
  public CurveSpecificationBuilder getCurveSpecificationBuilder() {
    return _builder;
  }

  //-------------------------------------------------------------------------
  @GET
  public Response getHateaos(@Context UriInfo uriInfo) {
    return hateoasResponse(uriInfo);
  }

  @POST
  @Path("builder/{valuationTime}/{date}")
  public Response buildCurve(
      @PathParam("valuationTime") String valuationTimeStr,
      @PathParam("date") String curveDateStr,
      CurveDefinition definition) {
    final Instant valuationTime = Instant.parse(valuationTimeStr);
    final LocalDate curveDate = LocalDate.parse(curveDateStr);
    CurveSpecification result = getCurveSpecificationBuilder().buildCurve(valuationTime, curveDate, definition);
    return responseOkObject(result);
  }

  /**
   * Builds a URI.
   * 
   * @param baseUri  the base URI, not null
   * @param valuationTime  the valuation time, not null
   * @param curveDate  the curve date, not null
   * @return the URI, not null
   */
  public static URI uriBuildCurve(URI baseUri, Instant valuationTime, LocalDate curveDate) {
    UriBuilder bld = UriBuilder.fromUri(baseUri).path("/builder/{valuationTime}/{date}");
    return bld.build(valuationTime, curveDate);
  }

}
