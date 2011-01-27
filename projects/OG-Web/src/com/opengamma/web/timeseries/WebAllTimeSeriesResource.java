/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.timeseries;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.CalendricalException;
import javax.time.calendar.LocalDate;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.joda.beans.impl.flexi.FlexiBean;

import com.google.common.collect.Lists;
import com.opengamma.id.IdentificationScheme;
import com.opengamma.id.Identifier;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.master.timeseries.TimeSeriesDocument;
import com.opengamma.master.timeseries.TimeSeriesLoader;
import com.opengamma.master.timeseries.TimeSeriesMaster;
import com.opengamma.master.timeseries.TimeSeriesSearchRequest;
import com.opengamma.master.timeseries.TimeSeriesSearchResult;
import com.opengamma.util.db.PagingRequest;
import com.opengamma.web.WebPaging;

/**
 * RESTful resource for all time series.
 * <p>
 * The time series resource represents the whole of a time series master.
 */
@Path("/timeseries")
public class WebAllTimeSeriesResource extends AbstractWebTimeSeriesResource {
  
  private static final String TYPE = "TimeSeries";
  private static final List<String> DATA_FIELDS = Lists.newArrayList("id", "identifiers", "datasource", "dataprovider", "datafield",  "observationtime");

  /**
   * Creates the resource.
   * @param timeSeriesMaster  the time series master, not null
   * @param timeSeriesLoader the timeseries loader, not null
   */
  public WebAllTimeSeriesResource(final TimeSeriesMaster<?> timeSeriesMaster, final TimeSeriesLoader timeSeriesLoader) {
    super(timeSeriesMaster, timeSeriesLoader);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String get(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("identifier") String identifier,
      @QueryParam("dataSource") String dataSource,
      @QueryParam("dataProvider") String dataProvider,
      @QueryParam("dataField") String dataField,
      @QueryParam("observationTime") String observationTime,
      @Context UriInfo uriInfo) {
    FlexiBean out = createSearchResultData(page, pageSize, identifier, dataSource, dataProvider, dataField, observationTime, uriInfo);
    return getFreemarker().build("timeseries/alltimeseries.ftl", out);
  }
  
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("page") int page,
      @QueryParam("pageSize") int pageSize,
      @QueryParam("identifier") String identifier,
      @QueryParam("dataSource") String dataSource,
      @QueryParam("dataProvider") String dataProvider,
      @QueryParam("dataField") String dataField,
      @QueryParam("observationTime") String observationTime,
      @Context UriInfo uriInfo) {
    String result = null;
    FlexiBean out = createSearchResultData(page, pageSize, identifier, dataSource, dataProvider, dataField, observationTime, uriInfo);
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      TimeSeriesSearchResult<?> searchResult = (TimeSeriesSearchResult<?>) out.get("searchResult");
      result = getJSONOutputter().buildJSONSearchResult(TYPE, DATA_FIELDS, searchResult);
    }
    return result;
  }

  private FlexiBean createSearchResultData(int page, int pageSize, String identifier, String dataSource, String dataProvider, String dataField, String observationTime, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    TimeSeriesSearchRequest searchRequest = new TimeSeriesSearchRequest();
    searchRequest.setPagingRequest(PagingRequest.of(page, pageSize));
    searchRequest.setIdentifierValue(StringUtils.trimToNull(identifier));
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
    return out;
  }
  
//-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  public Response post(
      @FormParam("dataProvider") String dataProvider,
      @FormParam("dataField") String dataField,
      @FormParam("start") String start,
      @FormParam("end") String end,
      @FormParam("idscheme") String idScheme,
      @FormParam("idvalue") String idValue) {
    idScheme = StringUtils.trimToNull(idScheme);
    idValue = StringUtils.trimToNull(idValue);
    dataField = StringUtils.trimToNull(dataField);
    start = StringUtils.trimToNull(start);
    end = StringUtils.trimToNull(end);
    dataProvider = StringUtils.trimToNull(dataProvider);
    
    FlexiBean out = createRootData();
    LocalDate startDate = null;
    boolean invalidStartDate = false;
    if (start != null) {
      try {
        startDate = LocalDate.parse(start);
      } catch (CalendricalException e) {
        out.put("err_startInvalid", true);
        invalidStartDate = true;
      }
    }
    LocalDate endDate = null;
    boolean invalidEndDate = false;
    if (end != null) {
      try {
        endDate = LocalDate.parse(end);
      } catch (CalendricalException e) {
        out.put("err_endInvalid", true);
        invalidEndDate = true;
      }
    }
    
    if (dataField == null || idValue == null || invalidStartDate || invalidEndDate) {
      //data for repopulating the form
      out.put("scheme", idScheme);
      out.put("dataField", dataField);
      out.put("idValue", idValue);
      out.put("dataProvider", dataProvider);
      out.put("start", start);
      out.put("end", end);
      
      if (dataField == null) {
        out.put("err_iddatafieldMissing", true);
      }
      if (idValue == null) {
        out.put("err_idvalueMissing", true);
      } 
      String html = getFreemarker().build("timeseries/timeseries-add.ftl", out);
      return Response.ok(html).build();
    }
    
    IdentificationScheme scheme = IdentificationScheme.of(idScheme);
    Set<Identifier> identifiers = buildSecurityRequest(scheme, idValue);
    TimeSeriesLoader timeSeriesLoader = data().getTimeSeriesLoader();
    Map<Identifier, UniqueIdentifier> added = timeSeriesLoader.loadTimeSeries(identifiers, dataProvider, dataField, startDate, endDate);
    
    URI uri = null;
    if (identifiers.size() == 1) {
      UniqueIdentifier uid = added.get(identifiers.iterator().next());
      uri = data().getUriInfo().getAbsolutePathBuilder().path(uid.toString()).build();
    } else {
      uri = uri(data(), identifiers);
    }
    return Response.seeOther(uri).build();
  }
  
  private Set<Identifier> buildSecurityRequest(final IdentificationScheme identificationScheme, final String idValue) {
    if (idValue == null) {
      return Collections.emptySet();
    }
    final String[] identifiers = StringUtils.split(idValue, "\n");
    final Set<Identifier> result = new HashSet<Identifier>(identifiers.length);
    for (String identifier : identifiers) {
      identifier = StringUtils.trimToNull(identifier);
      if (identifier != null) {
        result.add(Identifier.of(identificationScheme, identifier));
      }
    }
    return result;
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
  
  /**
   * Builds a URI for collection of timeseries.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(WebTimeSeriesData data, Collection<Identifier> identifiers) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebAllTimeSeriesResource.class);
    if (identifiers != null) {
      Iterator<Identifier> it = identifiers.iterator();
      for (int i = 0; it.hasNext(); i++) {
        Identifier id = it.next();
        builder.queryParam("idscheme." + i, id.getScheme().getName());
        builder.queryParam("idvalue." + i, id.getValue());
      }
    }
    return builder.build();
  }

}
