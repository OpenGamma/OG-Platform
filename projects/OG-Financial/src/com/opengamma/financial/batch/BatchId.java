/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.batch;

import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.util.ArgumentChecker;

import javax.time.Instant;


/**
 * An id for a batch in the batch database.
 * <p>
 * This class is immutable and thread-safe.
 */
public final class BatchId {

  /**
   * The unique id of the market data snapshot used by the batch
   */
  final private UniqueId _marketDataSnapshotUid;
  /**
   * The unique id of the view definition used by the batch
   */
  final private UniqueId _viewDefinitionUid;
  /**
   * The version correction used by the batch
   */
  final private VersionCorrection _versionCorrection;
  /**
   * The valuation time used by the batch
   */
  final Instant _valuationTime;

  /**
   * Creates an instance.
   *
   * @param snapshotUid  the  unique id of the market data snapshot, not null
   * @param viewDefinitionId  the unique id of the view definition, not null
   * @param versionCorrection  the version correction, not null
   * @param valuationTime  the valuation time, not null
   */
  public BatchId(UniqueId snapshotUid, UniqueId viewDefinitionId, VersionCorrection versionCorrection, Instant valuationTime) {
    ArgumentChecker.notNull(snapshotUid, "snapshotUid");
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(versionCorrection, "versionCorrection");
    ArgumentChecker.notNull(valuationTime, "valuationTime");
    this._marketDataSnapshotUid = snapshotUid;
    this._viewDefinitionUid = viewDefinitionId;
    this._versionCorrection = versionCorrection;
    this._valuationTime = valuationTime;
  }

  public UniqueId getMarketDataSnapshotUid() {
    return _marketDataSnapshotUid;
  }

  public UniqueId getViewDefinitionUid() {
    return _viewDefinitionUid;
  }

  public VersionCorrection getVersionCorrection() {
    return _versionCorrection;
  }

  public Instant getValuationTime() {
    return _valuationTime;
  }
}
