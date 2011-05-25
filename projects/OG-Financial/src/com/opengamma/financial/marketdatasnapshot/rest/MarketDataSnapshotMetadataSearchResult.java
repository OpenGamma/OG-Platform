/**
 * Copyright (C) 2009 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.marketdatasnapshot.rest;

import java.util.Collection;

import org.joda.beans.BeanDefinition;

import com.opengamma.master.AbstractSearchResult;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotSearchRequest;
import com.opengamma.util.PublicSPI;

/**
 * Result from searching for snapshot metadata.
 * <p>
 * The returned documents will match the search criteria.
 * See {@link MarketDataSnapshotSearchRequest} for more details.
 */
@PublicSPI
@BeanDefinition
public class MarketDataSnapshotMetadataSearchResult extends AbstractSearchResult<MarketDataSnapshotMetadataDocument> {



  /**
   * Creates an instance.
   */
  public MarketDataSnapshotMetadataSearchResult() {
  }

  /**
   * Creates an instance from a collection of documents.
   * 
   * @param coll  the collection of documents to add, not null
   */
  public MarketDataSnapshotMetadataSearchResult(Collection<MarketDataSnapshotMetadataDocument> coll) {
    super(coll);
  }
}
