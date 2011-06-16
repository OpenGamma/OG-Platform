/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.timeseries;

import java.io.StringWriter;
import java.net.URI;
import java.util.Map;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.joda.beans.impl.flexi.FlexiBean;

import au.com.bytecode.opencsv.CSVWriter;

import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.timeseries.HistoricalDataDocument;
import com.opengamma.master.timeseries.HistoricalDataLoader;

/**
 * RESTful resource for a historical data.
 */
@Path("/timeseries/{timeseriesId}")
public class WebHistoricalDataResource extends AbstractWebHistoricalDataResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebHistoricalDataResource(final AbstractWebHistoricalDataResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build("timeseries/onetimeseries.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    FlexiBean out = createRootData();
    return getFreemarker().build("timeseries/jsononetimeseries.ftl", out);
  }

  @GET
  @Produces("text/csv")
  public String getCSV() {
    StringWriter stringWriter  = new StringWriter();
    CSVWriter csvWriter = new CSVWriter(stringWriter);
    csvWriter.writeNext(new String[] {"Time", "Value"});
    for (Map.Entry<?, Double> entry : data().getTimeSeries().getTimeSeries()) {
      csvWriter.writeNext(new String[] {entry.getKey().toString(), entry.getValue().toString()});
    }
    return stringWriter.toString();
  }
  
  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON() {
    HistoricalDataDocument tsDoc = data().getTimeSeries();
    Response result = null;
    if (updateTimeseries(tsDoc.getUniqueId())) {
      result =  Response.ok().build();
    } else {
      result = Response.notModified().build();
    }
    return result;
  }

  private boolean updateTimeseries(final UniqueIdentifier uniqueId) {
    HistoricalDataLoader timeSeriesLoader = data().getTimeSeriesLoader();
    return timeSeriesLoader.updateTimeSeries(uniqueId);
  }

  //-------------------------------------------------------------------------
  @DELETE
  @Produces(MediaType.TEXT_HTML)
  public Response delete() {
    URI uri = deleteTimeSeries();
    return Response.seeOther(uri).build();
  }

  @DELETE
  @Produces(MediaType.APPLICATION_JSON)
  public Response deleteJSON() {
    deleteTimeSeries();
    return Response.ok().build();
  }

  private URI deleteTimeSeries() {
    HistoricalDataDocument doc = data().getTimeSeries();
    data().getTimeSeriesMaster().remove(doc.getUniqueId());
    URI uri = WebAllHistoricalDataResource.uri(data());
    return uri;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    HistoricalDataDocument doc = data().getTimeSeries();
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
  public static URI uri(final WebHistoricalDataData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideTimeSeriesId  the override historical data id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebHistoricalDataData data, final UniqueIdentifier overrideTimeSeriesId) {
    String portfolioId = data.getBestTimeSeriesUriId(overrideTimeSeriesId);
    return data.getUriInfo().getBaseUriBuilder().path(WebHistoricalDataResource.class).build(portfolioId);
  }

}
