/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.integration.marketdata.manipulator.dsl;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;
import org.threeten.bp.LocalDate;

import com.opengamma.engine.marketdata.spec.FixedHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LatestHistoricalMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.LiveMarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecification;
import com.opengamma.engine.marketdata.spec.MarketDataSpecificationParser;
import com.opengamma.engine.marketdata.spec.UserMarketDataSpecification;
import com.opengamma.id.UniqueId;
import com.opengamma.util.test.TestGroup;

@Test(groups = TestGroup.UNIT)
public class MarketDataSpecificationParserTest {

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void unknownType() {
    MarketDataSpecificationParser.parse("foo:bar");
  }

  @Test
  public void liveValid() {
    MarketDataSpecification spec = MarketDataSpecificationParser.parse("live:sourceName");
    assertEquals(LiveMarketDataSpecification.of("sourceName"), spec);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void liveMissing1() {
    MarketDataSpecificationParser.parse("live");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void liveMissing2() {
    MarketDataSpecificationParser.parse("live:");
  }

  @Test
  public void snapshotValid() {
    MarketDataSpecification spec = MarketDataSpecificationParser.parse("snapshot:scheme~value~version");
    assertEquals(UserMarketDataSpecification.of(UniqueId.of("scheme", "value", "version")), spec);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void snapshotMissing1() {
    MarketDataSpecificationParser.parse("snapshot");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void snapshotMissing2() {
    MarketDataSpecificationParser.parse("snapshot:");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void snapshotInvalidId() {
    MarketDataSpecificationParser.parse("snapshot:invalidIdFormat");
  }

  @Test
  public void fixedHistoricalValid() {
    MarketDataSpecification specNoRating = MarketDataSpecificationParser.parse("fixedhistorical:2011-03-08");
    LocalDate date = LocalDate.of(2011, 3, 8);
    assertEquals(new FixedHistoricalMarketDataSpecification(date), specNoRating);
    MarketDataSpecification specWithRating =
        MarketDataSpecificationParser.parse("fixedhistorical:2011-03-08,RATING_NAME");
    assertEquals(new FixedHistoricalMarketDataSpecification("RATING_NAME", date), specWithRating);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void fixedHistoricalMissing1() {
    MarketDataSpecificationParser.parse("fixedhistorical");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void fixedHistoricalMissing2() {
    MarketDataSpecificationParser.parse("fixedhistorical:");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void fixedHistoricalInvalidDate1() {
    MarketDataSpecificationParser.parse("fixedhistorical:123456");
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void fixedHistoricalInvalidDate2() {
    MarketDataSpecificationParser.parse("fixedhistorical:123456,RATING_NAME");
  }

  @Test
  public void latestHistoricalValid() {
    MarketDataSpecification specNoRating = MarketDataSpecificationParser.parse("latesthistorical");
    assertEquals(new LatestHistoricalMarketDataSpecification(), specNoRating);
    MarketDataSpecification specWithRating = MarketDataSpecificationParser.parse("latesthistorical:RATING_NAME");
    assertEquals(new LatestHistoricalMarketDataSpecification("RATING_NAME"), specWithRating);
  }

  @Test(expectedExceptions = IllegalArgumentException.class)
  public void latestHistoricalMissing() {
    MarketDataSpecificationParser.parse("latesthistorical:");
  }
}
