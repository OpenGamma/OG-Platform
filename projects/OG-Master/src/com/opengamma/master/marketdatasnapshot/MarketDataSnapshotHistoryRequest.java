/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.master.marketdatasnapshot;

import javax.time.InstantProvider;

import org.joda.beans.BeanDefinition;

import com.opengamma.id.ObjectIdentifiable;
import com.opengamma.master.AbstractHistoryRequest;
import com.opengamma.util.PublicSPI;

/**
 * Request for the history of a market data snapshot.
 * <p>
 * A full snapshot master implements historical storage of data.
 * History can be stored in two dimensions and this request provides searching.
 * <p>
 * The first historic dimension is the classic series of versions.
 * Each new version is stored in such a manor that previous versions can be accessed.
 * <p>
 * The second historic dimension is corrections.
 * A correction occurs when it is realized that the original data stored was incorrect.
 * A simple master might simply replace the original version with the corrected value.
 * A full implementation will store the correction in such a manner that it is still possible
 * to obtain the value before the correction was made.
 * <p>
 * For example, a snapshot added on Monday and updated on Thursday has two versions.
 * If it is realized on Friday that the version stored on Monday was incorrect, then a
 * correction may be applied. There are now two versions, the first of which has one correction.
 * This may continue, with multiple corrections allowed for each version.
 * <p>
 * Versions and corrections are represented by instants in the search.
 */
@PublicSPI
@BeanDefinition
public class MarketDataSnapshotHistoryRequest extends AbstractHistoryRequest {

  /**
   * Creates an instance.
   * The object identifier must be added before searching.
   */
  public MarketDataSnapshotHistoryRequest() {
    super();
  }

  /**
   * Creates an instance with object identifier.
   * This will retrieve all versions and corrections unless the relevant fields are set.
   * 
   * @param objectId  the object identifier, not null
   */
  public MarketDataSnapshotHistoryRequest(final ObjectIdentifiable objectId) {
    super(objectId);
  }

  /**
   * Creates an instance with object identifier and optional version and correction.
   * 
   * @param objectId  the object identifier, not null
   * @param versionInstantProvider  the version instant to retrieve, null for all versions
   * @param correctedToInstantProvider  the instant that the data should be corrected to, null for all corrections
   */
  public MarketDataSnapshotHistoryRequest(final ObjectIdentifiable objectId, InstantProvider versionInstantProvider, InstantProvider correctedToInstantProvider) {
    super(objectId, versionInstantProvider, correctedToInstantProvider);
  }

}
