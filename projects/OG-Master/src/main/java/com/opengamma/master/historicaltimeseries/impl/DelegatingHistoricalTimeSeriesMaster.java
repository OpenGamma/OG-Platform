/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.threeten.bp.LocalDate;

import com.opengamma.core.change.AggregatingChangeManager;
import com.opengamma.core.change.ChangeManager;
import com.opengamma.id.ObjectId;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.UniqueIdSchemeDelegator;
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
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;

/**
 * Implementation of an historical time-series master which uses the scheme of the identifier to determine which
 * underlying master should handle the request.
 * <p>
 * If no scheme-specific handler has been registered, a default is used.
 * <p>
 * Change events are aggregated from the different masters and presented through a single change manager.
 */
public class DelegatingHistoricalTimeSeriesMaster extends UniqueIdSchemeDelegator<HistoricalTimeSeriesMaster> implements HistoricalTimeSeriesMaster {

  /**
   * The change manager.
   */
  private final ChangeManager _changeManager;
  
  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultMaster  the master to use when no scheme matches, not null
   */
  public DelegatingHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster defaultMaster) {
    super(defaultMaster);
    _changeManager = defaultMaster.changeManager();
  }
  
  /**
   * Creates an instance specifying the default delegate.
   * 
   * @param defaultMaster  the master to use when no scheme matches, not null
   * @param schemePrefixToMasterMap  the map of masters by scheme to switch on, not null
   */
  public DelegatingHistoricalTimeSeriesMaster(HistoricalTimeSeriesMaster defaultMaster, Map<String, HistoricalTimeSeriesMaster> schemePrefixToMasterMap) {
    super(defaultMaster, schemePrefixToMasterMap);
    AggregatingChangeManager changeManager = new AggregatingChangeManager();
    
    changeManager.addChangeManager(defaultMaster.changeManager());
    for (HistoricalTimeSeriesMaster master : schemePrefixToMasterMap.values()) {
      changeManager.addChangeManager(master.changeManager());
    }
    _changeManager = changeManager;
  }
  
  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoDocument get(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId.getScheme()).get(uniqueId);
  }

  @Override
  public HistoricalTimeSeriesInfoDocument get(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getObjectId().getScheme()).get(objectId, versionCorrection);
  }

  @Override
  public Map<UniqueId, HistoricalTimeSeriesInfoDocument> get(Collection<UniqueId> uniqueIds) {
    Map<UniqueId, HistoricalTimeSeriesInfoDocument> resultMap = newHashMap();
    for (UniqueId uniqueId : uniqueIds) {
      HistoricalTimeSeriesInfoDocument doc = get(uniqueId);
      resultMap.put(uniqueId, doc);
    }
    return resultMap;
  }

  @Override
  public HistoricalTimeSeriesInfoDocument add(HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document, "document");
    return getDefaultDelegate().add(document);
  }

  @Override
  public HistoricalTimeSeriesInfoDocument update(HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getUniqueId().getScheme()).update(document);
  }

  @Override
  public void remove(ObjectIdentifiable objectIdentifiable) {
    ArgumentChecker.notNull(objectIdentifiable, "objectIdentifiable");
    chooseDelegate(objectIdentifiable.getObjectId().getScheme()).remove(objectIdentifiable);
  }

  @Override
  public HistoricalTimeSeriesInfoDocument correct(HistoricalTimeSeriesInfoDocument document) {
    ArgumentChecker.notNull(document, "document");
    return chooseDelegate(document.getObjectId().getScheme()).correct(document);
  }

  @Override
  public List<UniqueId> replaceVersion(UniqueId uniqueId, List<HistoricalTimeSeriesInfoDocument> replacementDocuments) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(uniqueId.getScheme()).replaceVersion(uniqueId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceAllVersions(ObjectIdentifiable objectId, List<HistoricalTimeSeriesInfoDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceAllVersions(objectId, replacementDocuments);
  }

  @Override
  public List<UniqueId> replaceVersions(ObjectIdentifiable objectId, List<HistoricalTimeSeriesInfoDocument> replacementDocuments) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(replacementDocuments, "replacementDocuments");
    return chooseDelegate(objectId.getObjectId().getScheme()).replaceVersions(objectId, replacementDocuments);
  }

  @Override
  public UniqueId replaceVersion(HistoricalTimeSeriesInfoDocument replacementDocument) {
    ArgumentChecker.notNull(replacementDocument, "replacementDocument");
    return chooseDelegate(replacementDocument.getObjectId().getScheme()).replaceVersion(replacementDocument);
  }

  @Override
  public void removeVersion(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    chooseDelegate(uniqueId.getScheme()).removeVersion(uniqueId);
  }

  @Override
  public UniqueId addVersion(ObjectIdentifiable objectId, HistoricalTimeSeriesInfoDocument documentToAdd) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(documentToAdd, "documentToAdd");
    return chooseDelegate(objectId.getObjectId().getScheme()).addVersion(objectId, documentToAdd);
  }

  //-------------------------------------------------------------------------
  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(HistoricalTimeSeriesInfoMetaDataRequest request) {
    ArgumentChecker.notNull(request, "request");
    
    HistoricalTimeSeriesInfoMetaDataResult defaultResult = getDefaultDelegate().metaData(request);
    if (getDelegates().isEmpty()) {
      return defaultResult;
    }
    
    Set<String> dataFields = new HashSet<String>();
    Set<String> dataSources = new HashSet<String>();
    Set<String> dataProviders = new HashSet<String>();
    Set<String> observationTimes = new HashSet<String>();
    dataFields.addAll(defaultResult.getDataFields());
    dataSources.addAll(defaultResult.getDataSources());
    dataProviders.addAll(defaultResult.getDataProviders());
    observationTimes.addAll(defaultResult.getObservationTimes());
    
    for (HistoricalTimeSeriesMaster delegate : getDelegates().values()) {
      HistoricalTimeSeriesInfoMetaDataResult delegateResult = delegate.metaData(request);
      dataFields.addAll(delegateResult.getDataFields());
      dataSources.addAll(delegateResult.getDataSources());
      dataProviders.addAll(delegateResult.getDataProviders());
      observationTimes.addAll(delegateResult.getObservationTimes());
    }
    
    HistoricalTimeSeriesInfoMetaDataResult result = new HistoricalTimeSeriesInfoMetaDataResult();
    result.getDataFields().addAll(dataFields);
    result.getDataSources().addAll(dataSources);
    result.getDataProviders().addAll(dataProviders);
    result.getObservationTimes().addAll(observationTimes);
    return result;
  }

  @Override
  public HistoricalTimeSeriesInfoSearchResult search(HistoricalTimeSeriesInfoSearchRequest request) {
    ArgumentChecker.notNull(request, "request");
    List<ObjectId> ids = request.getObjectIds();
    if (ids == null || ids.isEmpty()) {
      return getDefaultDelegate().search(request);
    }
    return chooseDelegate(ids.get(0).getScheme()).search(request);
  }

  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(HistoricalTimeSeriesInfoHistoryRequest request) {
    ArgumentChecker.notNull(request, "request");
    return chooseDelegate(request.getObjectId().getScheme()).history(request);
  }

  //-------------------------------------------------------------------------
  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    return chooseDelegate(uniqueId.getScheme()).getTimeSeries(uniqueId);
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(UniqueId uniqueId, HistoricalTimeSeriesGetFilter filter) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    ArgumentChecker.notNull(filter, "filter");
    return chooseDelegate(uniqueId.getScheme()).getTimeSeries(uniqueId, filter);
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    return chooseDelegate(objectId.getObjectId().getScheme()).getTimeSeries(objectId, versionCorrection);
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(ObjectIdentifiable objectId, VersionCorrection versionCorrection, HistoricalTimeSeriesGetFilter filter) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(filter, "filter");
    return chooseDelegate(objectId.getObjectId().getScheme()).getTimeSeries(objectId, versionCorrection, filter);
  }

  //-------------------------------------------------------------------------
  @Override
  public UniqueId updateTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(series, "series");
    return chooseDelegate(objectId.getObjectId().getScheme()).updateTimeSeriesDataPoints(objectId, series);
  }

  @Override
  public UniqueId correctTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDateDoubleTimeSeries series) {
    ArgumentChecker.notNull(objectId, "objectId");
    ArgumentChecker.notNull(series, "series");
    return chooseDelegate(objectId.getObjectId().getScheme()).correctTimeSeriesDataPoints(objectId, series);
  }

  @Override
  public UniqueId removeTimeSeriesDataPoints(ObjectIdentifiable objectId, LocalDate fromDateInclusive, LocalDate toDateInclusive) {
    ArgumentChecker.notNull(objectId, "objectId");
    return chooseDelegate(objectId.getObjectId().getScheme()).removeTimeSeriesDataPoints(objectId, fromDateInclusive, toDateInclusive);
  }
  
  //-------------------------------------------------------------------------
  @Override
  public ChangeManager changeManager() {
    return _changeManager;
  }

}
