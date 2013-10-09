/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.RandomizingMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.id.UniqueId;

/**
 * <p>Creates instances of {@link MarketDataSpecification} subclasses from JSON. The JSON format is:</p>
 * <h4>Live Data</h4>
 * <code>{"marketDataType": "live", "source": "Bloomberg"}</code>
 * <h4>Fixed Historical Data</h4>
 * <code>{"marketDataType": "fixedHistorical", "resolverKey": "TSS_CONFIG", "date": "2012-08-30"}</code>
 * <h4>Latest Historical Data</h4>
 * <code>{"marketDataType": "latestHistorical", "resolverKey": "TSS_CONFIG"}</code>
 * <h4>Snapshot Data</h4>
 * <code>{"marketDataType": "snapshot", "snapshotId": "Snap~1234"}</code>
 * <h4>Randomized Snapshot Data</h4>
 * <code>{"marketDataType": "snapshot", "snapshotId": "Snap~1234", "updateProbability": "0.2",
 * "maxPercentageChange": "5", "averageCycleInterval": "1000"}</code>
 * <p>There are REST endpoints for looking up available values for live data source names, resolver keys and
 * snapshot IDs. See the package documentation for {@link com.opengamma.web.analytics.rest}.</p>
 */
public class MarketDataSpecificationJsonReader {

  private static final String SNAPSHOT_ID = "snapshotId";
  private static final String RESOLVER_KEY = "resolverKey";
  private static final String SOURCE = "source";
  private static final String SNAPSHOT = "snapshot";
  private static final String RANDOMIZED_SNAPSHOT = "randomizedsnapshot";
  private static final String MARKET_DATA_TYPE = "marketDataType";
  private static final String LIVE = "live";
  private static final String LATEST_HISTORICAL = "latestHistorical";
  private static final String FIXED_HISTORICAL = "fixedHistorical";
  private static final String DATE = "date";
  private static final String UPDATE_PROBABILITY = "updateProbability";
  private static final String MAX_PERCENTAGE_CHANGE = "maxPercentageChange";
  private static final String AVERAGE_CYCLE_INTERVAL = "averageCycleInterval";

  /** Builders keyed by the name of the market data type. */
  private static final Map<String, SpecificationBuilder> s_builders = ImmutableMap.of(
      LIVE, new LiveSpecificationBuilder(),
      LATEST_HISTORICAL, new LatestHistoricalSpecificationBuilder(),
      FIXED_HISTORICAL, new FixedHistoricalSpecificationBuilder(),
      SNAPSHOT, new SnapshotSpecificationBuilder(),
      RANDOMIZED_SNAPSHOT, new RandomSnapshotSpecificationBuilder()
  );

  public static MarketDataSpecification buildSpecification(String json) throws JSONException {
    return buildSpecification(new JSONObject(json));
  }

  private static MarketDataSpecification buildSpecification(JSONObject json) throws JSONException {
    String marketDataType = json.getString(MARKET_DATA_TYPE);
    SpecificationBuilder builder = s_builders.get(marketDataType);
    if (builder == null) {
      throw new IllegalArgumentException("No builder found for market data type " + marketDataType);
    }
    return builder.build(json);
  }

  public static List<MarketDataSpecification> buildSpecifications(String json) {
    try {
      JSONArray array = new JSONArray(json);
      List<MarketDataSpecification> specs = Lists.newArrayListWithCapacity(array.length());
      for (int i = 0; i < array.length(); i++) {
        specs.add(buildSpecification(array.getJSONObject(i)));
      }
      return specs;
    } catch (JSONException e) {
      throw new IllegalArgumentException("Failed to parse MarketDataSpecification JSON", e);
    }
  }

  /** For classes that can build instances of {@link MarketDataSpecification} subclasses. */
  private interface SpecificationBuilder {

    MarketDataSpecification build(JSONObject json) throws JSONException;
  }

  /** Builds instances of {@link LiveMarketDataSpecification}. */
  private static class LiveSpecificationBuilder implements SpecificationBuilder {

    @Override
    public MarketDataSpecification build(JSONObject json) throws JSONException {
      return LiveMarketDataSpecification.of(json.getString(MarketDataSpecificationJsonReader.SOURCE));
    }
  }

  /** Builds instances of {@link LatestHistoricalMarketDataSpecification}. */
  private static class LatestHistoricalSpecificationBuilder implements SpecificationBuilder {

    @Override
    public MarketDataSpecification build(JSONObject json) throws JSONException {
      return new LatestHistoricalMarketDataSpecification(
          json.getString(MarketDataSpecificationJsonReader.RESOLVER_KEY));
    }
  }

  /** Builds instances of {@link FixedHistoricalMarketDataSpecification}. */
  private static class FixedHistoricalSpecificationBuilder implements SpecificationBuilder {

    @Override
    public MarketDataSpecification build(JSONObject json) throws JSONException {
      return new FixedHistoricalMarketDataSpecification(
          json.getString(MarketDataSpecificationJsonReader.RESOLVER_KEY),
          LocalDate.parse(json.getString(DATE)));
    }
  }

  /** Builds instances of {@link UserMarketDataSpecification}. */
  private static class SnapshotSpecificationBuilder implements SpecificationBuilder {

    @Override
    public MarketDataSpecification build(JSONObject json) throws JSONException {
      return UserMarketDataSpecification.of(UniqueId.parse(json.getString(MarketDataSpecificationJsonReader.SNAPSHOT_ID)));
    }
  }

  /** Builds instances of {@link UserMarketDataSpecification}. */
  private static class RandomSnapshotSpecificationBuilder implements SpecificationBuilder {

    @Override
    public MarketDataSpecification build(JSONObject json) throws JSONException {
      return RandomizingMarketDataSpecification.of(
        UserMarketDataSpecification.of(UniqueId.parse(json.getString(MarketDataSpecificationJsonReader.SNAPSHOT_ID))),
        json.getDouble(MarketDataSpecificationJsonReader.UPDATE_PROBABILITY),
        json.getInt(MarketDataSpecificationJsonReader.MAX_PERCENTAGE_CHANGE),
        json.getLong(MarketDataSpecificationJsonReader.AVERAGE_CYCLE_INTERVAL)
      );
    }
  }
}
