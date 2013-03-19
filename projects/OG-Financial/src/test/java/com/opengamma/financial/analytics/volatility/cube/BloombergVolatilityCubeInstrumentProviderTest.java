/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.volatility.cube;

import java.util.Set;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;
import com.opengamma.util.test.TestGroup;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class BloombergVolatilityCubeInstrumentProviderTest {

  @Test
  public void canGetBloomberg() {
    AssertJUnit.assertNotNull(BloombergSwaptionVolatilityCubeInstrumentProvider.BLOOMBERG);
  }

  @Test
  public void canGetUSDInfo() {
    final Currency usd = Currency.USD;
    final BloombergSwaptionVolatilityCubeInstrumentProvider instrumentProvider = BloombergSwaptionVolatilityCubeInstrumentProvider.BLOOMBERG;

    final Set<VolatilityPoint> allPoints = instrumentProvider.getAllPoints(usd);
    Assert.assertNotSame(0, allPoints.size());
    for (final VolatilityPoint volatilityPoint : allPoints) {
      final Set<ExternalId> instruments = instrumentProvider.getInstruments(usd, volatilityPoint);
      Assert.assertNotNull(instruments);
      Assert.assertNotSame(0, instruments.size());

      final ExternalId strikeInstrument = instrumentProvider.getStrikeInstrument(usd, volatilityPoint.getSwapTenor(), volatilityPoint.getOptionExpiry());
      Assert.assertNotNull(strikeInstrument);
    }

  }
}
