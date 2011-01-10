/**
 * Copyright (C) 2009 - Present by OpenGamma Inc.
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaldata.rest;

import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.HISTORICALDATASOURCE_TIMESERIES;
import static com.opengamma.financial.historicaldata.rest.HistoricalDataSourceServiceNames.HISTORICALDATASOURCE_UNIQUEID;
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

import com.opengamma.core.historicaldata.HistoricalDataSource;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.id.UniqueIdentifier;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.ArrayLocalDateDoubleTimeSeries;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.Pair;

/**
 * A HistoricalDataSource implementation that connects to a remote one with REST calls.
 */
public class RemoteHistoricalDataSource implements HistoricalDataSource {

  private static final LocalDateDoubleTimeSeries EMPTY_TIMESERIES = new ArrayLocalDateDoubleTimeSeries();

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
      return Pair.of(null, EMPTY_TIMESERIES);
    }
    final FudgeField uniqueIdField = message.getByName(HISTORICALDATASOURCE_UNIQUEID);
    if (uniqueIdField == null) {
      throw new IllegalArgumentException(HISTORICALDATASOURCE_UNIQUEID + " not present in message");
    }
    final FudgeField timeSeriesField = message.getByName(HISTORICALDATASOURCE_TIMESERIES);
    if (timeSeriesField == null) {
      throw new IllegalArgumentException(HISTORICALDATASOURCE_TIMESERIES + " not present in message");
    }
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getRestClient().getFudgeContext());
    return Pair.of(context.fieldValueToObject(UniqueIdentifier.class, uniqueIdField), context.fieldValueToObject(LocalDateDoubleTimeSeries.class, timeSeriesField));
  }

  private LocalDateDoubleTimeSeries decodeTimeSeriesMessage(final FudgeFieldContainer message) {
    if (message == null) {
      return EMPTY_TIMESERIES;
    }
    final FudgeField timeSeriesField = message.getByName(HISTORICALDATASOURCE_TIMESERIES);
    if (timeSeriesField == null) {
      throw new IllegalArgumentException(HISTORICALDATASOURCE_TIMESERIES + " not present in message");
    }
    final FudgeDeserializationContext context = new FudgeDeserializationContext(getRestClient().getFudgeContext());
    return context.fieldValueToObject(LocalDateDoubleTimeSeries.class, timeSeriesField);
  }
  
  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "dataField");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_ALL)
      .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
      .resolveBase(dataSource).resolveBase((dataProvider != null) ? dataProvider : NULL_VALUE)
      .resolveBase(dataField).resolveQuery("id", identifiers.toStringList());
    return decodePairMessage(getRestClient().getMsg(target));
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField) {
    return getHistoricalData(identifiers, (LocalDate) null, dataSource, dataProvider, dataField);
  }
  
  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String configDocName) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_DEFAULT)
      .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
      .resolveBase((configDocName != null) ? configDocName : NULL_VALUE)
      .resolveQuery("id", identifiers.toStringList());
    return decodePairMessage(getRestClient().getMsg(target));
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String configDocName) {
    return getHistoricalData(identifiers, (LocalDate) null, configDocName);
  }
  
  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String configDocName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    ArgumentChecker.notNull(identifiers, "identifiers");
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(end, "end");
    
    final RestTarget target = getTargetBase().resolveBase(REQUEST_DEFAULT_BY_DATE)
      .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
      .resolveBase((configDocName != null) ? configDocName : NULL_VALUE)
      .resolveBase(start.toString())
      .resolveBase(String.valueOf(inclusiveStart))
      .resolveBase(end.toString())
      .resolveBase(String.valueOf(exclusiveEnd))
      .resolveQuery("id", identifiers.toStringList());
    return decodePairMessage(getRestClient().getMsg(target));
  }
  
  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String configDocName, 
      LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    return getHistoricalData(identifiers, (LocalDate) null, configDocName, start, inclusiveStart, end, exclusiveEnd);
  }
  
  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid) {
    ArgumentChecker.notNull(uid, "uid");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID).resolve(uid.toString());
    return decodeTimeSeriesMessage(getRestClient().getMsg(target));
  }

  @Override
  public LocalDateDoubleTimeSeries getHistoricalData(UniqueIdentifier uid, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    ArgumentChecker.notNull(uid, "uid");
    ArgumentChecker.notNull(start, "start");
    ArgumentChecker.notNull(end, "end");
    
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID_BY_DATE).resolveBase(uid.toString())
      .resolveBase(start.toString())
      .resolveBase(String.valueOf(inclusiveStart))
      .resolveBase(end.toString())
      .resolveBase(String.valueOf(exclusiveEnd));
    return decodeTimeSeriesMessage(getRestClient().getMsg(target));
  }
  
  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, LocalDate currentDate, String dataSource, 
      String dataProvider, String dataField, LocalDate start, boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    ArgumentChecker.notNull(identifiers, "identifiers");
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
      .resolveQuery("id", identifiers.toStringList());
    return decodePairMessage(getRestClient().getMsg(target));
  }

  @Override
  public Pair<UniqueIdentifier, LocalDateDoubleTimeSeries> getHistoricalData(IdentifierBundle identifiers, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean inclusiveStart, LocalDate end, boolean exclusiveEnd) {
    return getHistoricalData(identifiers, (LocalDate) null, dataSource, dataProvider, dataField, start, inclusiveStart, end, exclusiveEnd);
  }

}
