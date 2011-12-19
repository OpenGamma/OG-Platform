/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaltimeseries.rest;

import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.HISTORICALTIMESERIESSOURCE_TIMESERIES;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.HISTORICALTIMESERIESSOURCE_UNIQUEID;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.NULL_VALUE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_ALL;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_ALL_BY_DATE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_ALL_BY_DATE_LIMIT;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_DATA_FIELD;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_DATA_PROVIDER;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_DATA_SOURCE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_END;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_IDENTIFIER_SET;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_INCLUDE_END;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_INCLUDE_START;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_MULTIPLE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_RESOLVED;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_RESOLVED_BY_DATE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_RESOLVED_BY_DATE_LIMIT;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_START;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_UID;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_UID_BY_DATE;
import static com.opengamma.financial.historicaltimeseries.rest.HistoricalTimeSeriesSourceServiceNames.REQUEST_UID_BY_DATE_LIMIT;

import java.util.Map;
import java.util.Set;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeField;
import org.fudgemsg.FudgeMsg;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.MutableFudgeMsg;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSource;
import com.opengamma.core.historicaltimeseries.impl.SimpleHistoricalTimeSeries;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.UniqueId;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestRuntimeException;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.tuple.ObjectsPair;
import com.opengamma.util.tuple.Pair;

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
    final FudgeDeserializer deserializer = new FudgeDeserializer(getRestClient().getFudgeContext());
    UniqueId uniqueId = deserializer.fieldValueToObject(UniqueId.class, uniqueIdField);
    LocalDateDoubleTimeSeries ts = deserializer.fieldValueToObject(LocalDateDoubleTimeSeries.class, timeSeriesField);
    return new SimpleHistoricalTimeSeries(uniqueId, ts);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID).resolve(uniqueId.toString());
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID_BY_DATE).resolveBase(uniqueId.toString())
        .resolveBase(start == null ? NULL_VALUE : start.toString())
        .resolveBase(String.valueOf(includeStart))
        .resolveBase(end == null ? NULL_VALUE : end.toString())
        .resolveBase(String.valueOf(includeEnd));
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID_BY_DATE_LIMIT).resolveBase(uniqueId.toString())
        .resolveBase(start == null ? NULL_VALUE : start.toString())
        .resolveBase(String.valueOf(includeStart))
        .resolveBase(end == null ? NULL_VALUE : end.toString())
        .resolveBase(String.valueOf(includeEnd))
        .resolveBase(String.valueOf(maxPoints));
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      UniqueId uniqueId, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    HistoricalTimeSeries hts;
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID_BY_DATE).resolveBase(uniqueId.toString())
        .resolveBase(start == null ? NULL_VALUE : start.toString())
        .resolveBase(String.valueOf(includeStart))
        .resolveBase(end == null ? NULL_VALUE : end.toString())
        .resolveBase(String.valueOf(includeEnd))
        .resolveBase("-1");
    try {
      hts = decodeMessage(getRestClient().getMsg(target));
      return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    HistoricalTimeSeries hts;
    final RestTarget target = getTargetBase().resolveBase(REQUEST_UID_BY_DATE).resolveBase(uniqueId.toString())
        .resolveBase(NULL_VALUE)
        .resolveBase(String.valueOf(true))
        .resolveBase(NULL_VALUE)
        .resolveBase(String.valueOf(true))
        .resolveBase("-1");
    try {
      hts = decodeMessage(getRestClient().getMsg(target));
      return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "dataField");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_ALL)
      .resolveBase(dataSource).resolveBase((dataProvider != null) ? dataProvider : NULL_VALUE)
      .resolveBase(dataField).resolveQuery("id", identifierBundle.toStringList());
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "dataField");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_ALL)
        .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
        .resolveBase(dataSource).resolveBase((dataProvider != null) ? dataProvider : NULL_VALUE)
        .resolveBase(dataField).resolveQuery("id", identifierBundle.toStringList());
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start,
      boolean includeStart, LocalDate end, boolean includeEnd) {
    return getHistoricalTimeSeries(identifierBundle, (LocalDate) null, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, LocalDate currentDate, String dataSource,
      String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "dataField");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_ALL_BY_DATE)
        .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
        .resolveBase(dataSource).resolveBase((dataProvider != null) ? dataProvider : NULL_VALUE).resolveBase(dataField)
        .resolveBase(start == null ? NULL_VALUE : start.toString())
        .resolveBase(String.valueOf(includeStart))
        .resolveBase(end == null ? NULL_VALUE : end.toString())
        .resolveBase(String.valueOf(includeEnd))
        .resolveQuery("id", identifierBundle.toStringList());
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, LocalDate currentDate, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    ArgumentChecker.notNull(identifierBundle, "identifierBundle");
    ArgumentChecker.notNull(dataSource, "dataSource");
    ArgumentChecker.notNull(dataField, "dataField");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_ALL_BY_DATE_LIMIT)
        .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
        .resolveBase(dataSource).resolveBase((dataProvider != null) ? dataProvider : NULL_VALUE).resolveBase(dataField)
        .resolveBase(start == null ? NULL_VALUE : start.toString())
        .resolveBase(String.valueOf(includeStart))
        .resolveBase(end == null ? NULL_VALUE : end.toString())
        .resolveBase(String.valueOf(includeEnd))
        .resolveBase(String.valueOf(maxPoints))
        .resolveQuery("id", identifierBundle.toStringList());
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }

  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return getHistoricalTimeSeries(identifierBundle, (LocalDate) null, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, maxPoints);
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifierBundle, LocalDate currentDate, String dataSource, String dataProvider, String dataField) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(identifierBundle, currentDate, dataSource, dataProvider, dataField, null, true, null, true, -1);
    return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      ExternalIdBundle identifierBundle, LocalDate currentDate, String dataSource, String dataProvider, String dataField, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(identifierBundle, currentDate, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, -1);
    return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());    
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(
        identifierBundle, (LocalDate) null, dataSource, dataProvider, dataField, null, true, null, true, -1);
    return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());    
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(ExternalIdBundle identifierBundle, String dataSource, String dataProvider, String dataField, LocalDate start, boolean includeStart, LocalDate end,
      boolean includeEnd) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(identifierBundle, (LocalDate) null, dataSource, dataProvider, dataField, start, includeStart, end, includeEnd, -1);
    return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());    
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    return getHistoricalTimeSeries(dataField, identifierBundle, (LocalDate) null, resolutionKey);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, LocalDate currentDate, String resolutionKey) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_RESOLVED)
        .resolveBase(dataField)
        .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
        .resolveBase((resolutionKey != null) ? resolutionKey : NULL_VALUE)
        .resolveQuery("id", identifierBundle.toStringList());
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    return getHistoricalTimeSeries(dataField, identifierBundle, (LocalDate) null, resolutionKey, start, includeStart, end, includeEnd);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    return getHistoricalTimeSeries(dataField, identifierBundle, (LocalDate) null, resolutionKey, start, includeStart, end, includeEnd, maxPoints);
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, LocalDate currentDate, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd, int maxPoints) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_RESOLVED_BY_DATE_LIMIT)
        .resolveBase(dataField)
        .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
        .resolveBase((resolutionKey != null) ? resolutionKey : NULL_VALUE)
        .resolveBase(start == null ? NULL_VALUE : start.toString())
        .resolveBase(String.valueOf(includeStart))
        .resolveBase(end == null ? NULL_VALUE : end.toString())
        .resolveBase(String.valueOf(includeEnd))
        .resolveBase(String.valueOf(maxPoints))
        .resolveQuery("id", identifierBundle.toStringList());
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public HistoricalTimeSeries getHistoricalTimeSeries(
      String dataField, ExternalIdBundle identifierBundle, LocalDate currentDate, String resolutionKey,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    ArgumentChecker.notNull(dataField, "dataField");
    ArgumentChecker.notEmpty(identifierBundle, "identifierBundle");
    final RestTarget target = getTargetBase().resolveBase(REQUEST_RESOLVED_BY_DATE)
        .resolveBase(dataField)
        .resolveBase((currentDate != null) ? currentDate.toString() : NULL_VALUE)
        .resolveBase((resolutionKey != null) ? resolutionKey : NULL_VALUE)
        .resolveBase(start == null ? NULL_VALUE : start.toString())
        .resolveBase(String.valueOf(includeStart))
        .resolveBase(end == null ? NULL_VALUE : end.toString())
        .resolveBase(String.valueOf(includeEnd))
        .resolveQuery("id", identifierBundle.toStringList());
    try {
      return decodeMessage(getRestClient().getMsg(target));
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, String resolutionKey) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(dataField, identifierBundle, null, resolutionKey, null, true, null, true, -1);
    return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(
      String dataField, ExternalIdBundle identifierBundle, String resolutionKey, 
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(dataField, identifierBundle, null, resolutionKey, start, includeStart, end, includeEnd, -1);
    return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, null, true, null, true, -1);
    return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
  }

  @Override
  public Pair<LocalDate, Double> getLatestDataPoint(String dataField, ExternalIdBundle identifierBundle, LocalDate identifierValidityDate, String resolutionKey, LocalDate start, boolean includeStart,
      LocalDate end, boolean includeEnd) {
    HistoricalTimeSeries hts = getHistoricalTimeSeries(dataField, identifierBundle, identifierValidityDate, resolutionKey, start, includeStart, end, includeEnd, -1);
    return new ObjectsPair<LocalDate, Double>(hts.getTimeSeries().getLatestTime(), hts.getTimeSeries().getLatestValue());
  }

  //-------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  @Override
  public Map<ExternalIdBundle, HistoricalTimeSeries> getHistoricalTimeSeries(
      Set<ExternalIdBundle> identifierSet, String dataSource, String dataProvider, String dataField,
      LocalDate start, boolean includeStart, LocalDate end, boolean includeEnd) {
    final RestTarget target = getTargetBase().resolveBase(REQUEST_MULTIPLE);
    FudgeSerializer serializationContext = new FudgeSerializer(getRestClient().getFudgeContext());
    MutableFudgeMsg msg = serializationContext.newMessage();
    serializationContext.addToMessage(msg, REQUEST_IDENTIFIER_SET, null, identifierSet);
    serializationContext.addToMessage(msg, REQUEST_DATA_SOURCE, null, dataSource);
    serializationContext.addToMessage(msg, REQUEST_DATA_PROVIDER, null, dataProvider);
    serializationContext.addToMessage(msg, REQUEST_DATA_FIELD, null, dataField);
    serializationContext.addToMessage(msg, REQUEST_START, null, start);
    serializationContext.addToMessage(msg, REQUEST_INCLUDE_START, null, includeStart);
    serializationContext.addToMessage(msg, REQUEST_END, null, end);
    serializationContext.addToMessage(msg, REQUEST_INCLUDE_END, null, includeEnd);
    final FudgeMsgEnvelope result;
    try {
      result = getRestClient().post(target, msg);
    } catch (RestRuntimeException e) {
      throw e.translate();
    }
    FudgeDeserializer deserializationContext = new FudgeDeserializer(getRestClient().getFudgeContext());
    return deserializationContext.fudgeMsgToObject(Map.class, result.getMessage().getMessage(HISTORICALTIMESERIESSOURCE_TIMESERIES));
  }

}
