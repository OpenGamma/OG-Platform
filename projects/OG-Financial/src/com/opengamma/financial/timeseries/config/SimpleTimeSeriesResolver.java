/**
 * Copyright (C) 2009 - 2010 by OpenGamma Inc.
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.timeseries.config;

import java.util.ArrayList;
import java.util.List;

import com.opengamma.financial.timeseries.TimeSeriesDocument;
import com.opengamma.financial.timeseries.TimeSeriesMaster;
import com.opengamma.financial.timeseries.TimeSeriesMetaData;
import com.opengamma.financial.timeseries.TimeSeriesMetaDataResolver;
import com.opengamma.financial.timeseries.TimeSeriesSearchRequest;
import com.opengamma.financial.timeseries.TimeSeriesSearchResult;
import com.opengamma.id.IdentifierBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Simple timeSeries resolver, returns the best match from the timeseries metadata in datastore
 * @param <T> the type of the timeseries i.e LocalDate/LocalDateTime
 */
public class SimpleTimeSeriesResolver<T> implements TimeSeriesMetaDataResolver {

  private static final String DEFAULT_DATA_FIELD = "PX_LAST";

  private static final String DEFAULT_DATA_PROVIDER = "CMPL";

  private static final Object DEFAULT_DATA_SOURCE = "BLOOMBERG";
  
  private final TimeSeriesMaster<T> _tsMaster;
  
  /**
   * @param tsMaster the timeseries master, not-null
   */
  public SimpleTimeSeriesResolver(TimeSeriesMaster<T> tsMaster) {
    ArgumentChecker.notNull(tsMaster, "timeseries master");
    _tsMaster = tsMaster;
  }

  @Override
  public TimeSeriesMetaData getDefaultMetaData(IdentifierBundle identifiers) {
    TimeSeriesSearchRequest<T> searchRequest = new TimeSeriesSearchRequest<T>();
    searchRequest.setIdentifiers(identifiers.getIdentifiers());
    searchRequest.setLoadTimeSeries(false);
    
    TimeSeriesSearchResult<T> searchResult = _tsMaster.searchTimeSeries(searchRequest);
    if (searchResult == null) {
      return null;
    }
    
    List<TimeSeriesDocument<T>> documents = searchResult.getDocuments();
    List<TimeSeriesMetaData> metaDataList = new ArrayList<TimeSeriesMetaData>(documents.size());
    
    for (TimeSeriesDocument<T> tsDocument : documents) {
      if (tsDocument.getDataField().equals(DEFAULT_DATA_FIELD)) {
        TimeSeriesMetaData tsMetaData = new TimeSeriesMetaData();
        tsMetaData.setDataField(DEFAULT_DATA_FIELD);
        tsMetaData.setDataProvider(tsDocument.getDataProvider());
        tsMetaData.setDataSource(tsDocument.getDataSource());
        metaDataList.add(tsMetaData);
      }
    }
    
    return bestMatch(metaDataList);
  }
  
  /**
   * @param metaDataList
   * @return
   */
  private TimeSeriesMetaData bestMatch(List<TimeSeriesMetaData> metaDataList) {
    for (TimeSeriesMetaData tsMetaData : metaDataList) {
      if (tsMetaData.getDataProvider().equals(DEFAULT_DATA_PROVIDER) && tsMetaData.getDataSource().equals(DEFAULT_DATA_SOURCE)) {
        return tsMetaData;
      }
    }
    return null;
  }

}
