/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.beans.BeanDefinition;

import com.opengamma.OpenGammaRuntimeException;
import com.opengamma.master.AbstractHistoryResult;
import com.opengamma.util.PublicSPI;

/**
 * Result providing the history of a market data snapshot.
 * <p>
 * The returned documents may be a mixture of versions and corrections.
 * The document instant fields are used to identify which are which.
 * See {@link MarketDataSnapshotHistoryRequest} for more details.
 */
@PublicSPI
@BeanDefinition
public class MarketDataSnapshotHistoryResult extends AbstractHistoryResult<MarketDataSnapshotDocument> {

  /**
   * Creates an instance.
   */
  public MarketDataSnapshotHistoryResult() {
  }

  /**
   * Creates an instance.
   * 
   * @param coll  the collection of documents to add, not null
   */
  public MarketDataSnapshotHistoryResult(Collection<MarketDataSnapshotDocument> coll) {
    super(coll);
  }

  //-------------------------------------------------------------------------
  /**
   * Gets the returned snapshots from within the documents.
   * 
   * @return the snapshots, not null
   */
  public List<ManageableMarketDataSnapshot> getSnapshot() {
    List<ManageableMarketDataSnapshot> result = new ArrayList<ManageableMarketDataSnapshot>();
    if (getDocuments() != null) {
      for (MarketDataSnapshotDocument doc : getDocuments()) {
        result.add(doc.getSnapshot());
      }
    }
    return result;
  }

  /**
   * Gets the first snapshot, or null if no documents.
   * 
   * @return the first snapshot, null if none
   */
  public ManageableMarketDataSnapshot getFirstSnapshot() {
    return getDocuments().size() > 0 ? getDocuments().get(0).getSnapshot() : null;
  }

  /**
   * Gets the single result expected from a query.
   * <p>
   * This throws an exception if more than 1 result is actually available.
   * Thus, this method implies an assumption about uniqueness of the queried snapshot.
   * 
   * @return the matching snapshot, not null
   * @throws IllegalStateException if no holiday was found
   */
  public ManageableMarketDataSnapshot getSingleSnapshot() {
    if (getDocuments().size() != 1) {
      throw new OpenGammaRuntimeException("Expecting zero or single resulting match, and was " + getDocuments().size());
    } else {
      return getDocuments().get(0).getSnapshot();
    }
  }

}
