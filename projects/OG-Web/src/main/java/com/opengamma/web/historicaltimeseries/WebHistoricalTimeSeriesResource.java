/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.historicaltimeseries;

import java.io.StringWriter;
import java.net.URI;
import java.util.Collection;
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

import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.util.rest.RestUtils;

/**
 * RESTful resource for a historical time-series.
 */
@Path("/timeseries/{timeseriesId}")
public class WebHistoricalTimeSeriesResource extends AbstractWebHistoricalTimeSeriesResource {

  /**
   * Creates the resource.
   * @param parent  the parent resource, not null
   */
  public WebHistoricalTimeSeriesResource(final AbstractWebHistoricalTimeSeriesResource parent) {
    super(parent);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML() {
    FlexiBean out = createRootData();
    return getFreemarker().build(HTML_DIR + "onetimeseries.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON() {
    FlexiBean out = createRootData();
    return getFreemarker().build(JSON_DIR + "onetimeseries.ftl", out);
  }

  @GET
  @Produces(RestUtils.TEXT_CSV)
  public String getCSV() {
    StringWriter stringWriter  = new StringWriter();
    @SuppressWarnings("resource")
    CSVWriter csvWriter = new CSVWriter(stringWriter);
    csvWriter.writeNext(new String[] {"Time", "Value"});
    for (Map.Entry<?, Double> entry : data().getTimeSeries().getTimeSeries()) {
      csvWriter.writeNext(new String[] {entry.getKey().toString(), entry.getValue().toString()});
    }
    return stringWriter.toString();
  }

  @PUT
  @Produces(MediaType.TEXT_HTML)
  public Response put() {
    HistoricalTimeSeriesInfoDocument tsDoc = data().getInfo();
    updateTimeseries(tsDoc.getUniqueId());
    URI uri = uri(data());
    return Response.seeOther(uri).build();
  }

  @PUT
  @Produces(MediaType.APPLICATION_JSON)
  public Response putJSON() {
    HistoricalTimeSeriesInfoDocument tsDoc = data().getInfo();
    Response result = null;
    if (updateTimeseries(tsDoc.getUniqueId())) {
      result =  Response.ok().build();
    } else {
      result = Response.notModified().build();
    }
    return result;
  }

  private boolean updateTimeseries(final UniqueId uniqueId) {
    HistoricalTimeSeriesLoader timeSeriesLoader = data().getHistoricalTimeSeriesLoader();
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
    HistoricalTimeSeriesInfoDocument doc = data().getInfo();
    data().getHistoricalTimeSeriesMaster().remove(doc.getUniqueId());
    URI uri = WebAllHistoricalTimeSeriesResource.uri(data());
    return uri;
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  @Override
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    HistoricalTimeSeriesInfoDocument doc = data().getInfo();
    out.put("infoDoc", doc);
    out.put("info", doc.getInfo());
    out.put("related", getRelatedTimeSeries());
    out.put("timeseries", data().getTimeSeries());
    out.put("deleted", !doc.isLatest());
    return out;
  }

  private Collection<ManageableHistoricalTimeSeriesInfo> getRelatedTimeSeries() {
    HistoricalTimeSeriesInfoSearchRequest searchRequest = 
        new HistoricalTimeSeriesInfoSearchRequest(data().getInfo().getInfo().getExternalIdBundle().toBundle());
    searchRequest.setPagingRequest(PagingRequest.FIRST_PAGE);
    HistoricalTimeSeriesInfoSearchResult searchResult = data().getHistoricalTimeSeriesMaster().search(searchRequest);    
    Collection<ManageableHistoricalTimeSeriesInfo> result = searchResult.getInfoList();
    result.remove(data().getInfo().getInfo()); // remove the original time series itself from its related list
    return result;
  }
  
  //-------------------------------------------------------------------------
  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(final WebHistoricalTimeSeriesData data) {
    return uri(data, null);
  }

  /**
   * Builds a URI for this resource.
   * @param data  the data, not null
   * @param overrideTimeSeriesId  the override historical time-series id, null uses information from data
   * @return the URI, not null
   */
  public static URI uri(final WebHistoricalTimeSeriesData data, final UniqueId overrideTimeSeriesId) {
    String portfolioId = data.getBestHistoricalTimeSeriesUriId(overrideTimeSeriesId);
    return data.getUriInfo().getBaseUriBuilder().path(WebHistoricalTimeSeriesResource.class).build(portfolioId);
  }

}
