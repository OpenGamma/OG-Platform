/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.loader.timeseries;

import java.util.HashMap;
import java.util.Map;

import com.opengamma.financial.tool.ToolContext;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ObjectId;
import com.opengamma.master.historicaltimeseries.impl.HistoricalTimeSeriesWriter;
import com.opengamma.util.timeseries.localdate.LocalDateDoubleTimeSeries;

/**
 * Time-series writer.
 */
public class MasterTimeSeriesWriter implements TimeSeriesWriter {

  private HistoricalTimeSeriesWriter _htsWriter;
  
  private Map<ExternalId, ObjectId> _idMap = new HashMap<ExternalId, ObjectId>();
  
  public MasterTimeSeriesWriter(ToolContext toolContext) {
    _htsWriter = new HistoricalTimeSeriesWriter(toolContext.getHistoricalTimeSeriesMaster());
  }

  @Override
  public LocalDateDoubleTimeSeries writeDataPoints(ExternalId htsId, String dataSource, String dataProvider, String dataField,   
      String observationTime, LocalDateDoubleTimeSeries series) {
    
    // Attempt to avoid external ID lookup by using local cache to determine the time series's object id
    ObjectId oId = _idMap.get(htsId);
    if (oId == null) {
      oId = _htsWriter.writeTimeSeries(
          observationTime, dataSource, dataProvider, dataField, observationTime, htsId.toBundle(), series).getObjectId();
      _idMap.put(htsId, oId);
    } else {
      _htsWriter.writeTimeSeries(observationTime, dataSource, dataProvider, dataField, observationTime, oId, series);
    }
    return series;
  }

  @Override
  public void flush() {
    
  }

}
