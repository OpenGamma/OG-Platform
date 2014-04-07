/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader.hts;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeries;
import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ExternalIdSearchType;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.ExternalIdResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesLoaderResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.AbstractHistoricalTimeSeriesLoader;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetRequest;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProviderGetResult;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.OpenGammaClock;
import com.opengamma.util.monitor.OperationTimer;
import com.opengamma.util.time.LocalDateRange;

/**
 * Loads time-series information from Bloomberg into a master.
 * <p>
 * This loads missing historical time-series data from Bloomberg and stores it
 * into a master.
 */
public class BloombergHistoricalTimeSeriesLoader extends AbstractHistoricalTimeSeriesLoader {
  // note that there is relatively little Bloomberg specific code here

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergHistoricalTimeSeriesLoader.class);
  /**
   * No time-series before this date.
   */
  private static final LocalDate DEFAULT_START_DATE = LocalDate.of(1900, 1, 1);

  /**
   * The master.
   */
  private final HistoricalTimeSeriesMaster _htsMaster;
  /**
   * The provider of time-series.
   */
  private final HistoricalTimeSeriesProvider _underlyingHtsProvider;
  /**
   * The resolver of identifiers.
   */
  private final ExternalIdResolver _identifierResolver;

  /**
   * Creates an instance.
   * 
   * @param htsMaster  the time-series master, not null
   * @param underlyingHtsProvider  the time-series provider for the underlying data source, not null
   * @param identifierProvider  the identifier resolver for the underlying data source, not null
   */
  public BloombergHistoricalTimeSeriesLoader(
      final HistoricalTimeSeriesMaster htsMaster,
      final HistoricalTimeSeriesProvider underlyingHtsProvider,
      final ExternalIdResolver identifierProvider) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(underlyingHtsProvider, "underlyingHtsProvider");
    ArgumentChecker.notNull(identifierProvider, "identifierProvider");
    _htsMaster = htsMaster;
    _underlyingHtsProvider = underlyingHtsProvider;
    _identifierResolver = identifierProvider;
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesLoaderResult doBulkLoad(HistoricalTimeSeriesLoaderRequest request) {
    ArgumentChecker.notNull(request, "request");
    ArgumentChecker.notNull(request.getDataField(), "dataField");
    
    Set<ExternalId> externalIds = request.getExternalIds();
    LocalDate startDate = request.getStartDate();
    LocalDate endDate = request.getEndDate();
    String dataProvider = request.getDataProvider();
    String dataField = request.getDataField();
    dataProvider = BloombergDataUtils.resolveDataProvider(dataProvider);
    if (startDate == null) {
      startDate = DEFAULT_START_DATE;
    }
    if (endDate == null) {
      endDate = LocalDate.MAX;
    }
    
    // finds the time-series that need loading
    Map<ExternalId, UniqueId> resultMap = new HashMap<ExternalId, UniqueId>();
    Set<ExternalId> missingTimeseries = findTimeSeries(externalIds, dataProvider, dataField, resultMap);
    
    // batch in groups of 100 to avoid out-of-memory issues
    for (List<ExternalId> partition : Iterables.partition(missingTimeseries, 100)) {
      Set<ExternalId> subSet = Sets.newHashSet(partition);
      fetchTimeSeries(subSet, dataField, dataProvider, startDate, endDate, resultMap);
    }
    return new HistoricalTimeSeriesLoaderResult(resultMap);
  }

  /**
   * Finds those time-series that are not in the master.
   * 
   * @param externalIds  the identifiers to lookup, not null
   * @param dataProvider  the data provider, not null
   * @param dataField  the data field, not null
   * @param result  the result map of identifiers, updated if already in database, not null
   * @return the missing identifiers, not null
   */
  protected Set<ExternalId> findTimeSeries(final Set<ExternalId> externalIds, final String dataProvider, final String dataField, final Map<ExternalId, UniqueId> result) {
    HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
    searchRequest.addExternalIds(externalIds);
    searchRequest.setDataField(dataField);
    if (dataProvider == null) {
      searchRequest.setDataProvider(BloombergConstants.DEFAULT_DATA_PROVIDER);
    } else {
      searchRequest.setDataProvider(dataProvider);
    }
    searchRequest.setDataSource(BLOOMBERG_DATA_SOURCE_NAME);
    HistoricalTimeSeriesInfoSearchResult searchResult = _htsMaster.search(searchRequest);
    
    Set<ExternalId> missing = new HashSet<ExternalId>(externalIds);
    for (HistoricalTimeSeriesInfoDocument doc : searchResult.getDocuments()) {
      Set<ExternalId> intersection = Sets.intersection(doc.getInfo().getExternalIdBundle().toBundle().getExternalIds(), externalIds).immutableCopy();
      if (intersection.size() == 1) {
        ExternalId identifier = intersection.iterator().next();
        missing.remove(identifier);
        result.put(identifier, doc.getUniqueId());
      } else {
        throw new OpenGammaRuntimeException("Unable to match single identifier: " + doc.getInfo().getExternalIdBundle());
      }
    }
    return missing;
  }

  /**
   * Fetches the time-series from Bloomberg and stores them in the master.
   * 
   * @param identifiers  the identifiers to fetch, not null
   * @param dataField  the data field, not null
   * @param dataProvider  the data provider, not null
   * @param startDate  the start date to load, not null
   * @param endDate  the end date to load, not null
   * @param result  the result map of identifiers, updated if already in database, not null
   */
  protected void fetchTimeSeries(
      final Set<ExternalId> identifiers, final String dataField, final String dataProvider, final LocalDate startDate, final LocalDate endDate, final Map<ExternalId, UniqueId> result) {
    
    Map<ExternalIdBundleWithDates, ExternalId> withDates2ExternalId = new HashMap<ExternalIdBundleWithDates, ExternalId>();
    Map<ExternalIdBundle, ExternalIdBundleWithDates> bundle2WithDates = new HashMap<ExternalIdBundle, ExternalIdBundleWithDates>();
    
    // lookup full set of identifiers
    Map<ExternalId, ExternalIdBundleWithDates> externalId2WithDates = _identifierResolver.getExternalIds(identifiers);
    
    // reverse map and normalize identifiers
    for (Entry<ExternalId, ExternalIdBundleWithDates> entry : externalId2WithDates.entrySet()) {
      ExternalId requestIdentifier = entry.getKey();
      ExternalIdBundleWithDates bundle = entry.getValue();
      bundle = BloombergDataUtils.addTwoDigitYearCode(bundle);
      bundle2WithDates.put(bundle.toBundle(), bundle);
      withDates2ExternalId.put(bundle, requestIdentifier);
    }
    
    // fetch time-series and store to master
    if (bundle2WithDates.size() > 0) {
      int identifiersSize = bundle2WithDates.keySet().size();
      if (bundle2WithDates.size() == 1) {
        System.out.printf("Loading ts for %s: dataField: %s dataProvider: %s startDate: %s endDate: %s\n", Iterables.get(bundle2WithDates.keySet(), 0), dataField, dataProvider, startDate, endDate);
      } else {
        System.out.printf("Loading %d ts:  dataField: %s dataProvider: %s startDate: %s endDate: %s\n", identifiersSize, dataField, dataProvider, startDate, endDate);
      }
      OperationTimer timer = new OperationTimer(s_logger, " loading " + identifiersSize + " timeseries from Bloomberg");
      final HistoricalTimeSeriesProviderGetResult tsResult = provideTimeSeries(bundle2WithDates.keySet(), dataField, dataProvider, startDate, endDate);
      timer.finished();
      
      timer = new OperationTimer(s_logger, " storing " + identifiersSize + " timeseries from Bloomberg");
      storeTimeSeries(tsResult, dataField, dataProvider, withDates2ExternalId, bundle2WithDates, result);
      timer.finished();
    }
  }

  /**
   * Loads time-series from the underlying source.
   * 
   * @param externalIds  the external identifies to load, not null
   * @param dataField  the data field, not null
   * @param dataProvider  the data provider, not null
   * @param startDate  the start date to load, not null
   * @param endDate  the end date to load, not null
   * @return the map of results, not null
   */
  protected HistoricalTimeSeriesProviderGetResult provideTimeSeries(
      Set<ExternalIdBundle> externalIds, String dataField, String dataProvider, LocalDate startDate, LocalDate endDate) {
    s_logger.debug("Loading time series {} ({}-{}) {}: {}", new Object[] {dataField, startDate, endDate, dataProvider, externalIds });
    LocalDateRange dateRange = LocalDateRange.of(startDate, endDate, true);

    HistoricalTimeSeriesProviderGetRequest request = HistoricalTimeSeriesProviderGetRequest.createGetBulk(externalIds,
        BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME, dataProvider, dataField, dateRange);
    return _underlyingHtsProvider.getHistoricalTimeSeries(request);
  }

  /**
   * Stores the time-series in the master.
   * 
   * @param tsResult  the time-series result, not null
   * @param dataField  the data field, not null
   * @param dataProvider  the data provider, not null
   * @param bundleToIdentifier  the lookup map, not null
   * @param identifiersToBundleWithDates  the lookup map, not null
   * @param result  the result map of identifiers, updated if already in database, not null
   */
  protected void storeTimeSeries(
      HistoricalTimeSeriesProviderGetResult tsResult,
      String dataField, String dataProvider,
      Map<ExternalIdBundleWithDates, ExternalId> bundleToIdentifier,
      Map<ExternalIdBundle, ExternalIdBundleWithDates> identifiersToBundleWithDates,
      Map<ExternalId, UniqueId> result) {

    Map<ExternalIdBundle, LocalDateDoubleTimeSeries> tsMap = tsResult.getResultMap();

    // Add timeseries to data store
    for (Entry<ExternalIdBundle, LocalDateDoubleTimeSeries> entry : tsMap.entrySet()) {
      ExternalIdBundle identifers = entry.getKey();
      LocalDateDoubleTimeSeries timeSeries = entry.getValue();
      if (timeSeries != null && !timeSeries.isEmpty()) {
        ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
        ExternalIdBundleWithDates bundleWithDates = identifiersToBundleWithDates.get(identifers);
        info.setExternalIdBundle(bundleWithDates);
        info.setDataField(dataField);
        info.setDataSource(BLOOMBERG_DATA_SOURCE_NAME);
        ExternalIdBundle bundle = bundleWithDates.toBundle(LocalDate.now(OpenGammaClock.getInstance()));
        String idStr = Objects.firstNonNull(
            bundle.getValue(ExternalSchemes.BLOOMBERG_TICKER),
            Objects.firstNonNull(
              bundle.getExternalId(ExternalSchemes.BLOOMBERG_BUID),
              bundle.getExternalIds().iterator().next())).toString();
        info.setName(dataField + " " + idStr);
        info.setDataProvider(dataProvider);
        String resolvedObservationTime = BloombergDataUtils.resolveObservationTime(dataProvider);
        if (resolvedObservationTime == null) {
          throw new OpenGammaRuntimeException("Unable to resolve observation time from given dataProvider: " + dataProvider);
        }
        info.setObservationTime(resolvedObservationTime);

        Map<ExternalIdBundle, Set<String>> permissionsMap = tsResult.getPermissionsMap();
        if (permissionsMap != null) {
          Set<String> permissions = permissionsMap.get(identifers);
          if (permissions != null) {
            info.getPermissions().addAll(permissions);
          }
        }
        
        // get time-series creating if necessary
        HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
        request.setDataField(info.getDataField());
        request.setDataSource(info.getDataSource());
        request.setDataProvider(info.getDataProvider());
        request.setObservationTime(info.getObservationTime());
        request.setExternalIdSearch(ExternalIdSearch.of(ExternalIdSearchType.EXACT, info.getExternalIdBundle().toBundle()));
        HistoricalTimeSeriesInfoSearchResult searchResult = _htsMaster.search(request);
        if (searchResult.getDocuments().size() == 0) {
          // add new
          HistoricalTimeSeriesInfoDocument doc = _htsMaster.add(new HistoricalTimeSeriesInfoDocument(info));
          UniqueId uniqueId = _htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), timeSeries);
          result.put(bundleToIdentifier.get(bundleWithDates), uniqueId);
        } else {
          // update existing
          HistoricalTimeSeriesInfoDocument doc = searchResult.getDocuments().get(0);
          if (info.getPermissions().equals(doc.getInfo().getPermissions()) == false) {
            doc.setInfo(info);
            doc = _htsMaster.update(doc);
          }

          HistoricalTimeSeries existingSeries = _htsMaster.getTimeSeries(doc.getInfo().getTimeSeriesObjectId(), VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofLatestPoint());
          if (existingSeries.getTimeSeries().size() > 0) {
            LocalDate latestTime = existingSeries.getTimeSeries().getLatestTime();
            timeSeries = timeSeries.subSeries(latestTime, false, timeSeries.getLatestTime(), true);
          }
          UniqueId uniqueId = existingSeries.getUniqueId();
          if (timeSeries.size() > 0) {
            uniqueId = _htsMaster.updateTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), timeSeries);
          }
          result.put(bundleToIdentifier.get(bundleWithDates), uniqueId);
        }
        
      } else {
        s_logger.warn("Empty historical data returned for {}", identifers);
      }
    }
  }

  //-------------------------------------------------------------------------
  @Override
  public boolean updateTimeSeries(final UniqueId uniqueId) {
    ArgumentChecker.notNull(uniqueId, "uniqueId");
    
    HistoricalTimeSeriesInfoDocument doc = _htsMaster.get(uniqueId);
    ManageableHistoricalTimeSeriesInfo info = doc.getInfo();
    ExternalIdBundle externalIdBundle = info.getExternalIdBundle().toBundle();
    String dataSource = info.getDataSource();
    String dataProvider = info.getDataProvider();
    String dataField = info.getDataField();
    LocalDateDoubleTimeSeries series = _underlyingHtsProvider.getHistoricalTimeSeries(externalIdBundle, dataSource, dataProvider, dataField);
    if (series == null || series.isEmpty()) {
      return false;
    }
    _htsMaster.correctTimeSeriesDataPoints(doc.getInfo().getTimeSeriesObjectId(), series);
    return true;
  }

}
