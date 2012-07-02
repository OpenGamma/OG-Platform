/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import java.util.EnumSet;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.opengamma.DataNotFoundException;
import com.opengamma.engine.marketdata.NamedMarketDataSpecificationRepository;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.view.execution.ExecutionFlags;
import com.opengamma.engine.view.execution.ExecutionOptions;
import com.opengamma.engine.view.execution.ViewExecutionFlags;
import com.opengamma.engine.view.execution.ViewExecutionOptions;
import com.opengamma.id.UniqueId;
import com.opengamma.id.VersionCorrection;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotDocument;
import com.opengamma.master.marketdatasnapshot.MarketDataSnapshotMaster;
import com.opengamma.util.ArgumentChecker;

/**
 *
 */
public class ViewRequest {

  private final UniqueId _viewDefinitionId;
  private final MarketData _marketData;
  private final List<String> _aggregators;

  public ViewRequest(UniqueId viewDefinitionId, List<String> aggregators, MarketData marketData) {
    ArgumentChecker.notNull(viewDefinitionId, "viewDefinitionId");
    ArgumentChecker.notNull(marketData, "marketDataType");
    _viewDefinitionId = viewDefinitionId;
    _aggregators = ImmutableList.copyOf(aggregators);
    _marketData = marketData;
  }

  public UniqueId getViewDefinitionId() {
    return _viewDefinitionId;
  }

  public List<String> getAggregators() {
    return _aggregators;
  }

  public MarketData getMarketData() {
    return _marketData;
  }

  @Override
  public String toString() {
    return "ViewRequest [" +
        "_viewDefinitionId=" + _viewDefinitionId +
        ", _marketData=" + _marketData +
        ", _aggregators=" + _aggregators +
        "]";
  }

  /* package */ public interface MarketData {

    ViewExecutionOptions createExecutionOptions(MarketDataSnapshotMaster snapshotMaster,
                                                NamedMarketDataSpecificationRepository namedMarketDataSpecRepo);
  }

  public static class Snapshot implements MarketData {

    private final UniqueId _snapshotId;
    private final VersionCorrection _versionCorrection;

    public Snapshot(UniqueId snapshotId, VersionCorrection versionCorrection) {
      ArgumentChecker.notNull(snapshotId, "snapshotId");
      ArgumentChecker.notNull(versionCorrection, "versionCorrection");
      _snapshotId = snapshotId;
      _versionCorrection = versionCorrection;
      if (snapshotId.isVersioned() && versionCorrection != VersionCorrection.LATEST) {
        throw new IllegalArgumentException("Cannot specify both a versioned snapshot and a custom verion-correction");
      }
    }

    @Override
    public ViewExecutionOptions createExecutionOptions(MarketDataSnapshotMaster snapshotMaster,
                                                       NamedMarketDataSpecificationRepository namedMarketDataSpecRepo) {
      // TODO double-check this logic, compare with LiveResultsService.processChangeViewRequest
      VersionCorrection actualVersionCorrection;
      UniqueId actualSnapshotId;
      if (_snapshotId.isVersioned()) {
        // If the version-correction is to be based on a snapshot then use the time at which the snapshot was created
        MarketDataSnapshotDocument snapshotDoc = snapshotMaster.get(_snapshotId.getObjectId(), _versionCorrection);
        actualVersionCorrection = VersionCorrection.ofVersionAsOf(snapshotDoc.getVersionFromInstant());
        actualSnapshotId = _snapshotId;
      } else {
        try {
          MarketDataSnapshotDocument snapshotDoc = snapshotMaster.get(_snapshotId.getObjectId(), _versionCorrection);
          actualSnapshotId = snapshotDoc.getUniqueId();
        } catch (DataNotFoundException e) {
          throw new DataNotFoundException("Snapshot " + _snapshotId.getObjectId() +
                                              " not found for version-correction " + _versionCorrection, e);
        }
        actualVersionCorrection = _versionCorrection;
      }
      MarketDataSpecification marketDataSpec = com.opengamma.engine.marketdata.spec.MarketData.user(actualSnapshotId);
      EnumSet<ViewExecutionFlags> flags = ExecutionFlags.none().triggerOnMarketData().get();
      return ExecutionOptions.infinite(marketDataSpec, flags, actualVersionCorrection);
    }

    @Override
    public String toString() {
      return "ViewRequest.Snapshot [" +
          "_snapshotId=" + _snapshotId +
          ", _versionCorrection=" + _versionCorrection +
          "]";
    }
  }

  public static class Live implements MarketData {

    private final String _dataProvider;

    public Live(String dataProvider) {
      ArgumentChecker.notNull(dataProvider, "dataProvider");
      _dataProvider = dataProvider;
    }

    @Override
    public ViewExecutionOptions createExecutionOptions(MarketDataSnapshotMaster snapshotMaster,
                                                       NamedMarketDataSpecificationRepository namedMarketDataSpecRepo) {
      MarketDataSpecification marketDataSpec = namedMarketDataSpecRepo.getSpecification(_dataProvider);
      EnumSet<ViewExecutionFlags> flags = ExecutionFlags.triggersEnabled().get();
      return ExecutionOptions.infinite(marketDataSpec, flags, VersionCorrection.LATEST);
    }

    @Override
    public String toString() {
      return "ViewRequest.Live [_dataProvider='" + _dataProvider + '\'' + "]";
    }
  }
}
