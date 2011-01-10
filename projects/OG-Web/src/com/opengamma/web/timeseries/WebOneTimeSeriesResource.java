/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.web.timeseries;

import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.beans.impl.flexi.FlexiBean;

import au.com.bytecode.opencsv.CSVWriter;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.timeseries.TimeSeriesDocument;

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
  
  @GET
  @Produces("text/csv")
  public String getCsv() {
    StringWriter stringWriter  = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(stringWriter);
    csvWriter.writeNext(new String[] {"Time", "Value"});
    for (Map.Entry<?, Double> entry : data().getTimeSeries().getTimeSeries()) {
      csvWriter.writeNext(new String[] {entry.getKey().toString(), entry.getValue().toString()});
    }
    return stringWriter.toString();
  }

  @DELETE
  public Response delete() {
    TimeSeriesDocument<?> doc = data().getTimeSeries();
    data().getTimeSeriesMaster().removeTimeSeries(doc.getUniqueId());
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
