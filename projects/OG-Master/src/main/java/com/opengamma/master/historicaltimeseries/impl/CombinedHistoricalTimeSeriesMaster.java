/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.List;

import org.threeten.bp.LocalDate;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.ChangeProvidingCombinedMaster;
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

/**
 * A {@link HistoricalTimeSeriesMaster} which delegates its calls to a list of underlying {@link HistoricalTimeSeriesMaster}s.
 * 
 * This class extends {@link ChangeProvidingCombinedMaster} to implement methods specific to the {@link HistoricalTimeSeriesMaster}.
 */
public class CombinedHistoricalTimeSeriesMaster extends ChangeProvidingCombinedMaster<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster> implements HistoricalTimeSeriesMaster {
  
  public CombinedHistoricalTimeSeriesMaster(List<HistoricalTimeSeriesMaster> masterList) {
    super(masterList);
  }

  @Override
  public HistoricalTimeSeriesInfoMetaDataResult metaData(HistoricalTimeSeriesInfoMetaDataRequest request) {
    HistoricalTimeSeriesInfoMetaDataResult result = new HistoricalTimeSeriesInfoMetaDataResult();
    for (HistoricalTimeSeriesMaster master : getMasterList()) {
      HistoricalTimeSeriesInfoMetaDataResult masterResult = master.metaData(request);
      result.getDataFields().addAll(masterResult.getDataFields());
      result.getDataProviders().addAll(masterResult.getDataProviders());
      result.getDataSources().addAll(masterResult.getDataSources());
      result.getObservationTimes().addAll(masterResult.getObservationTimes());
    }
    return result;
  }

  @Override
  public HistoricalTimeSeriesInfoSearchResult search(HistoricalTimeSeriesInfoSearchRequest request) {
    
    final HistoricalTimeSeriesInfoSearchResult result = new HistoricalTimeSeriesInfoSearchResult();
    
    SearchStrategy<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster, HistoricalTimeSeriesInfoSearchRequest> searchStrategy =
        new SearchStrategy<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster, HistoricalTimeSeriesInfoSearchRequest>() {

          @Override
          public HistoricalTimeSeriesInfoSearchResult search(HistoricalTimeSeriesMaster master, HistoricalTimeSeriesInfoSearchRequest searchRequest) {
            HistoricalTimeSeriesInfoSearchResult searchResult = master.search(searchRequest);
            result.setVersionCorrection(searchResult.getVersionCorrection());
            return searchResult;
          }
        };
    
    pagedSearch(searchStrategy, result, request);
    
    return result;
  }

  @Override
  public HistoricalTimeSeriesInfoHistoryResult history(HistoricalTimeSeriesInfoHistoryRequest request) {
    final HistoricalTimeSeriesInfoHistoryResult result = new HistoricalTimeSeriesInfoHistoryResult();
    
    SearchStrategy<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster, HistoricalTimeSeriesInfoHistoryRequest> searchStrategy =
        new SearchStrategy<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster, HistoricalTimeSeriesInfoHistoryRequest>() {

          @Override
          public HistoricalTimeSeriesInfoHistoryResult search(HistoricalTimeSeriesMaster master, HistoricalTimeSeriesInfoHistoryRequest searchRequest) {
            HistoricalTimeSeriesInfoHistoryResult searchResult = master.history(searchRequest);
            searchResult.getInfoList().addAll(result.getInfoList());
            return searchResult;
          }
        };
    
    pagedSearch(searchStrategy, result, request);
    
    return result;

  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final UniqueId uniqueId) {
    return apply(uniqueId.getScheme(), new Try<ManageableHistoricalTimeSeries>() {
      @Override
      public ManageableHistoricalTimeSeries tryMaster(HistoricalTimeSeriesMaster master) {
        return master.getTimeSeries(uniqueId);
      }
    });
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final UniqueId uniqueId, final HistoricalTimeSeriesGetFilter filter) {
    return apply(uniqueId.getScheme(), new Try<ManageableHistoricalTimeSeries>() {
      @Override
      public ManageableHistoricalTimeSeries tryMaster(HistoricalTimeSeriesMaster master) {
        return master.getTimeSeries(uniqueId, filter);
      }
    });
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection) {
    return apply(objectId.getObjectId().getScheme(), new Try<ManageableHistoricalTimeSeries>() {
      @Override
      public ManageableHistoricalTimeSeries tryMaster(HistoricalTimeSeriesMaster master) {
        return master.getTimeSeries(objectId, versionCorrection);
      }
    });
  }

  @Override
  public ManageableHistoricalTimeSeries getTimeSeries(final ObjectIdentifiable objectId, final VersionCorrection versionCorrection, final HistoricalTimeSeriesGetFilter filter) {
    return apply(objectId.getObjectId().getScheme(), new Try<ManageableHistoricalTimeSeries>() {
      @Override
      public ManageableHistoricalTimeSeries tryMaster(HistoricalTimeSeriesMaster master) {
        return master.getTimeSeries(objectId, versionCorrection, filter);
      }
    });
  }

  @Override
  public UniqueId updateTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    return apply(objectId.getObjectId().getScheme(), new Try<UniqueId>() {
      @Override
      public UniqueId tryMaster(HistoricalTimeSeriesMaster master) {
        return master.updateTimeSeriesDataPoints(objectId, series);
      }
    });
  }

  @Override
  public UniqueId correctTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDateDoubleTimeSeries series) {
    return apply(objectId.getObjectId().getScheme(), new Try<UniqueId>() {
      @Override
      public UniqueId tryMaster(HistoricalTimeSeriesMaster master) {
        return master.correctTimeSeriesDataPoints(objectId, series);
      }
    });
  }

  @Override
  public UniqueId removeTimeSeriesDataPoints(final ObjectIdentifiable objectId, final LocalDate fromDateInclusive, final LocalDate toDateInclusive) {
    return apply(objectId.getObjectId().getScheme(), new Try<UniqueId>() {
      @Override
      public UniqueId tryMaster(HistoricalTimeSeriesMaster master) {
        return master.removeTimeSeriesDataPoints(objectId, fromDateInclusive, toDateInclusive);
      }
    });
  }
  
  private <D> D apply(String scheme, Try<D> tryObject) {
    HistoricalTimeSeriesMaster master = getMasterByScheme(scheme);
    if (master != null) {
      return tryObject.tryMaster(master);
    }
    return tryObject.each(scheme);
  }


}
