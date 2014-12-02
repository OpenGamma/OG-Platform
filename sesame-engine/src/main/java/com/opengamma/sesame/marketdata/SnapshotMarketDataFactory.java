/**
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.sesame.marketdata;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.opengamma.core.marketdatasnapshot.CurveKey;
import com.opengamma.core.marketdatasnapshot.CurveSnapshot;
import com.opengamma.core.marketdatasnapshot.MarketDataSnapshotSource;
import com.opengamma.core.marketdatasnapshot.NamedSnapshot;
import com.opengamma.core.marketdatasnapshot.StructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.UnstructuredMarketDataSnapshot;
import com.opengamma.core.marketdatasnapshot.ValueSnapshot;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.id.ExternalId;
import com.opengamma.id.ExternalIdBundle;
import com.opengamma.util.ArgumentChecker;

/**
 * Market data factory that creates a data source backed by a {@link StructuredMarketDataSnapshot}.
 * <p>
 * This acts an an adaptor between the old and new market data systems. This class is likely to be redundant
 * after everything has migrated to use {@link MarketDataEnvironment}.
 */
public class SnapshotMarketDataFactory
    implements MarketDataFactory<UserMarketDataSpecification> {

  private final MarketDataSnapshotSource _snapshotSource;

  /**
   * @param snapshotSource for loading snapshots
   */
  public SnapshotMarketDataFactory(MarketDataSnapshotSource snapshotSource) {
    _snapshotSource = ArgumentChecker.notNull(snapshotSource, "snapshotSource");
  }

  @Override
  public Class<UserMarketDataSpecification> getSpecificationType() {
    return UserMarketDataSpecification.class;
  }

  @Override
  public MarketDataSource create(UserMarketDataSpecification spec) {
    NamedSnapshot snapshot = _snapshotSource.get(spec.getUserSnapshotId());
    return new DataSource(flattenSnapshot((StructuredMarketDataSnapshot) snapshot));
  }

  /**
   * Flattens a snapshot into a map of raw values. Handles raw data and curves, not surfaces or cubes.
   *
   * @param snapshot a snapshot
   * @return the snapshot's raw data and curve data flattened into a map
   */
  private static Map<MarketDataRequest, Object> flattenSnapshot(StructuredMarketDataSnapshot snapshot) {
    Map<MarketDataRequest, Object> marketData = new HashMap<>();
    marketData.putAll(extractSnapshotData(snapshot.getGlobalValues()));

    for (Map.Entry<CurveKey, CurveSnapshot> curveEntry : snapshot.getCurves().entrySet()) {
      UnstructuredMarketDataSnapshot curveValues = curveEntry.getValue().getValues();
      marketData.putAll(extractSnapshotData(curveValues));
    }
    return marketData;
  }

  private static Map<MarketDataRequest, Object> extractSnapshotData(UnstructuredMarketDataSnapshot snapshot) {
    Map<MarketDataRequest, Object> marketData = new HashMap<>();

    for (ExternalIdBundle idBundle : snapshot.getTargets()) {
      Map<String, ValueSnapshot> targetValues = snapshot.getTargetValues(idBundle);

      for (Map.Entry<String, ValueSnapshot> targetValue : targetValues.entrySet()) {
        FieldName fieldName = FieldName.of(targetValue.getKey());
        ValueSnapshot value = targetValue.getValue();

        // key by individual IDs, not bundles
        for (ExternalId id : idBundle) {
          MarketDataRequest request = MarketDataRequest.of(id.toBundle(), fieldName);
          marketData.put(request, value.getMarketValue());
        }
      }
    }
    return marketData;
  }

  private static class DataSource implements MarketDataSource {

    private final Map<MarketDataRequest, Object> _marketData;

    private DataSource(Map<MarketDataRequest, Object> marketData) {
      _marketData = marketData;
    }


    @Override
    public MarketDataResults get(Set<MarketDataRequest> requests) {
      MarketDataResults.Builder builder = MarketDataResults.builder();

      for (MarketDataRequest request : requests) {
        for (ExternalId id : request.getId()) {
          MarketDataRequest singleIdRequest = MarketDataRequest.of(id.toBundle(), request.getFieldName());
          Object value = _marketData.get(singleIdRequest);

          if (value != null) {
            builder.add(request, value);
            break;
          } else {
            builder.missing(request);
          }
        }
      }
      return builder.build();
    }
  }
}
