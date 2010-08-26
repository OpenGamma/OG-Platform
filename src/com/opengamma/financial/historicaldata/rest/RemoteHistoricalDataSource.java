/**
 * Copyright (C) 2009 - 2009 by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaldata.rest;

import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.HISTORICALDATASOURCE_TIMESERIES;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.HISTORICALDATASOURCE_UNIQUEIDENTIFIER;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.NULL_VALUE;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.REQUEST_ALL;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.REQUEST_ALL_BY_DATE;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.REQUEST_DEFAULT;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.REQUEST_DEFAULT_BY_DATE;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.REQUEST_UID;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.REQUEST_UID_BY_DATE;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeFieldContainer;
import org.fudgemsg.mapping.FudgeDeserializationContext;

import com.opengamma.engine.historicaldata.HistoricalDataSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * A HistoricalDataSource implementation that connects to a remote one with REST calls.
 */
public class RemoteHistoricalDataSource implements HistoricalDataSource {

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
   * @param fudgeContext  the Fudge context, not null
   * @param baseTarget  the base target URI to call, not null
   */
  public RemoteHistoricalDataSource(final FudgeContext fudgeContext, final RestTarget baseTarget) {
    ArgumentChecker.notNull(fudgeContext, "fudgeContext");
    ArgumentChecker.notNull(baseTarget, "baseTarget");
    _restClient = RestClient.getInstance(fudgeContext, null);
    _targetBase = baseTarget;
  }

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

  private Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> decodePairMessage(final FudgeFieldContainer message) {
    if (message == null) {
      return null;
    }
    final FudgeField uniqueIdentifierField = message.getByName(HISTORICALDATASOURCE_UNIQUEIDENTIFIER);
    if (uniqueIdentifierField == null) {
      throw new IllegalArgumentException(HISTORICALDATASOURCE_UNIQUEIDENTIFIER + " not present in message");
    }
    final FudgeField timeSeriesField = message.getByName(HISTORICALDATASOURCE_TIMESERIES);
    if (timeSeriesField == null) {
      throw new IllegalArgumentException(HISTORICALDATASOURCE_TIMESERIES + " not present in message");
    }
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getRestClient().getFudgeContext());
    return Pair.of(context.fieldValueToObject(UniqueIdentifier.class, uniqueIdentifierField), context.fieldValueToObject(LocalDateDoubleTimeSeries.class, timeSeriesField));
  }

  private LocalDateDoubleTimeSeries decodeTimeSeriesMessage(final FudgeFieldContainer message) {
    if (message == null) {
      return null;
    }
    final FudgeField timeSeriesField = message.getByName(HISTORICALDATASOURCE_TIMESERIES);
    if (timeSeriesField == null) {
      throw new IllegalArgumentException(HISTORICALDATASOURCE_TIMESERIES + " not present in message");
    }
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getRestClient().getFudgeContext());
    return context.fieldValueToObject(LocalDateDoubleTimeSeries.class, timeSeriesField);
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "dataField");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_ALL).resolveBase(dataSource).resolveBase((dataProvider != null) ? dataProvider : NULL_VALUE).resolveBase(dataField).resolveQuery(
        "id", identifiers.toStringList());
    return decodePairMessage(getRestClient().getMsg(target));
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField, LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "dataField");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_ALL_BY_DATE).resolveBase(dataSource).resolveBase((dataProvider != null) ? dataProvider : NULL_VALUE).resolveBase(dataField)
        .resolveBase((start != null) ? start.toString() : NULL_VALUE).resolveBase((end != null) ? end.toString() : NULL_VALUE).resolveQuery("id", identifiers.toStringList());
    return decodePairMessage(getRestClient().getMsg(target));
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_DEFAULT).resolveQuery("id", identifiers.toStringList());
    return decodePairMessage(getRestClient().getMsg(target));
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_DEFAULT_BY_DATE).resolveBase((start != null) ? start.toString() : NULL_VALUE)
        .resolveBase((end != null) ? end.toString() : NULL_VALUE).resolveQuery("id", identifiers.toStringList());
    return decodePairMessage(getRestClient().getMsg(target));
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID).resolve(uid.toString());
    return decodeTimeSeriesMessage(getRestClient().getMsg(target));
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, LocalDate end) {
    ArgumentChecker.notNull(uid, "uid");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID_BY_DATE).resolveBase(uid.toString()).resolveBase((start != null) ? start.toString() : NULL_VALUE).resolve(
        (end != null) ? end.toString() : "null");
    return decodeTimeSeriesMessage(getRestClient().getMsg(target));
  }

}
