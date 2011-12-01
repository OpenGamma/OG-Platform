/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaltimeseries.rest;

import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.HISTORICALTIMESERIESSOURCE_TIMESERIES;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.HISTORICALTIMESERIESSOURCE_UNIQUEID;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.NULL_VALUE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_DATA_FIELD;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_DATA_PROVIDER;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_DATA_SOURCE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_END;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_IDENTIFIER_SET;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_INCLUDE_END;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_INCLUDE_START;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_MULTIPLE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_START;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_ALL;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_ALL_BY_DATE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_ALL_BY_DATE_LIMIT;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_UID;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_UID_BY_DATE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_UID_BY_DATE_LIMIT;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_RESOLVED;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_RESOLVED_BY_DATE;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * REST resource wrapper for a {@link HistoricalTimeSeriesSource}.
 */
public class HistoricalTimeSeriesSourceResource {

  /**
   * The Fudge context.
   */
  private final FudgeContext _fudgeContext;
  /**
   * The underlying data source.
   */
  private final HistoricalTimeSeriesSource _source;

  /**
   * Creates a resource to expose a source over REST.
   * @param fudgeContext  the context, not null
   * @param source  the historical time-series source, not null
   */
  public HistoricalTimeSeriesSourceResource(final FudgeContext fudgeContext, final HistoricalTimeSeriesSource source) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(source, "source");
    _fudgeContext = fudgeContext;
    _source = source;
  }

  // -------------------------------------------------------------------------
  /**
   * Gets the Fudge context.
   * 
   * @return the context, not null
   */
  protected FudgeContext getFudgeContext() {
    return _fudgeContext;
  }

  /**
   * Gets the source.
   * 
   * @return the source, not null
   */
  protected HistoricalTimeSeriesSource getHistoricalTimeSeriesSource() {
    return _source;
  }

  private ExternalIdBundle identifiersToBundle(final List<String> identifiers) {
    ExternalIdBundle bundle = ExternalIdBundle.EMPTY;
    for (String identifier : identifiers) {
      bundle = bundle.withExternalId(ExternalId.parse(identifier));
    }
    return bundle;
  }

  private FudgeMsgEnvelope encodeMessage(final HistoricalTimeSeries result) {
    if (result == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    final MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(message, HISTORICALTIMESERIESSOURCE_UNIQUEID, null, result.getUniqueId(), UniqueId.class);
    serializer.addToMessageWithClassHeaders(message, HISTORICALTIMESERIESSOURCE_TIMESERIES, null, result.getTimeSeries(), LocalDateDoubleTimeSeries.class);
    return new FudgeMsgEnvelope(message);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path(REQUEST_UID + "/{uid}")
  public FudgeMsgEnvelope getUid(@PathParam("uid") String uid) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(UniqueId.parse(uid)));
  }

  @GET
  @Path(REQUEST_UID_BY_DATE + "/{uid}/{start}/{includeStart}/{end}/{includeEnd}")
  public FudgeMsgEnvelope getUidByDate(@PathParam("uid") String uid, 
      @PathParam("start") String start, 
      @PathParam("includeStart") String includeStart,
      @PathParam("end") String end,
      @PathParam("includeEnd") String includeEnd) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        UniqueId.parse(uid), 
        NULL_VALUE.equals(start) ? null : LocalDate.parse(start),
        Boolean.valueOf(includeStart),
        NULL_VALUE.equals(end) ? null : LocalDate.parse(end),
        Boolean.valueOf(includeEnd)));
  }

  @GET
  @Path(REQUEST_UID_BY_DATE_LIMIT + "/{uid}/{start}/{includeStart}/{end}/{includeEnd}/{maxPoints}")
  public FudgeMsgEnvelope getUidByDateLimit(@PathParam("uid") String uid, 
      @PathParam("start") String start, 
      @PathParam("includeStart") String includeStart,
      @PathParam("end") String end,
      @PathParam("includeEnd") String includeEnd,
      @PathParam("maxPoints") String maxPoints) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        UniqueId.parse(uid), 
        NULL_VALUE.equals(start) ? null : LocalDate.parse(start),
        Boolean.valueOf(includeStart),
        NULL_VALUE.equals(end) ? null : LocalDate.parse(end),
        Boolean.valueOf(includeEnd),
        Integer.parseInt(maxPoints)));
  }

  //-------------------------------------------------------------------------
  @GET
  @Path(REQUEST_ALL + "/{currentDate}/{dataSource}/{dataProvider}/{dataField}")
  public FudgeMsgEnvelope getAllWithCurrentDate(@PathParam("currentDate") String currentDate, 
      @PathParam("dataSource") String dataSource, 
      @PathParam("dataProvider") String dataProvider, 
      @PathParam("dataField") String dataField,
      @QueryParam("id") List<String> identifiers) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        identifiersToBundle(identifiers),
        NULL_VALUE.equals(currentDate) ? null : LocalDate.parse(currentDate),
        dataSource,
        NULL_VALUE.equals(dataProvider) ? null : dataProvider,
        dataField));
  }
  
  @GET
  @Path(REQUEST_ALL + "/{dataSource}/{dataProvider}/{dataField}")
  public FudgeMsgEnvelope getAll(@PathParam("dataSource") String dataSource, 
      @PathParam("dataProvider") String dataProvider, 
      @PathParam("dataField") String dataField,
      @QueryParam("id") List<String> identifiers) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        identifiersToBundle(identifiers),
        dataSource,
        NULL_VALUE.equals(dataProvider) ? null : dataProvider,
        dataField));
  }

  @GET
  @Path(REQUEST_ALL_BY_DATE + "/{currentDate}/{dataSource}/{dataProvider}/{dataField}/{start}/{includeStart}/{end}/{includeEnd}")
  public FudgeMsgEnvelope getAllByDate(@PathParam("currentDate") String currentDate,
      @PathParam("dataSource") String dataSource, 
      @PathParam("dataProvider") String dataProvider, 
      @PathParam("dataField") String dataField,
      @PathParam("start") String start, 
      @PathParam("includeStart") String includeStart,
      @PathParam("end") String end, 
      @PathParam("includeEnd") String includeEnd,
      @QueryParam("id") List<String> identifiers) {
    
    HistoricalTimeSeries hts = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        identifiersToBundle(identifiers),
        NULL_VALUE.equals(currentDate) ? null : LocalDate.parse(currentDate),
        dataSource, 
        NULL_VALUE.equals(dataProvider) ? null : dataProvider,
        dataField,
        NULL_VALUE.equals(start) ? null : LocalDate.parse(start),
        Boolean.valueOf(includeStart),
        NULL_VALUE.equals(end) ? null : LocalDate.parse(end),
        Boolean.valueOf(includeEnd));
    return encodeMessage(hts);
  }

  @GET
  @Path(REQUEST_ALL_BY_DATE_LIMIT + "/{currentDate}/{dataSource}/{dataProvider}/{dataField}/{start}/{includeStart}/{end}/{includeEnd}/{maxPoints}")
  public FudgeMsgEnvelope getAllByDateLimit(@PathParam("currentDate") String currentDate,
      @PathParam("dataSource") String dataSource, 
      @PathParam("dataProvider") String dataProvider, 
      @PathParam("dataField") String dataField,
      @PathParam("start") String start, 
      @PathParam("includeStart") String includeStart,
      @PathParam("end") String end, 
      @PathParam("includeEnd") String includeEnd,
      @PathParam("maxPoints") String maxPoints,
      @QueryParam("id") List<String> identifiers) {
    
    HistoricalTimeSeries hts = getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        identifiersToBundle(identifiers),
        NULL_VALUE.equals(currentDate) ? null : LocalDate.parse(currentDate),
        dataSource, 
        NULL_VALUE.equals(dataProvider) ? null : dataProvider,
        dataField,
        NULL_VALUE.equals(start) ? null : LocalDate.parse(start),
        Boolean.valueOf(includeStart),
        NULL_VALUE.equals(end) ? null : LocalDate.parse(end),
        Boolean.valueOf(includeEnd),
        Integer.parseInt(maxPoints));
    return encodeMessage(hts);
  }

  @GET
  @Path(REQUEST_RESOLVED + "/{dataField}/{currentDate}/{resolutionKey}")
  public FudgeMsgEnvelope getResolved(@PathParam("currentDate") String currentDate, 
      @PathParam("dataField") String dataField, 
      @PathParam("resolutionKey") String resolutionKey, 
      @QueryParam("id") List<String> identifiers) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        dataField,
        identifiersToBundle(identifiers),
        NULL_VALUE.equals(currentDate) ? null : LocalDate.parse(currentDate),
        NULL_VALUE.equals(resolutionKey) ? null : resolutionKey));
  }

  @GET
  @Path(REQUEST_RESOLVED_BY_DATE + "/{dataField}/{currentDate}/{resolutionKey}/{start}/{includeStart}/{end}/{includeEnd}")
  public FudgeMsgEnvelope getResolvedByDate(@PathParam("currentDate") String currentDate,
      @PathParam("dataField") String dataField, 
      @PathParam("resolutionKey") String resolutionKey, 
      @PathParam("start") String start, 
      @PathParam("includeStart") String includeStart,
      @PathParam("end") String end, 
      @PathParam("includeEnd") String includeEnd,
      @QueryParam("id") List<String> identifiers) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        dataField,
        identifiersToBundle(identifiers),
        NULL_VALUE.equals(currentDate) ? null : LocalDate.parse(currentDate),
        NULL_VALUE.equals(resolutionKey) ? null : resolutionKey,
        NULL_VALUE.equals(start) ? null : LocalDate.parse(start),
        Boolean.valueOf(includeStart),
        NULL_VALUE.equals(end) ? null : LocalDate.parse(end),
        Boolean.valueOf(includeEnd)));
  }

  //-------------------------------------------------------------------------
  @POST
  @Path(REQUEST_MULTIPLE)
  @SuppressWarnings("unchecked")
  public FudgeMsgEnvelope getMultiple(FudgeMsgEnvelope request) {
    // REVIEW jonathan 2011-02-03 -- this kind of query which can only realistically be represented by an entity
    // delivered by a POST is not ideally placed here, but other Source interfaces are currently suffering from the
    // same problem and the solution is not clear.
    
    FudgeMsg msg = request.getMessage();
    FudgeDeserializer deserializationContext = new FudgeDeserializer(getFudgeContext());
    Set<ExternalIdBundle> identifierSet = deserializationContext.fudgeMsgToObject(Set.class, msg.getMessage(REQUEST_IDENTIFIER_SET));
    String dataSource = msg.getString(REQUEST_DATA_SOURCE);
    String dataProvider = msg.getString(REQUEST_DATA_PROVIDER);
    String dataField = msg.getString(REQUEST_DATA_FIELD);
    LocalDate start = deserializationContext.fieldValueToObject(LocalDate.class, msg.getByName(REQUEST_START));
    boolean inclusiveStart = msg.getBoolean(REQUEST_INCLUDE_START);
    LocalDate end = deserializationContext.fieldValueToObject(LocalDate.class, msg.getByName(REQUEST_END));
    boolean includeEnd = msg.getBoolean(REQUEST_INCLUDE_END);
    
    Map<ExternalIdBundle, HistoricalTimeSeries> result = _source.getHistoricalTimeSeries(
        identifierSet, dataSource, dataProvider, dataField, start, inclusiveStart, end, includeEnd);
    FudgeSerializer serializer = new FudgeSerializer(getFudgeContext());
    MutableFudgeMsg message = serializer.newMessage();
    serializer.addToMessageWithClassHeaders(message, HISTORICALTIMESERIESSOURCE_TIMESERIES, null, result, Map.class);
    return new FudgeMsgEnvelope(message); 
  }

  /**
   * For debugging purposes only.
   * 
   * @return some debug information about the state of this resource object; e.g. which underlying objects is it connected to.
   */
  @GET
  @Path("debugInfo")
  public FudgeMsgEnvelope getDebugInfo() {
    final MutableFudgeMsg message = getFudgeContext().newMessage();
    message.add("fudgeContext", getFudgeContext().toString());
    message.add("historicalTimeSeriesSource", getHistoricalTimeSeriesSource().toString());
    return new FudgeMsgEnvelope(message);
  }

}
