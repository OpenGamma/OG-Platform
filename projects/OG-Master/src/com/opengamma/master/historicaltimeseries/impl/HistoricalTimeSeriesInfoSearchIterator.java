/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.historicaltimeseries.impl;

import java.util.Iterator;

import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoDocument;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchRequest;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesInfoSearchResult;
import com.opengamma.master.historicaltimeseries.HistoricalTimeSeriesMaster;
import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.util.ArgumentChecker;

/**
 * An iterator that searches a time-series master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class HistoricalTimeSeriesInfoSearchIterator extends AbstractSearchIterator<HistoricalTimeSeriesInfoDocument, HistoricalTimeSeriesMaster, HistoricalTimeSeriesInfoSearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<HistoricalTimeSeriesInfoDocument> iterable(final HistoricalTimeSeriesMaster master, final HistoricalTimeSeriesInfoSearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<HistoricalTimeSeriesInfoDocument>() {
      @Override
      public Iterator<HistoricalTimeSeriesInfoDocument> iterator() {
        return new HistoricalTimeSeriesInfoSearchIterator(master, request);
      }
    };
  }

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   */
  public HistoricalTimeSeriesInfoSearchIterator(HistoricalTimeSeriesMaster master, HistoricalTimeSeriesInfoSearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected HistoricalTimeSeriesInfoSearchResult doSearch(HistoricalTimeSeriesInfoSearchRequest request) {
    return getMaster().search(request);
  }

}
