/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot.impl;

import java.util.Iterator;

import com.opengamma.master.impl.AbstractSearchIterator;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchResult;
import com.opengamma.util.ArgumentChecker;

/**
 * An iterator that searches a snapshot master as an iterator.
 * <p>
 * Large systems may store a large amount of data in each master.
 * A simple search request that pulls back the entire database is unrealistic.
 * This remote iterator allows the database to be queried in a consistent way remotely.
 */
public class MarketDataSnapshotSearchIterator extends AbstractSearchIterator<MarketDataSnapshotDocument, MarketDataSnapshotMaster, MarketDataSnapshotSearchRequest> {

  /**
   * Creates an instance based on a request.
   * <p>
   * The request will be altered during the iteration.
   * 
   * @param master  the underlying master, not null
   * @param request  the request object, not null
   * @return an iterable suitable for use in a for-each loop, not null
   */
  public static Iterable<MarketDataSnapshotDocument> iterable(final MarketDataSnapshotMaster master, final MarketDataSnapshotSearchRequest request) {
    ArgumentChecker.notNull(master, "master");
    ArgumentChecker.notNull(request, "request");
    return new Iterable<MarketDataSnapshotDocument>() {
      @Override
      public Iterator<MarketDataSnapshotDocument> iterator() {
        return new MarketDataSnapshotSearchIterator(master, request);
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
  public MarketDataSnapshotSearchIterator(MarketDataSnapshotMaster master, MarketDataSnapshotSearchRequest request) {
    super(master, request);
  }

  //-------------------------------------------------------------------------
  @Override
  protected MarketDataSnapshotSearchResult doSearch(MarketDataSnapshotSearchRequest request) {
    return getMaster().search(request);
  }

}
