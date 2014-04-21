package com.opengamma.financial.analytics.volatility.surface;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.volatility.surface.BloombergFXOptionVolatilitySurfaceInstrumentProvider.FXVolQuoteType;
import com.opengamma.financial.fudgemsg.FinancialTestBase;
import com.opengamma.id.ExternalId;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;
import com.opengamma.util.tuple.Pairs;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class ICAPFXOptionVolatilitySurfaceInstrumentProviderTest extends FinancialTestBase {

  @Test
  public void testFXOptionVolatilitySurfaceInstrumentProvider() {
    final ICAPFXOptionVolatilitySurfaceInstrumentProvider provider = new ICAPFXOptionVolatilitySurfaceInstrumentProvider("I", "EURUSD", "Market_Value");
    assertEquals(provider, cycleObject(ICAPFXOptionVolatilitySurfaceInstrumentProvider.class, provider));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSD_1WK"), provider.getInstrument(Tenor.ofDays(7), Pairs.of((Number)0, FXVolQuoteType.ATM)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSD_1M"), provider.getInstrument(Tenor.ofMonths(1), Pairs.of((Number)0, FXVolQuoteType.ATM)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSD_1YR"), provider.getInstrument(Tenor.ofYears(1), Pairs.of((Number)0, FXVolQuoteType.ATM)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSD_1WK"), provider.getInstrument(Tenor.ofDays(7), Pairs.of((Number)0, FXVolQuoteType.ATM)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSD_1M"), provider.getInstrument(Tenor.ofMonths(1), Pairs.of((Number)0, FXVolQuoteType.ATM)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSD_1YR"), provider.getInstrument(Tenor.ofYears(1), Pairs.of((Number)0, FXVolQuoteType.ATM)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDBF25_1WK"), provider.getInstrument(Tenor.ofDays(7), Pairs.of((Number)25, FXVolQuoteType.BUTTERFLY)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDBF25_1M"), provider.getInstrument(Tenor.ofMonths(1), Pairs.of((Number)25, FXVolQuoteType.BUTTERFLY)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDBF25_1YR"), provider.getInstrument(Tenor.ofYears(1), Pairs.of((Number)25, FXVolQuoteType.BUTTERFLY)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDBF10_1WK"), provider.getInstrument(Tenor.ofDays(7), Pairs.of((Number)10, FXVolQuoteType.BUTTERFLY)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDBF10_1M"), provider.getInstrument(Tenor.ofMonths(1), Pairs.of((Number)10, FXVolQuoteType.BUTTERFLY)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDBF10_1YR"), provider.getInstrument(Tenor.ofYears(1), Pairs.of((Number)10, FXVolQuoteType.BUTTERFLY)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDRR25_1WK"), provider.getInstrument(Tenor.ofDays(7), Pairs.of((Number)25, FXVolQuoteType.RISK_REVERSAL)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDRR25_1M"), provider.getInstrument(Tenor.ofMonths(1), Pairs.of((Number)25, FXVolQuoteType.RISK_REVERSAL)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDRR25_1YR"), provider.getInstrument(Tenor.ofYears(1), Pairs.of((Number)25, FXVolQuoteType.RISK_REVERSAL)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDRR10_1WK"), provider.getInstrument(Tenor.ofDays(7), Pairs.of((Number)10, FXVolQuoteType.RISK_REVERSAL)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDRR10_1M"), provider.getInstrument(Tenor.ofMonths(1), Pairs.of((Number)10, FXVolQuoteType.RISK_REVERSAL)));
    assertEquals(ExternalId.of(ExternalSchemes.ICAP, "IEURUSDRR10_1YR"), provider.getInstrument(Tenor.ofYears(1), Pairs.of((Number)10, FXVolQuoteType.RISK_REVERSAL)));
  }

}
