package com.opengamma.financial.analytics.volatility.cube;

import java.util.Set;

import org.testng.Assert;
import org.testng.AssertJUnit;
import org.testng.annotations.Test;

import com.opengamma.core.marketdatasnapshot.VolatilityPoint;
import com.opengamma.id.ExternalId;
import com.opengamma.util.money.Currency;

/**
 * 
 */
public class VolatilityCubeInstrumentProviderTest {

  @Test
  public void canGetBloomberg() {
    AssertJUnit.assertNotNull(VolatilityCubeInstrumentProvider.BLOOMBERG);
  }
  
  @Test
  public void canGetUSdInfo() {
    Currency usd = Currency.USD;
    VolatilityCubeInstrumentProvider instrumentProvider = VolatilityCubeInstrumentProvider.BLOOMBERG;
    
    Set<VolatilityPoint> allPoints = instrumentProvider.getAllPoints(usd);
    Assert.assertNotSame(0, allPoints.size());
    for (VolatilityPoint volatilityPoint : allPoints) {
      Set<ExternalId> instruments = instrumentProvider.getInstruments(usd, volatilityPoint);
      Assert.assertNotNull(instruments);
      Assert.assertNotSame(0, instruments.size());
      
      ExternalId strikeInstrument = instrumentProvider.getStrikeInstrument(usd, volatilityPoint.getSwapTenor(), volatilityPoint.getOptionExpiry());
      Assert.assertNotNull(strikeInstrument);
    }
    
  }
}
