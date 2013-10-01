/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
 
package com.opengamma.integration.copier.timeseries.writer;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundleWithDates;
import com.opengamma.id.ExternalIdSearch;
import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesGetFilter;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeries;
import com.opengamma.master.historicaltimeseries.ManageableHistoricalTimeSeriesInfo;
import com.opengamma.timeseries.date.localdate.LocalDateDoubleTimeSeries;
import com.opengamma.util.ArgumentChecker;
 
/**
 * A time series writer that writes to an OpenGamma time series master
 */
public class MasterTimeSeriesWriter implements TimeSeriesWriter {

  private HistoricalTimeSeriesMaster _htsMaster;
  private Map<ExternalId, ObjectIdentifiable> _idMap = new HashMap<ExternalId, ObjectIdentifiable>();

  public MasterTimeSeriesWriter(HistoricalTimeSeriesMaster htsMaster) {
    ArgumentChecker.notNull(htsMaster, "htsMaster");
    _htsMaster = htsMaster;
  }

  @Override
  public LocalDateDoubleTimeSeries writeDataPoints(ExternalId htsId, String dataSource, String dataProvider, String dataField,   
      String observationTime, LocalDateDoubleTimeSeries series) {

    ObjectIdentifiable oId = _idMap.get(htsId);

    if (oId == null) {
      // resolve hts name to id of hts
      HistoricalTimeSeriesInfoSearchRequest searchRequest = new HistoricalTimeSeriesInfoSearchRequest();
      searchRequest.setExternalIdSearch(ExternalIdSearch.of(htsId));
      searchRequest.setDataField(dataField);
      searchRequest.setDataProvider(dataProvider);
      searchRequest.setDataSource(dataSource);
      searchRequest.setObservationTime(observationTime);
      HistoricalTimeSeriesInfoSearchResult searchResult = _htsMaster.search(searchRequest);
      oId = searchResult.getFirstDocument() == null ? null : searchResult.getFirstDocument().getObjectId();

      // if this is a new hts, create it
      if (oId == null) {
        ManageableHistoricalTimeSeriesInfo info = new ManageableHistoricalTimeSeriesInfo();
        info.setDataField(dataField);
        info.setDataProvider(dataProvider);
        info.setDataSource(dataSource);
        info.setObservationTime(observationTime);
        info.setExternalIdBundle(ExternalIdBundleWithDates.of(htsId.toBundle()));
        info.setName(dataProvider + " " + dataField + " - " + htsId.getValue());
        HistoricalTimeSeriesInfoDocument infoDoc = new HistoricalTimeSeriesInfoDocument();
        infoDoc.setInfo(info);
        infoDoc = _htsMaster.add(infoDoc);

        oId = infoDoc.getObjectId();
      }

      _idMap.put(htsId, oId);
    }

    // TODO check if update could be used more often instead of correct
    ManageableHistoricalTimeSeries orig = _htsMaster.getTimeSeries(oId, VersionCorrection.LATEST, HistoricalTimeSeriesGetFilter.ofRange(series.getEarliestTime(), null, 1));
    if (orig.getTimeSeries().isEmpty()) {
      _htsMaster.updateTimeSeriesDataPoints(oId, series);
    } else {
      _htsMaster.correctTimeSeriesDataPoints(oId, series);
    }
    return series;
  }

  @Override
  public void flush() {

  }

}
