/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.web.timeseries;

import java.net.URI;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.opengamma.financial.timeseries.TimeSeriesDocument;
import com.opengamma.financial.timeseries.TimeSeriesMaster;
import com.opengamma.financial.timeseries.TimeSeriesSearchRequest;
import com.opengamma.financial.timeseries.TimeSeriesSearchResult;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.util.rest.WebPaging;

/**
 * RESTful resource for all time series.
 * <p>
 * The time series resource represents the whole of a time series master.
 */
@Path("/timeseries")
public class WebAllTimeSeriesResource extends AbstractWebTimeSeriesResource {

  /**
   * Creates the resource.
   * @param timeSeriesMaster  the time series master, not null
   */
  public WebAllTimeSeriesResource(final TimeSeriesMaster<?> timeSeriesMaster) {
    super(timeSeriesMaster);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("dataSource") String dataSource,
      @QueryParam("dataProvider") String dataProvider,
      @QueryParam("dataField") String dataField,
      @QueryParam("observationTime") String observationTime,
      @Context UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    TimeSeriesSearchRequest searchRequest = new TimeSeriesSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setDataSource(StringUtils.trimToNull(dataSource));
    searchRequest.setDataProvider(StringUtils.trimToNull(dataProvider));
    searchRequest.setDataField(StringUtils.trimToNull(dataField));
    searchRequest.setObservationTime(StringUtils.trimToNull(observationTime));
    MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      Identifier id = Identifier.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.getIdentifiers().add(id);
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      TimeSeriesSearchResult<?> searchResult = data().getTimeSeriesMaster().searchTimeSeries(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), data().getUriInfo()));
    }
    return getFreemarker().build("timeseries/alltimeseries.ftl", out);
  }

  //-------------------------------------------------------------------------
  @Path("{timeseriesId}")
  public WebOneTimeSeriesResource findPortfolio(@PathParam("timeseriesId") String idStr) {
    data().setUriTimeSeriesId(idStr);
    TimeSeriesDocument<?> portfolio = data().getTimeSeriesMaster().getTimeSeries(UniqueIdentifier.parse(idStr));
    data().setTimeSeries(portfolio);
    return new WebOneTimeSeriesResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    TimeSeriesSearchRequest<?> searchRequest = new TimeSeriesSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for time series.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebTimeSeriesData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebAllTimeSeriesResource.class).build();
  }

}
