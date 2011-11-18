/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.historicaltimeseries.rest;

import javax.time.calendar.LocalDate;

import org.fudgemsg.FudgeContext;
import org.fudgemsg.FudgeMsgEnvelope;
import org.fudgemsg.mapping.FudgeDeserializer;
import org.fudgemsg.mapping.FudgeSerializer;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesSummary;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.transport.jaxrs.RestClient;
import com.opengamma.transport.jaxrs.RestRuntimeException;
import com.opengamma.transport.jaxrs.RestTarget;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * A {@code HistoricalTimeSeriesMaster} implementation that connects to a remote one with REST calls.
 */
public class RemoteHistoricalTimeSeriesMaster implements HistoricalTimeSeriesMaster {

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
  public RemoteHistoricalTimeSeriesMaster(final FudgeContext fudgeContext, final RestTarget baseTarget) {
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

  /**
   * Creates and returns a Fudge serializer from the Fudge context.
   * 
   * @return the serializer
   */
  protected FudgeSerializer getFudgeSerializer() {
    return getRestClient().getFudgeSerializer();
  }

  /**
   * Creates and returns a Fudge deserializer from the Fudge context.
   * 
   * @return the deserializer
   */
  protected FudgeDeserializer getFudgeDeserializer() {
    return getRestClient().getFudgeDeserializer();
  }

  @Override
  public HistoricalTimeSeriesInfoDocument get(UniqueId uniqueId) {
    try {
      return getFudgeDeserializer().fudgeMsgToObject(HistoricalTimeSeriesInfoDocument.class, getRestClient().getMsg(getTargetBase().resolveBase("timeSeries").resolve(uniqueId.toString())));
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public HistoricalTimeSeriesInfoDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    // TODO: GET request to timeSeriesObject/objectId/...
    throw new UnsupportedOperationException();
  }

  @Override
  public HistoricalTimeSeriesInfoDocument add(HistoricalTimeSeriesInfoDocument document) {
    try {
      final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolve("add"), getFudgeSerializer().objectToFudgeMsg(document));
      final UniqueId timeSeriesId = getFudgeDeserializer().fieldValueToObject(UniqueId.class, response.getMessage().getByName("uniqueId"));
      document.setUniqueId(timeSeriesId);
      return document;
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public HistoricalTimeSeriesInfoDocument update(HistoricalTimeSeriesInfoDocument document) {
    // TODO: this is wrong; the target path should be timeSeries/uniqueId/... with a PUT operation
    try {
      final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolve("update"), getFudgeSerializer().objectToFudgeMsg(document));
      return getFudgeDeserializer().fudgeMsgToObject(HistoricalTimeSeriesInfoDocument.class, response.getMessage());
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public void remove(UniqueId uniqueId) {
    try {
      getRestClient().delete(getTargetBase().resolveBase("timeSeries").resolve(uniqueId.toString()));
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public HistoricalTimeSeriesInfoDocument correct(HistoricalTimeSeriesInfoDocument document) {
    // TODO: POST request to timeSeries/uniqueId/...
    throw new UnsupportedOperationException();
  }

  @Override
  public ChangeManager changeManager() {
    throw new UnsupportedOperationException();
  }

  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(HistoricalTimeSeriesInfoMetaDataRequest request) {
    // TODO: GET request to metaData
    throw new UnsupportedOperationException();
  }

  @Override
  public HistoricalTimeSeriesInfoSearchResult search(HistoricalTimeSeriesInfoSearchRequest request) {
    try {
      final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolve("search"), getFudgeSerializer().objectToFudgeMsg(request));
      return getFudgeDeserializer().fudgeMsgToObject(HistoricalTimeSeriesInfoSearchResult.class, response.getMessage());
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(HistoricalTimeSeriesInfoHistoryRequest request) {
    // TODO: GET request to timeSeriesObject/objectId/...
    throw new UnsupportedOperationException();
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    try {
      RestTarget target = getTargetBase().resolveBase("timeSeries").resolveBase(uniqueId.toString());
      if (fromDateInclusive != null) {
        target = target.resolveBase("from").resolveBase(fromDateInclusive.toString());
      }
      if (toDateInclusive != null) {
        target = target.resolveBase("to").resolve(toDateInclusive.toString());
      } else {
        if (fromDateInclusive == null) {
          // Append "timeSeries" to distinguish from the basic "get" method
          target = target.resolve("timeSeries");
        } else {
          target = target.resolve(".");
        }
      }
      return getFudgeDeserializer().fudgeMsgToObject(ManageableHistoricalTimeSeries.class, getRestClient().getMsg(target));
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    // TODO: GET request to timeSeriesObject/objectId/...
    throw new UnsupportedOperationException();
  }

  @Override
  public HistoricalTimeSeriesSummary getSummary(UniqueId uniqueId) {
    throw new OpenGammaRuntimeException("Getting remote HTS summary not yet implemented");
  }

  @Override
  public HistoricalTimeSeriesSummary getSummary(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    // TODO: GET request to timeSeriesObject/objectId/...
    throw new UnsupportedOperationException();    
  }
  
  @Override
  public UniqueId updateTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    try {
      final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolveBase("timeSeriesObject").resolveBase(objectId.getObjectId().toString()).resolve("update"),
          getFudgeSerializer().objectToFudgeMsg(series));
      return getFudgeDeserializer().fieldValueToObject(UniqueId.class, response.getMessage().getByName("uniqueId"));
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public UniqueId correctTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    try {
      final FudgeMsgEnvelope response = getRestClient().post(getTargetBase().resolveBase("timeSeriesObject").resolveBase(objectId.getObjectId().toString()).resolve("correct"),
          getFudgeSerializer().objectToFudgeMsg(series));
      return getFudgeDeserializer().fieldValueToObject(UniqueId.class, response.getMessage().getByName("uniqueId"));
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }

  @Override
  public UniqueId removeTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    try {
      RestTarget target = getTargetBase().resolveBase("timeSeriesObject").resolveBase(objectId.getObjectId().toString());
      if (fromDateInclusive != null) {
        target = target.resolveBase("from").resolveBase(fromDateInclusive.toString());
      }
      if (toDateInclusive != null) {
        target = target.resolveBase("to").resolve(toDateInclusive.toString());
      } else {
        target = target.resolve(".");
      }
      final FudgeMsgEnvelope response = getRestClient().delete(target);
      return getFudgeDeserializer().fieldValueToObject(UniqueId.class, response.getMessage().getByName("uniqueId"));
    } catch (RestRuntimeException ex) {
      throw ex.translate();
    }
  }


}
