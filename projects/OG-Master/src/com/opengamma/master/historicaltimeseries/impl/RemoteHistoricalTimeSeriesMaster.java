/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.net.URI;

import javax.time.calendar.LocalDate;

import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoHistoryResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoMetaDataResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.impl.AbstractRemoteMaster;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Provides access to a remote {@link HistoricalTimeSeriesMaster}.
 */
public class RemoteHistoricalTimeSeriesMaster extends AbstractRemoteMaster implements HistoricalTimeSeriesMaster {

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   */
  public RemoteHistoricalTimeSeriesMaster(final URI baseUri) {
    super(baseUri);
  }

  /**
   * Creates an instance.
   * 
   * @param baseUri  the base target URI for all RESTful web services, not null
   * @param changeManager  the change manager, not null
   */
  public RemoteHistoricalTimeSeriesMaster(final URI baseUri, ChangeManager changeManager) {
    super(baseUri, changeManager);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(HistoricalTimeSeriesInfoMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataHistoricalTimeSeriesMasterResource.uriMetaData(getBaseUri(), msgBase64);
    return accessRemote(uri).get(HistoricalTimeSeriesInfoMetaDataResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoSearchResult search(final HistoricalTimeSeriesInfoSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataHistoricalTimeSeriesMasterResource.uri(getBaseUri(), msgBase64);
    return accessRemote(uri).get(HistoricalTimeSeriesInfoSearchResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataHistoricalTimeSeriesResource.uriVersion(getBaseUri(), uniqueId);
      return accessRemote(uri).get(HistoricalTimeSeriesInfoDocument.class);
    } else {
      return get(uniqueId, VersionCorrection.LATEST);
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataHistoricalTimeSeriesResource.uri(getBaseUri(), objectId, versionCorrection);
    return accessRemote(uri).get(HistoricalTimeSeriesInfoDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument add(final HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getInfo(), "document.info");
    
    URI uri = DataHistoricalTimeSeriesMasterResource.uri(getBaseUri(), null);
    return accessRemote(uri).post(HistoricalTimeSeriesInfoDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument update(final HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getInfo(), "document.info");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataHistoricalTimeSeriesResource.uri(getBaseUri(), document.getUniqueId(), VersionCorrection.LATEST);
    return accessRemote(uri).put(HistoricalTimeSeriesInfoDocument.class, document);
  }

  //-------------------------------------------------------------------------
  @Override
  public void remove(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    URI uri = DataHistoricalTimeSeriesResource.uri(getBaseUri(), uniqueId, VersionCorrection.LATEST);
    accessRemote(uri).delete();
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(final HistoricalTimeSeriesInfoHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getObjectId(), "request.objectId");
    
    String msgBase64 = getRestClient().encodeBean(request);
    URI uri = DataHistoricalTimeSeriesResource.uriVersions(getBaseUri(), request.getObjectId(), msgBase64);
    return accessRemote(uri).get(HistoricalTimeSeriesInfoHistoryResult.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument correct(final HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document, "document");
    ArgumentChecker.notNull(document.getInfo(), "document.info");
    ArgumentChecker.notNull(document.getUniqueId(), "document.uniqueId");
    
    URI uri = DataHistoricalTimeSeriesResource.uriVersion(getBaseUri(), document.getUniqueId());
    return accessRemote(uri).get(HistoricalTimeSeriesInfoDocument.class);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      URI uri = DataHistoricalDataPointsResource.uriVersion(getBaseUri(), uniqueId, null);
      return accessRemote(uri).get(ManageableHistoricalTimeSeries.class);
    } else {
      return getTimeSeries(uniqueId, VersionCorrection.LATEST);
    }
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId, HistoricalTimeSeriesGetFilter filter) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    if (uniqueId.isVersioned()) {
      String msgBase64 = getRestClient().encodeBean(filter);
      URI uri = DataHistoricalDataPointsResource.uriVersion(getBaseUri(), uniqueId, msgBase64);
      return accessRemote(uri).get(ManageableHistoricalTimeSeries.class);
    } else {
      return getTimeSeries(uniqueId, VersionCorrection.LATEST);
    }
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataHistoricalDataPointsResource.uri(getBaseUri(), objectId, versionCorrection, null);
    return accessRemote(uri).get(ManageableHistoricalTimeSeries.class);
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection, HistoricalTimeSeriesGetFilter filter) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    String msgBase64 = getRestClient().encodeBean(filter);
    URI uri = DataHistoricalDataPointsResource.uri(getBaseUri(), objectId, versionCorrection, msgBase64);
    return accessRemote(uri).get(ManageableHistoricalTimeSeries.class);
  }

  @Override
  public UniqueId updateTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(series, "series");
    
    URI uri = DataHistoricalDataPointsResource.uriUpdates(getBaseUri(), objectId);
    return accessRemote(uri).post(UniqueId.class, series);
  }

  @Override
  public UniqueId correctTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(series, "series");
    
    URI uri = DataHistoricalDataPointsResource.uriCorrections(getBaseUri(), objectId);
    return accessRemote(uri).post(UniqueId.class, series);
  }

  @Override
  public UniqueId removeTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    ArgumentChecker.notNull(objectId, "objectId");
    
    URI uri = DataHistoricalDataPointsResource.uriRemovals(getBaseUri(), objectId, fromDateInclusive, toDateInclusive);
    return accessRemote(uri).delete(UniqueId.class);
  }

}
