/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.bbg.loader.hts;

import static com.opengamma.bbg.BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME;
import static com.opengamma.bbg.BloombergConstants.DEFAULT_START_DATE;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.threeten.bp.LocalDate;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.bbg.BloombergConstants;
import com.opengamma.bbg.util.BloombergDataUtils;
import com.opengamma.core.historicaltimeseries.HistoricalTimeSeriesConstants;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.id.ExternalIdWithDates;
import com.opengamma.id.ObjectId;
import com.opengamma.id.UniqueId;
import com.opengamma.master.historicaltimeseries.ExternalIdResolver;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesInfoSearchIterator;
import com.opengamma.provider.historicaltimeseries.HistoricalTimeSeriesProvider;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
import com.opengamma.util.MapUtils;
import com.opengamma.util.PoolExecutor;
import com.opengamma.util.PoolExecutor.CompletionListener;
import com.opengamma.util.PoolExecutor.Service;
import com.opengamma.util.time.DateUtils;
import com.opengamma.util.time.LocalDateRange;

/**
 * Updates the Bloomberg timeseries for a given timeSeries master or database
 * <p>
 * This loads missing historical time-series data from Bloomberg.
 */
public class BloombergHTSMasterUpdater {

  /** Logger. */
  private static final Logger s_logger = LoggerFactory.getLogger(BloombergHTSMasterUpdater.class);

  private final HistoricalTimeSeriesMaster _timeSeriesMaster;
  private final HistoricalTimeSeriesProvider _historicalTimeSeriesProvider;

  private LocalDate _startDate;
  private LocalDate _endDate;
  private boolean _reload;

  public BloombergHTSMasterUpdater(final HistoricalTimeSeriesMaster htsMaster,
      final HistoricalTimeSeriesProvider underlyingHtsProvider,
      final ExternalIdResolver identifierProvider) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    ArgumentChecker.notNull(underlyingHtsProvider, "underlyingHtsProvider");
    ArgumentChecker.notNull(identifierProvider, "identifierProvider");
    _timeSeriesMaster = htsMaster;
    _historicalTimeSeriesProvider = underlyingHtsProvider;
  }

  /**
   * Sets the startDate field.
   * 
   * @param startDate the startDate
   */
  public void setStartDate(LocalDate startDate) {
    _startDate = startDate;
  }

  /**
   * Sets the endDate field.
   * 
   * @param endDate the endDate
   */
  public void setEndDate(LocalDate endDate) {
    _endDate = endDate;
  }

  /**
   * Sets the reload field.
   * 
   * @param reload the reload
   */
  public void setReload(boolean reload) {
    _reload = reload;
  }

  public void run() {
    if (_reload) {
      if (_startDate == null) {
        _startDate = DEFAULT_START_DATE;
      }
      if (_endDate == null) {
        _endDate = LocalDate.MAX;
      }
    }
    updateTimeSeries();
  }

  //-------------------------------------------------------------------------
  /**
   * Check a time series entry to see if it requires updating and update request and lookup data structures
   * @param doc time series info document of the time series to update
   * @param metaDataKeyMap map from a meta data key to a set of object ids
   * @param bbgTSRequest data structure containing entries for start dates, each with a chain of maps to link providers and fields to id bundles
   * @return whether to update this time series
   */
  protected boolean checkForUpdates(final HistoricalTimeSeriesInfoDocument doc, final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap,
                                    final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> bbgTSRequest) {
    ManageableHistoricalTimeSeriesInfo info = doc.getInfo();
    ExternalIdBundle idBundle = info.getExternalIdBundle().toBundle();
    // select start date
    LocalDate startDate = _startDate;
    if (startDate == null) {
      // lookup start date as one day after the latest point in the series
      UniqueId htsId = doc.getInfo().getUniqueId();
      LocalDate latestDate = getLatestDate(htsId);
      if (isUpToDate(latestDate, doc.getInfo().getObservationTime())) {
        s_logger.debug("Not scheduling update for up to date series {} from {}", htsId, latestDate);
        return false; // up to date, so do not fetch
      }
      s_logger.debug("Scheduling update for series {} from {}", htsId, latestDate);
      startDate = DateUtils.nextWeekDay(latestDate);
    }
    String dataProvider = info.getDataProvider();
    String dataField = info.getDataField();
    synchronized (bbgTSRequest) {
      Map<String, Map<String, Set<ExternalIdBundle>>> providerFieldIdentifiers = MapUtils.putIfAbsentGet(bbgTSRequest, startDate, new HashMap<String, Map<String, Set<ExternalIdBundle>>>());
      Map<String, Set<ExternalIdBundle>> fieldIdentifiers = MapUtils.putIfAbsentGet(providerFieldIdentifiers, dataProvider, new HashMap<String, Set<ExternalIdBundle>>());
      Set<ExternalIdBundle> identifiers = MapUtils.putIfAbsentGet(fieldIdentifiers, dataField, new HashSet<ExternalIdBundle>());
      identifiers.add(idBundle);
    }
    MetaDataKey metaDataKey = new MetaDataKey(idBundle, dataProvider, dataField);
    ObjectId previous;
    synchronized (metaDataKeyMap) {
      ObjectId objectId = doc.getInfo().getTimeSeriesObjectId();
      Set<ObjectId> objectIds = MapUtils.putIfAbsentGet(metaDataKeyMap, metaDataKey, Sets.newHashSet(objectId));
      if (objectIds != null) {
        s_logger.warn("Duplicate time series for {}", metaDataKey._identifiers);
        objectIds.add(objectId);
      }
    }
    return true;
  }

  protected void checkForUpdates(final Collection<HistoricalTimeSeriesInfoDocument> documents, final Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap,
      final Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> bbgTSRequest) {
    final List<RuntimeException> failures = (_startDate == null) ? new ArrayList<RuntimeException>() : null;
    // Looking up the most recent date can be a costly database operation; mitigate slightly with a pool of threads
    final Service<Boolean> service = (_startDate == null) ? new PoolExecutor(10, "HTS checker").createService(new CompletionListener<Boolean>() {

      @Override
      public void success(Boolean result) {
        // Ignore
      }

      @Override
      public void failure(Throwable error) {
        synchronized (failures) {
          if (error instanceof RuntimeException) {
            failures.add((RuntimeException) error);
          } else {
            failures.add(new OpenGammaRuntimeException("Checked", error));
          }
        }
      }

    }) : null;
    for (final HistoricalTimeSeriesInfoDocument doc : documents) {
      if (service != null) {
        service.execute(new Callable<Boolean>() {
          @Override
          public Boolean call() throws Exception {
            return checkForUpdates(doc, metaDataKeyMap, bbgTSRequest);
          }
        });
      } else {
        checkForUpdates(doc, metaDataKeyMap, bbgTSRequest);
      }
    }
    if (service != null) {
      try {
        service.join();
        for (RuntimeException failure : failures) {
          throw failure;
        }
      } catch (InterruptedException e) {
        throw new OpenGammaRuntimeException("Interrupted", e);
      }
    }
  }

  protected void updateTimeSeries() {
    // load the info documents for all Bloomberg series that can be updated
    s_logger.info("Loading all time series information...");
    List<HistoricalTimeSeriesInfoDocument> documents = getCurrentTimeSeriesDocuments();
    s_logger.info("Loaded {} time series.", documents.size());
    // group Bloomberg request by dates/dataProviders/dataFields
    Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> bbgTSRequest = Maps.newHashMap();
    // store identifier to UID map for timeseries update
    Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap = new HashMap<>();
    if (_startDate != null) {
      bbgTSRequest.put(_startDate, new HashMap<String, Map<String, Set<ExternalIdBundle>>>());
    }
    checkForUpdates(documents, metaDataKeyMap, bbgTSRequest);
    // select end date
    LocalDate endDate = resolveEndDate();
    s_logger.info("Updating {} time series to {}", metaDataKeyMap, endDate);
    // load from Bloomberg and store in database
    getAndUpdateHistoricalData(bbgTSRequest, metaDataKeyMap, endDate);
  }

  private LocalDate resolveEndDate() {
    return _endDate == null ? LocalDate.MAX : _endDate;
  }

  private LocalDate getLatestDate(UniqueId htsId) {
    LocalDateDoubleTimeSeries timeSeries = _timeSeriesMaster.getTimeSeries(htsId,
        HistoricalTimeSeriesGetFilter.ofLatestPoint()).getTimeSeries();
    if (timeSeries.isEmpty()) {
      return DEFAULT_START_DATE;
    } else {
      return timeSeries.getLatestTime();
    }
  }

  private boolean isUpToDate(LocalDate latestDate, String observationTime) {
    LocalDate previousWeekDay = null;
    if (observationTime.equalsIgnoreCase(HistoricalTimeSeriesConstants.TOKYO_CLOSE)) {
      previousWeekDay = DateUtils.previousWeekDay().plusDays(1);
    } else {
      previousWeekDay = DateUtils.previousWeekDay();
    }
    return previousWeekDay.isBefore(latestDate) || previousWeekDay.equals(latestDate);
  }

  //-------------------------------------------------------------------------
  private List<HistoricalTimeSeriesInfoDocument> getCurrentTimeSeriesDocuments() {
    // loads all time-series that were originally loaded from Bloomberg
    HistoricalTimeSeriesInfoSearchRequest request = new HistoricalTimeSeriesInfoSearchRequest();
    request.setDataSource(BLOOMBERG_DATA_SOURCE_NAME);
    return removeExpiredTimeSeries(HistoricalTimeSeriesInfoSearchIterator.iterable(_timeSeriesMaster, request));
  }

  private List<HistoricalTimeSeriesInfoDocument> removeExpiredTimeSeries(final Iterable<HistoricalTimeSeriesInfoDocument> searchIterable) {
    List<HistoricalTimeSeriesInfoDocument> result = Lists.newArrayList();
    LocalDate previousWeekDay = DateUtils.previousWeekDay();

    for (HistoricalTimeSeriesInfoDocument htsInfoDoc : searchIterable) {
      ManageableHistoricalTimeSeriesInfo tsInfo = htsInfoDoc.getInfo();

      boolean valid = getIsValidOn(previousWeekDay, tsInfo);
      if (valid) {
        result.add(htsInfoDoc);
      } else {
        s_logger.debug("Time series {} is not valid on {}", tsInfo.getUniqueId(), previousWeekDay);
      }
    }
    return result;
  }

  private boolean getIsValidOn(LocalDate previousWeekDay, ManageableHistoricalTimeSeriesInfo tsInfo) {
    boolean anyInvalid = false;
    for (ExternalIdWithDates id : tsInfo.getExternalIdBundle()) {
      if (id.isValidOn(previousWeekDay)) {
        if (id.getValidFrom() != null || id.getValidTo() != null) {
          //[PLAT-1724] If there is a ticker with expiry, which is valid, that's ok
          return true;
        }
      } else {
        anyInvalid = true;
      }
    }
    // Otherwise be very strict, since many things have tickers with no expiry 
    return !anyInvalid;
  }

  //-------------------------------------------------------------------------
  private void getAndUpdateHistoricalData(Map<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> bbgTSRequest,
      Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap, LocalDate endDate) {
    // process the request
    for (Entry<LocalDate, Map<String, Map<String, Set<ExternalIdBundle>>>> entry : bbgTSRequest.entrySet()) {
      s_logger.debug("processing {}", entry);
      // if we're reloading we should get the whole ts, not just the end...
      LocalDate startDate = _reload ? DEFAULT_START_DATE : entry.getKey();

      for (Entry<String, Map<String, Set<ExternalIdBundle>>> providerFieldIdentifiers : entry.getValue().entrySet()) {
        s_logger.debug("processing {}", providerFieldIdentifiers);
        String dataProvider = providerFieldIdentifiers.getKey();

        for (Entry<String, Set<ExternalIdBundle>> fieldIdentifiers : providerFieldIdentifiers.getValue().entrySet()) {
          s_logger.debug("processing {}", fieldIdentifiers);
          String dataField = fieldIdentifiers.getKey();
          Set<ExternalIdBundle> identifiers = fieldIdentifiers.getValue();

          String bbgDataProvider = BloombergDataUtils.resolveDataProvider(dataProvider);
          Map<ExternalIdBundle, LocalDateDoubleTimeSeries> bbgLoadedTS = getTimeSeries(dataField, startDate, endDate, bbgDataProvider, identifiers);
          if (bbgLoadedTS.size() < identifiers.size()) {
            for (ExternalIdBundle failure : Sets.difference(identifiers, bbgLoadedTS.keySet())) {
              s_logger.error("Failed to load time series for {}, {}, {}", failure, dataProvider, dataField);
              errorLoading(new MetaDataKey(failure, dataProvider, dataField));
            }
          }
          updateTimeSeriesMaster(bbgLoadedTS, metaDataKeyMap, dataProvider, dataField);
        }
      }
    }
  }

  private void updateTimeSeriesMaster(Map<ExternalIdBundle, LocalDateDoubleTimeSeries> bbgLoadedTS, Map<MetaDataKey, Set<ObjectId>> metaDataKeyMap, String dataProvider, String dataField) {
    for (Entry<ExternalIdBundle, LocalDateDoubleTimeSeries> identifierTS : bbgLoadedTS.entrySet()) {
      // ensure data points are after the last stored data point
      LocalDateDoubleTimeSeries timeSeries = identifierTS.getValue();
      if (timeSeries.isEmpty()) {
        s_logger.info("No new data for series {} {}", dataField, identifierTS.getKey());
        continue; // avoids errors in getLatestTime()
      }
      s_logger.info("Got {} new points for series {} {}", new Object[] {timeSeries.size(), dataField, identifierTS.getKey() });

      LocalDate latestTime = timeSeries.getLatestTime();
      LocalDate startDate = (_startDate != null ? _startDate : DEFAULT_START_DATE);
      timeSeries = timeSeries.subSeries(startDate, true, latestTime, true);
      if (timeSeries != null && timeSeries.isEmpty() == false) {
        // metaDataKeyMap holds the object id of the series to be updated
        ExternalIdBundle idBundle = identifierTS.getKey();
        MetaDataKey metaDataKey = new MetaDataKey(idBundle, dataProvider, dataField);
        for (ObjectId oid : metaDataKeyMap.get(metaDataKey)) {
          try { 
            if (_reload) {
              _timeSeriesMaster.correctTimeSeriesDataPoints(oid, timeSeries);
            } else {
              _timeSeriesMaster.updateTimeSeriesDataPoints(oid, timeSeries);
            }
          } catch (Exception ex) {
            s_logger.error("Error writing time-series " + oid, ex);
            if (metaDataKeyMap.get(metaDataKey).size() > 1) {
              s_logger.error("This is probably because there are multiple time series for {} with differing lengths.  Manually delete one or the other.", metaDataKey._identifiers);
            }
            errorLoading(metaDataKey);
          }
        }
      }
    }
  }

  protected void errorLoading(MetaDataKey timeSeries) {
    // No-op
  }

  //-------------------------------------------------------------------------
  /**
   * Lookup data.
   */
  protected static final class MetaDataKey {

    private final ExternalIdBundle _identifiers;
    private final String _dataProvider;
    private final String _field;

    public MetaDataKey(ExternalIdBundle identifiers, String dataProvider, String field) {
      _identifiers = identifiers;
      _dataProvider = dataProvider;
      _field = field;
    }

    @Override
    public int hashCode() {
      return _identifiers.hashCode() ^ _field.hashCode();
    }

    @Override
    public String toString() {
      return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null) {
        return false;
      }
      if (!(obj instanceof MetaDataKey)) {
        return false;
      }
      MetaDataKey other = (MetaDataKey) obj;
      if (_field == null) {
        if (other._field != null) {
          return false;
        }
      } else if (!_field.equals(other._field)) {
        return false;
      }
      if (_identifiers == null) {
        if (other._identifiers != null) {
          return false;
        }
      } else if (!_identifiers.equals(other._identifiers)) {
        return false;
      }
      if (_dataProvider == null) {
        if (other._dataProvider != null) {
          return false;
        }
      } else if (!_dataProvider.equals(other._dataProvider)) {
        return false;
      }
      return true;
    }
  }

  //-------------------------------------------------------------------------
  protected Map<ExternalIdBundle, LocalDateDoubleTimeSeries> getTimeSeries(
      final String dataField, final LocalDate startDate, final LocalDate endDate, String bbgDataProvider, Set<ExternalIdBundle> identifierSet) {
    s_logger.debug("Loading time series {} ({}-{}) {}: {}", new Object[] {dataField, startDate, endDate, bbgDataProvider, identifierSet });
    LocalDateRange dateRange = LocalDateRange.of(startDate, endDate, true);
    return _historicalTimeSeriesProvider.getHistoricalTimeSeries(identifierSet, BloombergConstants.BLOOMBERG_DATA_SOURCE_NAME, bbgDataProvider, dataField, dateRange);
  }

}
