package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class TullettPrebonFXOptionVolatilitySurfaceInstrumentProviderTest {

  public void testFXOptionVolatilitySurfaceInstrumentProvider() {
    TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider provider = new TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider("FV", "USDZAR", "Market_Value");
    assertEquals(ExternalId.of(ExternalSchemes.SURF, "FV2DBUSDZAR02M"), provider.getInstrument(Tenor.ofMonths(2), Pairs.of((Number)25, FXVolQuoteType.BUTTERFLY)));
    provider = new TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider("FV", "USDTRY", "Market_Value");
    assertEquals(ExternalId.of(ExternalSchemes.SURF, "FV1DBUSDTRY24M"), provider.getInstrument(Tenor.ofYears(2), Pairs.of((Number)10, FXVolQuoteType.BUTTERFLY)));
    assertEquals(ExternalId.of(ExternalSchemes.SURF, "FV1DBUSDTRY24M"), provider.getInstrument(Tenor.ofMonths(24), Pairs.of((Number)10, FXVolQuoteType.BUTTERFLY)));
    provider = new TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider("FV", "EURRUB", "Market_Value");
    assertEquals(ExternalId.of(ExternalSchemes.SURF, "FV1DREURRUB12M"), provider.getInstrument(Tenor.ofYears(1), Pairs.of((Number)10, FXVolQuoteType.RISK_REVERSAL)));
    assertEquals(ExternalId.of(ExternalSchemes.SURF, "FV1DREURRUB12M"), provider.getInstrument(Tenor.ofMonths(12), Pairs.of((Number)10, FXVolQuoteType.RISK_REVERSAL)));
    provider = new TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider("FV", "USDPLN", "Market_Value");
    assertEquals(ExternalId.of(ExternalSchemes.SURF, "FV2DRUSDPLN05Y"), provider.getInstrument(Tenor.ofYears(5), Pairs.of((Number)25, FXVolQuoteType.RISK_REVERSAL)));
    provider = new TullettPrebonFXOptionVolatilitySurfaceInstrumentProvider("FV", "USDILS", "Market_Value");
    assertEquals(ExternalId.of(ExternalSchemes.SURF, "FVAFVUSDILS03Y"), provider.getInstrument(Tenor.ofYears(3), Pairs.of((Number)0, FXVolQuoteType.ATM)));
  }

}
