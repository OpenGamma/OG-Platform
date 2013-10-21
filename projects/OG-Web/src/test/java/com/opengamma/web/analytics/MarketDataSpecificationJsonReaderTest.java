/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.analytics;

import static org.testng.AssertJUnit.assertEquals;

import java.util.List;

import org.json.JSONException;
import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.google.common.collect.ImmutableList;
import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class MarketDataSpecificationJsonReaderTest {

  private static final String LIVE_JSON = "{\"marketDataType\": \"live\", \"source\": \"Bloomberg\"}";
  private static final String FIXED_JSON = "{\"marketDataType\": \"fixedHistorical\", \"resolverKey\": \"rk\", \"date\": \"2012-08-30\"}";
  private static final String LATEST_JSON = "{\"marketDataType\": \"latestHistorical\", \"resolverKey\": \"rk\"}";
  private static final String SNAPSHOT_JSON = "{\"marketDataType\": \"snapshot\", \"snapshotId\": \"scheme~value\"}";

  private static final LiveMarketDataSpecification LIVE = LiveMarketDataSpecification.of("Bloomberg");
  private static final FixedHistoricalMarketDataSpecification FIXED = new FixedHistoricalMarketDataSpecification("rk", LocalDate.of(2012, 8, 30));
  private static final LatestHistoricalMarketDataSpecification LATEST = new LatestHistoricalMarketDataSpecification("rk");
  private static final UserMarketDataSpecification SNAPSHOT = UserMarketDataSpecification.of(UniqueId.of("scheme", "value"));

  @Test
  public void live() throws JSONException {
    assertEquals(LIVE, MarketDataSpecificationJsonReader.buildSpecification(LIVE_JSON));
  }

  @Test
  public void fixedHistorical() throws JSONException {
    assertEquals(FIXED, MarketDataSpecificationJsonReader.buildSpecification(FIXED_JSON));
  }

  @Test
  public void latestHistorical() throws JSONException {
    assertEquals(LATEST, MarketDataSpecificationJsonReader.buildSpecification(LATEST_JSON));
  }

  @Test
  public void snapshot() throws JSONException {
    assertEquals(SNAPSHOT, MarketDataSpecificationJsonReader.buildSpecification(SNAPSHOT_JSON));
  }

  @Test
  public void multiple() throws JSONException {
    String json = "[" + LIVE_JSON + ", " + LATEST_JSON + ", " + FIXED_JSON + ", " + SNAPSHOT_JSON + "]";
    List<MarketDataSpecification> specs = MarketDataSpecificationJsonReader.buildSpecifications(json);
    assertEquals(specs, ImmutableList.of(LIVE, LATEST, FIXED, SNAPSHOT));
  }
}
