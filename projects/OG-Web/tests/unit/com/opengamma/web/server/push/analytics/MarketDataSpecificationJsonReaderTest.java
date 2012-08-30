/**
 * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.web.server.push.analytics;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import javax.time.calendar.LocalDate;

import org.testng.annotations.Test;

import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.id.UniqueId;

public class MarketDataSpecificationJsonReaderTest {

  @Test
  public void live() {
    String json = "{\"marketDataType\": \"live\", \"source\": \"Bloomberg\"}";
    MarketDataSpecification spec = MarketDataSpecificationJsonReader.buildSpecification(json);
    assertTrue(spec instanceof LiveMarketDataSpecification);
    LiveMarketDataSpecification liveSpec = (LiveMarketDataSpecification) spec;
    assertEquals("Bloomberg", liveSpec.getDataSource());
  }

  @Test
  public void fixedHistorical() {
    String json = "{\"marketDataType\": \"fixedHistorical\", \"resolverKey\": \"rk\", \"fieldResolverKey\": \"frk\", " +
        "\"date\": \"2012-08-30\"}";
    MarketDataSpecification spec = MarketDataSpecificationJsonReader.buildSpecification(json);
    assertTrue(spec instanceof FixedHistoricalMarketDataSpecification);
    assertEquals(new FixedHistoricalMarketDataSpecification("rk", "frk", LocalDate.of(2012, 8, 30)), spec);
  }

  @Test
  public void latestHistorical() {
    String json = "{\"marketDataType\": \"fixedHistorical\", \"resolverKey\": \"rk\", \"fieldResolverKey\": \"frk\"}";
    MarketDataSpecification spec = MarketDataSpecificationJsonReader.buildSpecification(json);
    assertTrue(spec instanceof LatestHistoricalMarketDataSpecification);
    LatestHistoricalMarketDataSpecification latestSpec = (LatestHistoricalMarketDataSpecification) spec;
    assertEquals("rk", latestSpec.getTimeSeriesResolverKey());
    assertEquals("frk", latestSpec.getTimeSeriesFieldResolverKey());
  }

  @Test
  public void snapshot() {
    String json = "{\"marketDataType\": \"snapshot\", \"snapshotId\": \"\"}";
    MarketDataSpecification spec = MarketDataSpecificationJsonReader.buildSpecification(json);
    assertTrue(spec instanceof UserMarketDataSpecification);
    UserMarketDataSpecification userSpec = (UserMarketDataSpecification) spec;
    assertEquals(UniqueId.of("", ""), userSpec.getUserSnapshotId());
  }
}
