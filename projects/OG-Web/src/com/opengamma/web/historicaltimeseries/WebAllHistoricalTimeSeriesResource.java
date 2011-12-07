/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.historicaltimeseries;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalScheme;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoader;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.util.paging.PagingRequest;
import com.opengamma.web.WebPaging;
import com.sun.jersey.api.client.ClientResponse.Status;

/**
 * RESTful resource for all historical time-series.
 * <p>
 * This resource represents the whole of a historical time-series master.
 */
@Path("/timeseries")
public class WebAllHistoricalTimeSeriesResource extends AbstractWebHistoricalTimeSeriesResource {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(WebAllHistoricalTimeSeriesResource.class);

  /**
   * Creates the resource.
   * @param master  the historical time-series master, not null
   * @param loader  the historical time-series loader, not null
   */
  public WebAllHistoricalTimeSeriesResource(final HistoricalTimeSeriesMaster master, final HistoricalTimeSeriesLoader loader) {
    super(master, loader);
  }

  //-------------------------------------------------------------------------
  @GET
  @Produces(MediaType.TEXT_HTML)
  public String getHTML(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("identifier") String identifier,
      @QueryParam("dataSource") String dataSource,
      @QueryParam("dataProvider") String dataProvider,
      @QueryParam("dataField") String dataField,
      @QueryParam("observationTime") String observationTime,
      @QueryParam("name") String name,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    FlexiBean out = createSearchResultData(pr, identifier, dataSource, dataProvider, dataField, observationTime, name, uriInfo);
    return getFreemarker().build("timeseries/alltimeseries.ftl", out);
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public String getJSON(
      @QueryParam("pgIdx") Integer pgIdx,
      @QueryParam("pgNum") Integer pgNum,
      @QueryParam("pgSze") Integer pgSze,
      @QueryParam("identifier") String identifier,
      @QueryParam("dataSource") String dataSource,
      @QueryParam("dataProvider") String dataProvider,
      @QueryParam("dataField") String dataField,
      @QueryParam("observationTime") String observationTime,
      @QueryParam("name") String name,
      @Context UriInfo uriInfo) {
    PagingRequest pr = buildPagingRequest(pgIdx, pgNum, pgSze);
    FlexiBean out = createSearchResultData(pr, identifier, dataSource, dataProvider, dataField, observationTime, name, uriInfo);
    return getFreemarker().build("timeseries/jsonalltimeseries.ftl", out);
  }

  private FlexiBean createSearchResultData(PagingRequest pr, String identifier, String dataSource, String dataProvider, String dataField, String observationTime, String name, UriInfo uriInfo) {
    FlexiBean out = createRootData();
    
    HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    searchRequest.setPagingRequest(pr);
    searchRequest.setExternalIdValue(StringUtils.trimToNull(identifier));
    searchRequest.setDataSource(StringUtils.trimToNull(dataSource));
    searchRequest.setDataProvider(StringUtils.trimToNull(dataProvider));
    searchRequest.setDataField(StringUtils.trimToNull(dataField));
    searchRequest.setObservationTime(StringUtils.trimToNull(observationTime));
    searchRequest.setName(StringUtils.trimToNull(name));
    MultivaluedMap<String, String> query = uriInfo.getQueryParameters();
    for (int i = 0; query.containsKey("idscheme." + i) && query.containsKey("idvalue." + i); i++) {
      ExternalId id = ExternalId.of(query.getFirst("idscheme." + i), query.getFirst("idvalue." + i));
      searchRequest.addExternalId(id);
    }
    out.put("searchRequest", searchRequest);
    
    if (data().getUriInfo().getQueryParameters().size() > 0) {
      HistoricalTimeSeriesInfoSearchResult searchResult = data().getHistoricalTimeSeriesMaster().search(searchRequest);
      out.put("searchResult", searchResult);
      out.put("paging", new WebPaging(searchResult.getPaging(), data().getUriInfo()));
    }
    return out;
  }

  //-------------------------------------------------------------------------
  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.TEXT_HTML)
  public Response postHTML(
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
    boolean validStartDate = true;
    if (start != null) {
      try {
        startDate = LocalDate.parse(start);
      } catch (CalendricalException e) {
        out.put("err_startInvalid", true);
        validStartDate = false;
      }
    }
    LocalDate endDate = null;
    boolean validEndDate = true;
    if (end != null) {
      try {
        endDate = LocalDate.parse(end);
      } catch (CalendricalException e) {
        out.put("err_endInvalid", true);
        validEndDate = false;
      }
    }
    
    if (dataField == null || idValue == null || !validStartDate || !validEndDate) {
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
    
    ExternalScheme scheme = ExternalScheme.of(idScheme);
    Set<ExternalId> identifiers = buildSecurityRequest(scheme, idValue);
    Map<ExternalId, UniqueId> added = addTimeSeries(dataProvider, dataField, identifiers, startDate, endDate);
    
    URI uri = null;
    if (!identifiers.isEmpty()) {
      if (identifiers.size() == 1) {
        ExternalId requestIdentifier = identifiers.iterator().next();
        UniqueId uniqueId = added.get(requestIdentifier);
        if (uniqueId != null) {
          uri = data().getUriInfo().getAbsolutePathBuilder().path(uniqueId.toString()).build();
        } else {
          s_logger.warn("No time-series added for {} ", requestIdentifier);
          uri = uri(data());
        }
      } else {
        uri = uri(data(), identifiers);
      }
    } 
    return Response.seeOther(uri).build();
  }

  @POST
  @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
  @Produces(MediaType.APPLICATION_JSON)
  public Response postJSON(
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
    
    LocalDate startDate = null;
    boolean validStartDate = true;
    if (start != null) {
      try {
        startDate = LocalDate.parse(start);
        validStartDate = true;
      } catch (CalendricalException e) {
        validStartDate = false;
      }
    }
    LocalDate endDate = null;
    boolean validEndDate = true;
    if (end != null) {
      try {
        endDate = LocalDate.parse(end);
        validEndDate = true;
      } catch (CalendricalException e) {
        validEndDate = false;
      }
    }
    
    if (dataField == null || idValue == null || !validStartDate || !validEndDate) {
      return Response.status(Status.BAD_REQUEST).build();
    }
    
    ExternalScheme scheme = ExternalScheme.of(idScheme);
    Set<ExternalId> identifiers = buildSecurityRequest(scheme, idValue);
    Map<ExternalId, UniqueId> added = addTimeSeries(dataProvider, dataField, identifiers, startDate, endDate);
    
    URI uri = null;
    if (!identifiers.isEmpty()) {
      if (identifiers.size() == 1) {
        ExternalId requestIdentifier = identifiers.iterator().next();
        UniqueId uniqueId = added.get(requestIdentifier);
        if (uniqueId != null) {
          uri = data().getUriInfo().getAbsolutePathBuilder().path(uniqueId.toString()).build();
        } else {
          s_logger.warn("No time-series added for {} ", requestIdentifier);
          uri = uri(data());
        }
      } else {
        uri = uri(data(), identifiers);
      }
    } 
    
    return Response.created(uri).build();
  }

  private Map<ExternalId, UniqueId> addTimeSeries(String dataProvider, String dataField, Set<ExternalId> identifiers, LocalDate startDate, LocalDate endDate) {
    HistoricalTimeSeriesLoader loader = data().getHistoricalTimeSeriesLoader();
    Map<ExternalId, UniqueId> added = Maps.newHashMap();
    if (!identifiers.isEmpty()) {
      added = loader.addTimeSeries(identifiers, dataProvider, dataField, startDate, endDate);
    }
    return added;
  }

  private Set<ExternalId> buildSecurityRequest(final ExternalScheme identificationScheme, final String idValue) {
    if (idValue == null) {
      return Collections.emptySet();
    }
    final String[] identifiers = StringUtils.split(idValue, "\n");
    final Set<ExternalId> result = new HashSet<ExternalId>(identifiers.length);
    for (String identifier : identifiers) {
      identifier = StringUtils.trimToNull(identifier);
      if (identifier != null) {
        result.add(ExternalId.of(identificationScheme, identifier));
      }
    }
    return result;
  }

  //-------------------------------------------------------------------------
  @Path("{timeseriesId}")
  public WebHistoricalTimeSeriesResource findSeries(@PathParam("timeseriesId") String idStr) {
    data().setUriHistoricalTimeSeriesId(idStr);
    HistoricalTimeSeriesInfoDocument info = data().getHistoricalTimeSeriesMaster().get(UniqueId.parse(idStr));
    data().setInfo(info);
    ManageableHistoricalTimeSeries series = data().getHistoricalTimeSeriesMaster().getTimeSeries(
        info.getInfo().getTimeSeriesObjectId(), VersionCorrection.LATEST);
    data().setTimeSeries(series);
    return new WebHistoricalTimeSeriesResource(this);
  }

  //-------------------------------------------------------------------------
  /**
   * Creates the output root data.
   * @return the output root data, not null
   */
  protected FlexiBean createRootData() {
    FlexiBean out = super.createRootData();
    HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    out.put("searchRequest", searchRequest);
    return out;
  }

  //-------------------------------------------------------------------------
  /**
   * Builds a URI for historical time-series.
   * @param data  the data, not null
   * @return the URI, not null
   */
  public static URI uri(WebHistoricalTimeSeriesData data) {
    return data.getUriInfo().getBaseUriBuilder().path(WebAllHistoricalTimeSeriesResource.class).build();
  }

  /**
   * Builds a URI for collection of historical time-series.
   * @param data  the data, not null
   * @param identifiers  the identifiers to search for, may be null
   * @return the URI, not null
   */
  public static URI uri(WebHistoricalTimeSeriesData data, Collection<ExternalId> identifiers) {
    UriBuilder builder = data.getUriInfo().getBaseUriBuilder().path(WebAllHistoricalTimeSeriesResource.class);
    if (identifiers != null) {
      Iterator<ExternalId> it = identifiers.iterator();
      for (int i = 0; it.hasNext(); i++) {
        ExternalId id = it.next();
        builder.queryParam("idscheme." + i, id.getScheme().getName());
        builder.queryParam("idvalue." + i, id.getValue());
      }
    }
    return builder.build();
  }

}
