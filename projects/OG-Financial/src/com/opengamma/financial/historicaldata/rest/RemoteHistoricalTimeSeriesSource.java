/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaldata.rest;

import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.HISTORICALTIMESERIESSOURCE_TIMESERIES;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.HISTORICALTIMESERIESSOURCE_UNIQUEID;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.NULL_VALUE;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_ALL;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_ALL_BY_DATE;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_DATA_FIELD;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_DATA_PROVIDER;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_DATA_SOURCE;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_RESOLVED;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_RESOLVED_BY_DATE;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_END;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_EXCLUSIVE_END;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_IDENTIFIER_SET;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_INCLUSIVE_START;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_MULTIPLE;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_START;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_UID;
import static com.opengamma.financial.historicaldata.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_UID_BY_DATE;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializationContext;
import org.fudgemsg.mapping.FudgeSerializationContext;

import com.opengamma.core.historicaldata.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaldata.HistoricalTimeSeries;
import com.opengamma.core.historicaldata.impl.HistoricalTimeSeriesImpl;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A {@code HistoricalTimeSeriesSource} implementation that connects to a remote one with REST calls.
 */
public class RemoteHistoricalTimeSeriesSource implements HistoricalTimeSeriesSource {

  /**
   * The RESTful client instance.
   */
  private final RestClient _restClient;
  /**
   * The base URI of the RESTful server.
   */
  private final RestTarget _targetBase;

  /**
   * Creates an instance.
   * 
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the base target URI to call, not null
   */
  public RemoteHistoricalTimeSeriesSource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the RESTful client.
   * @return the client, not null
   */
  protected RestClient getRestClient() {
    return _restClient;
  }

  /**
   * Gets the base target URI.
   * @return the base target URI, not null
   */
  protected RestTarget getTargetBase() {
    return _targetBase;
  }

  private HistoricalTimeSeries decodeMessage(final FudgeMsg message) {
    if (message == null) {
      return null;
    }
    final FudgeField uniqueIdField = message.getByName(HISTORICALTIMESERIESSOURCE_UNIQUEID);
    if (uniqueIdField == null) {
      throw new IllegalArgumentException(HISTORICALTIMESERIESSOURCE_UNIQUEID + " not present in message");
    }
    final FudgeField timeSeriesField = message.getByName(HISTORICALTIMESERIESSOURCE_TIMESERIES);
    if (timeSeriesField == null) {
      throw new IllegalArgumentException(HISTORICALTIMESERIESSOURCE_TIMESERIES + " not present in message");
    }
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getRestClient().getFudgeContext());
    UniqueIdentifier uniqueId = context.fieldValueToObject(UniqueIdentifier.class, uniqueIdField);
    LocalDateDoubleTimeSeries ts = context.fieldValueToObject(LocalDateDoubleTimeSeries.class, timeSeriesField);
    return new HistoricalTimeSeriesImpl(uniqueId, ts);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueIdentifier uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID).resolve(uniqueId.toString());
    return decodeMessage(getRestClient().getMsg(target));
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueIdentifier uniqueId, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(end, "end");
    
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID_BY_DATE).resolveBase(uniqueId.toString())
      .resolveBase(start.toString())
      .resolveBase(String.valueOf(inclusiveStart))
      .resolveBase(end.toString())
      .resolveBase(String.valueOf(exclusiveEnd));
    return decodeMessage(getRestClient().getMsg(target));
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    return getHistoricalTimeSeries(identifierBundle, (LocalDate) null, dataSource, dataProvider, dataField);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifierBundle, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "dataField");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_ALL)
      .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
      .resolveBase(dataSource).resolveBase((dataProvider != null) ? dataProvider : NULL_VALUE)
      .resolveBase(dataField).resolveQuery("id", identifierBundle.toStringList());
    return decodeMessage(getRestClient().getMsg(target));
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    return getHistoricalTimeSeries(identifierBundle, (LocalDate) null, dataSource, dataProvider, dataField, start, inclusiveStart, end, exclusiveEnd);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      IdentifierBundle identifierBundle, LocalDate currentDate, String dataSource, 
      String dataProvider, String dataField, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(end, "end");
    
    final RestTarget target = getTargetBase().resolveBase(REQUEST_ALL_BY_DATE)
      .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
      .resolveBase(dataSource).resolveBase((dataProvider != null) ? dataProvider : NULL_VALUE).resolveBase(dataField)
      .resolveBase(start.toString())
      .resolveBase(String.valueOf(inclusiveStart))
      .resolveBase(end.toString())
      .resolveBase(String.valueOf(exclusiveEnd))
      .resolveQuery("id", identifierBundle.toStringList());
    return decodeMessage(getRestClient().getMsg(target));
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, String resolutionKey) {
    return getHistoricalTimeSeries(dataField, identifierBundle, (LocalDate) null, resolutionKey);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, LocalDate currentDate, String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_RESOLVED)
      .resolveBase(dataField)
      .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
      .resolveBase((resolutionKey != null) ? resolutionKey : NULL_VALUE)
      .resolveQuery("id", identifierBundle.toStringList());
    return decodeMessage(getRestClient().getMsg(target));
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, String resolutionKey, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    return getHistoricalTimeSeries(dataField, identifierBundle, (LocalDate) null, resolutionKey, start, inclusiveStart, end, exclusiveEnd);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, IdentifierBundle identifierBundle, LocalDate currentDate, String resolutionKey, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(end, "end");
    
    final RestTarget target = getTargetBase().resolveBase(REQUEST_RESOLVED_BY_DATE)
      .resolveBase(dataField)
      .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
      .resolveBase((resolutionKey != null) ? resolutionKey : NULL_VALUE)
      .resolveBase(start.toString())
      .resolveBase(String.valueOf(inclusiveStart))
      .resolveBase(end.toString())
      .resolveBase(String.valueOf(exclusiveEnd))
      .resolveQuery("id", identifierBundle.toStringList());
    return decodeMessage(getRestClient().getMsg(target));
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Map<IdentifierBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<IdentifierBundle> identifierSet, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    final RestTarget target = getTargetBase().resolveBase(REQUEST_MULTIPLE);
    FudgeSerializationContext serializationContext = new FudgeSerializationContext(getRestClient().getFudgeContext());
    MutableFudgeMsg msg = serializationContext.newMessage();
    serializationContext.addToMessage(msg, REQUEST_IDENTIFIER_SET, null, identifierSet);
    serializationContext.addToMessage(msg, REQUEST_DATA_SOURCE, null, dataSource);
    serializationContext.addToMessage(msg, REQUEST_DATA_PROVIDER, null, dataProvider);
    serializationContext.addToMessage(msg, REQUEST_DATA_FIELD, null, dataField);
    serializationContext.addToMessage(msg, REQUEST_START, null, start);
    serializationContext.addToMessage(msg, REQUEST_INCLUSIVE_START, null, inclusiveStart);
    serializationContext.addToMessage(msg, REQUEST_END, null, end);
    serializationContext.addToMessage(msg, REQUEST_EXCLUSIVE_END, null, exclusiveEnd);
    
    FudgeMsgEnvelope result = getRestClient().post(target, msg);
    FudgeDeserializationContext deserializationContext = new FudgeDeserializationContext(getRestClient().getFudgeContext());
    return deserializationContext.fudgeMsgToObject(Map.class, result.getMessage().getMessage(HISTORICALTIMESERIESSOURCE_TIMESERIES));
  }

}
