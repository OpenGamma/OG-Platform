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
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_EXCLUSIVE_END;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_IDENTIFIER_SET;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_INCLUSIVE_START;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_MULTIPLE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_START;

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
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.id.Identifier;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
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

  //-------------------------------------------------------------------------
  /**
   * Gets the serialization context derived from the main Fudge context.
   * @return the context, not null
   */
  protected FudgeSerializationContext getFudgeSerializationContext() {
    return new FudgeSerializationContext(getFudgeContext());
  }

  private IdentifierBundle identifiersToBundle(final List<String> identifiers) {
    IdentifierBundle bundle = IdentifierBundle.EMPTY;
    for (String identifier : identifiers) {
      bundle = bundle.withIdentifier(Identifier.parse(identifier));
    }
    return bundle;
  }

  private FudgeMsgEnvelope encodeMessage(final HistoricalTimeSeries result) {
    if (result == null) {
      throw new WebApplicationException(Response.Status.NOT_FOUND);
    }
    final FudgeSerializationContext context = getFudgeSerializationContext();
    final MutableFudgeMsg message = context.newMessage();
    context.addToMessageWithClassHeaders(message, HISTORICALTIMESERIESSOURCE_UNIQUEID, null, result.getUniqueId(), UniqueIdentifier.class);
    context.addToMessageWithClassHeaders(message, HISTORICALTIMESERIESSOURCE_TIMESERIES, null, result.getTimeSeries(), LocalDateDoubleTimeSeries.class);
    return new FudgeMsgEnvelope(message);
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("uid/{uid}")
  public FudgeMsgEnvelope getUid(@PathParam("uid") String uid) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(UniqueIdentifier.parse(uid)));
  }

  @GET
  @Path("uidByDate/{uid}/{start}/{includeStart}/{end}/{excludeEnd}")
  public FudgeMsgEnvelope getUidByDate(@PathParam("uid") String uid, 
      @PathParam("start") String start, 
      @PathParam("includeStart") String includeStart,
      @PathParam("end") String end,
      @PathParam("excludeEnd") String excludeEnd) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        UniqueIdentifier.parse(uid), 
        NULL_VALUE.equals(start) ? null : LocalDate.parse(start),
        Boolean.valueOf(includeStart),
        NULL_VALUE.equals(end) ? null : LocalDate.parse(end),
        Boolean.valueOf(excludeEnd)));
  }

  //-------------------------------------------------------------------------
  @GET
  @Path("all/{currentDate}/{dataSource}/{dataProvider}/{dataField}")
  public FudgeMsgEnvelope getAll(@PathParam("currentDate") String currentDate, 
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
  @Path("allByDate/{currentDate}/{dataSource}/{dataProvider}/{dataField}/{start}/{includeStart}/{end}/{excludeEnd}")
  public FudgeMsgEnvelope getAllByDate(@PathParam("currentDate") String currentDate,
      @PathParam("dataSource") String dataSource, 
      @PathParam("dataProvider") String dataProvider, 
      @PathParam("dataField") String dataField,
      @PathParam("start") String start, 
      @PathParam("includeStart") String includeStart,
      @PathParam("end") String end, 
      @PathParam("excludeEnd") String excludeEnd,
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
        Boolean.valueOf(excludeEnd));
    return encodeMessage(hts);
  }

  @GET
  @Path("resolved/{dataField}/{currentDate}/{resolutionKey}")
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
  @Path("resolvedByDate/{dataField}/{currentDate}/{resolutionKey}/{start}/{includeStart}/{end}/{excludeEnd}")
  public FudgeMsgEnvelope getResolvedByDate(@PathParam("currentDate") String currentDate,
      @PathParam("dataField") String dataField, 
      @PathParam("resolutionKey") String resolutionKey, 
      @PathParam("start") String start, 
      @PathParam("includeStart") String includeStart,
      @PathParam("end") String end, 
      @PathParam("excludeEnd") String excludeEnd,
      @QueryParam("id") List<String> identifiers) {
    return encodeMessage(getHistoricalTimeSeriesSource().getHistoricalTimeSeries(
        dataField,
        identifiersToBundle(identifiers),
        NULL_VALUE.equals(currentDate) ? null : LocalDate.parse(currentDate),
        NULL_VALUE.equals(resolutionKey) ? null : resolutionKey,
        NULL_VALUE.equals(start) ? null : LocalDate.parse(start),
        Boolean.valueOf(includeStart),
        NULL_VALUE.equals(end) ? null : LocalDate.parse(end),
        Boolean.valueOf(excludeEnd)));
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
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(getFudgeContext());
    Set<IdentifierBundle> identifierSet = deserializationContext.fudgeMsgToObject(Set.class, msg.getMessage(REQUEST_IDENTIFIER_SET));
    String dataSource = msg.getString(REQUEST_DATA_SOURCE);
    String dataProvider = msg.getString(REQUEST_DATA_PROVIDER);
    String dataField = msg.getString(REQUEST_DATA_FIELD);
    LocalDate start = deserializationContext.fieldValueToObject(LocalDate.class, msg.getByName(REQUEST_START));
    boolean inclusiveStart = msg.getBoolean(REQUEST_INCLUSIVE_START);
    LocalDate end = deserializationContext.fieldValueToObject(LocalDate.class, msg.getByName(REQUEST_END));
    boolean exclusiveEnd = msg.getBoolean(REQUEST_EXCLUSIVE_END);
    
    Map<IdentifierBundle, HistoricalTimeSeries> result = _source.getHistoricalTimeSeries(
        identifierSet, dataSource, dataProvider, dataField, start, inclusiveStart, end, exclusiveEnd);
    FudgeSerializationContext context = getFudgeSerializationContext();
    MutableFudgeMsg message = context.newMessage();
    context.addToMessageWithClassHeaders(message, HISTORICALTIMESERIESSOURCE_TIMESERIES, null, result, Map.class);
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
