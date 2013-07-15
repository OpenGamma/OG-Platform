/**
 * Copyright (C) 2011 - present by OpenGamma Inc. and the OpenGamma group of companies
 * 
 * Please see distribution for license.
 */
package com.opengamma.financial.analytics.ircurve;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertFalse;

import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveDefinitionAndSpecificationsTest {

  @Test
  public void testBBG3MFRAHProvider() {
    final Map<Tenor, CurveInstrumentProvider> result = CurveDefinitionAndSpecifications.buildStandardBloomberg3MFRAInstrumentProvider("US");
    assertEquals(result.size(), 22);
    assertEquals(result.get(Tenor.THREE_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USFR00C Curncy")); // 0M x 3M
    assertEquals(result.get(Tenor.EIGHT_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USFR0EH Curncy")); // 5M x 8M
    assertEquals(result.get(Tenor.ofMonths(15)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USFR011C Curncy")); // 12M x 15M
    assertEquals(result.get(Tenor.ofMonths(24)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USFR1I2 Curncy")); // 21M x 24M
    assertFalse(result.containsKey(Tenor.ofYears(1)));
  }

  @Test
  public void testBBG3MSwapProvider() {
    Map<Tenor, CurveInstrumentProvider> result = CurveDefinitionAndSpecifications.buildStandardBloomberg3MSwapInstrumentProvider("EU", "V3");
    assertEquals(result.size(), 67);
    assertEquals(result.get(Tenor.THREE_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("EUSWCV3 Curncy")); // 3M
    assertEquals(result.get(Tenor.TEN_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("EUSWJV3 Curncy")); // 10M
    assertEquals(result.get(Tenor.ofYears(1)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("EUSW1V3 Curncy")); // 1Y
    assertEquals(result.get(Tenor.ofYears(2)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("EUSW2V3 Curncy")); // 2Y
    assertEquals(result.get(Tenor.ofMonths(33)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("EUSW2IV3 Curncy")); // 33M
    assertEquals(result.get(Tenor.ofYears(15)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("EUSW15V3 Curncy")); // 15Y
    assertFalse(result.containsKey(Tenor.ofMonths(12)));
    result = CurveDefinitionAndSpecifications.buildStandardBloomberg3MSwapInstrumentProvider("US", "");
    assertEquals(result.size(), 67);
    assertEquals(result.get(Tenor.THREE_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSWC Curncy")); // 3M
    assertEquals(result.get(Tenor.TEN_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSWJ Curncy")); // 10M
    assertEquals(result.get(Tenor.ofYears(1)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSW1 Curncy")); // 1Y
    assertEquals(result.get(Tenor.ofYears(2)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSW2 Curncy")); // 2Y
    assertEquals(result.get(Tenor.ofMonths(33)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSW2I Curncy")); // 33M
    assertEquals(result.get(Tenor.ofYears(15)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSW15 Curncy")); // 15Y
    assertFalse(result.containsKey(Tenor.ofMonths(12)));
  }

  @Test
  public void testBBG6MFRAProvider() {
    final Map<Tenor, CurveInstrumentProvider> result = CurveDefinitionAndSpecifications.buildStandardBloomberg6MFRAInstrumentProvider("US");
    assertEquals(result.size(), 22);
    assertEquals(result.get(Tenor.SIX_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USFR00F Curncy")); // 0M x 6M
    assertEquals(result.get(Tenor.ELEVEN_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USFR0EK Curncy")); // 5M x 11M
    assertEquals(result.get(Tenor.ofMonths(18)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USFR011F Curncy")); // 12M x 18M
    assertEquals(result.get(Tenor.ofMonths(21)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USFR1C1I Curncy")); // 15M x 21M
    assertFalse(result.containsKey(Tenor.ofYears(1)));
  }

  @Test
  public void testBBGDepositRateProvider() {
    final Map<Tenor, CurveInstrumentProvider> result = CurveDefinitionAndSpecifications.buildStandardBloombergDepositInstrumentProvider("US");
    assertEquals(result.size(), 67);
    assertEquals(result.get(Tenor.ofDays(1)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USDR1T Curncy")); // O/N
    assertEquals(result.get(Tenor.ofDays(7)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USDR1Z Curncy")); // 1W
    assertEquals(result.get(Tenor.SEVEN_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USDRG Curncy")); // 7M
    assertEquals(result.get(Tenor.FIVE_YEARS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USDR5 Curncy")); // 5Y
  }

  @Test
  public void testBBGEuriborProvider() {
    final Map<Tenor, CurveInstrumentProvider> result = CurveDefinitionAndSpecifications.buildStandardBloombergEuriborInstrumentProvider();
    assertEquals(result.size(), 12);
    assertEquals(result.get(Tenor.ofDays(14)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("EUR002W Index")); // 2W
    assertEquals(result.get(Tenor.EIGHT_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("EUR008M Index")); // 8M
  }

  @Test
  public void testBBGJPY6MFRAProvider() {
    final Map<Tenor, CurveInstrumentProvider> result = CurveDefinitionAndSpecifications.buildStandardBloombergJPY6MFRAInstrumentProvider();
    assertEquals(result.size(), 22);
    assertEquals(result.get(Tenor.ofMonths(12)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("JYFR6/12 Curncy")); // 6M x 12M
    assertEquals(result.get(Tenor.ofMonths(15)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("JYFR9/15 Curncy")); // 9M x 15M
  }

  @Test
  public void testBBGLiborProvider() {
    final Map<Tenor, CurveInstrumentProvider> result = CurveDefinitionAndSpecifications.buildStandardBloombergLiborInstrumentProvider("BP", "S/N", "T/N");
    assertEquals(result.size(), 17);
    assertEquals(result.get(Tenor.ofDays(1)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("BP00S/N Index")); // O/N
    assertEquals(result.get(Tenor.ofDays(21)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("BP0003W Index")); // 3W
    assertEquals(result.get(Tenor.ofMonths(4)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("BP0004M Index")); // 4M
    assertEquals(result.get(Tenor.ofMonths(11)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("BP0011M Index")); // 11M
  }

  @Test
  public void testBBGOISProvider() {
    final Map<Tenor, CurveInstrumentProvider> result = CurveDefinitionAndSpecifications.buildStandardBloombergOISSwapInstrumentProvider("USSO");
    assertEquals(result.size(), 70);
    assertEquals(result.get(Tenor.ofDays(21)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSO3Z Curncy")); // 3W
    assertEquals(result.get(Tenor.THREE_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSOC Curncy")); // 3M
    assertEquals(result.get(Tenor.ofYears(1)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSO1 Curncy")); // 1Y
    assertEquals(result.get(Tenor.ofMonths(33)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSO2I Curncy")); // 33M
    assertEquals(result.get(Tenor.ofYears(6)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("USSO6 Curncy")); // 6Y
  }

  @Test
  public void testBBG6MSwapProvider() {
    final Map<Tenor, CurveInstrumentProvider> result = CurveDefinitionAndSpecifications.buildStandardBloomberg6MSwapInstrumentProvider("BPSW");
    assertEquals(result.size(), 83);
    assertEquals(result.get(Tenor.TWO_MONTHS).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("BPSWB Curncy")); // 2M
    assertEquals(result.get(Tenor.ONE_YEAR).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("BPSW1 Curncy")); // 1Y
    assertEquals(result.get(Tenor.ofMonths(57)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("BPSW4I Curncy")); // 57M
    assertEquals(result.get(Tenor.ofYears(35)).getInstrument(null, null), ExternalSchemes.bloombergTickerSecurityId("BPSW35 Curncy")); // 50Y
  }
}
