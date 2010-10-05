/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.web.timeseries;

import java.net.URI;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.timeseries.TimeSeriesDocument;
import com.opengamma.id.UniqueIdentifier;

/**
 * RESTful resource for a time series.
 */
@Path("/timeseries/{timeseriesId}")
public class WebOneTimeSeriesResource extends AbstractWebTimeSeriesResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebOneTimeSeriesResource(final AbstractWebTimeSeriesResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get() {
    FlexiBean out = createRootData();
    return getFreemarker().build("timeseries/onetimeseries.ftl", out);
  }

  @DELETE
  public Response delete() {
    TimeSeriesDocument<?> doc = data().getTimeSeries();
    data().getTimeSeriesMaster().removeTimeSeries(doc.getUniqueIdentifier());
    URI uri = WebAllTimeSeriesResource.uri(data());
    return Response.seeOther(uri).build();
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    TimeSeriesDocument<?> doc = data().getTimeSeries();
    out.put("timeseriesDoc", doc);
    out.put("timeseries", doc.getTimeSeries());
    return out;
  }

  //-------------------------------------------------------------------------
  @Path("versions")
  public WebTimeSeriesVersionsResource findVersions() {
    return new WebTimeSeriesVersionsResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebTimeSeriesData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideTimeSeriesId  the override time series id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebTimeSeriesData data, final UniqueIdentifier overrideTimeSeriesId) {
    String portfolioId = data.getBestTimeSeriesUriId(overrideTimeSeriesId);
    return data.getUriInfo().getBaseUriBuilder().path(WebOneTimeSeriesResource.class).build(portfolioId);
  }

}
