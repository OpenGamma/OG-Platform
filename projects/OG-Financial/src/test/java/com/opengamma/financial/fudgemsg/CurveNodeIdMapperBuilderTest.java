/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.curve.CurveNodeIdMapper;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeIdMapperBuilderTest extends AnalyticsTestBase {

  /**
   * Tests the round trip through fudge.
   */
  @Test
  public void test() {
    final String name = "Mapper";
    final Map<Tenor, CurveInstrumentProvider> billIds = new HashMap<>();
    billIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.cusipSecurityId("SDV")));
    billIds.put(Tenor.FIVE_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.cusipSecurityId("THE")));
    final Map<Tenor, CurveInstrumentProvider> bondIds = new HashMap<>();
    bondIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.cusipSecurityId("ABCD")));
    bondIds.put(Tenor.FIVE_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.cusipSecurityId("ABCD")));
    final Map<Tenor, CurveInstrumentProvider> cashIds = new HashMap<>();
    cashIds.put(Tenor.ON, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("12")));
    cashIds.put(Tenor.TN, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("21")));
    cashIds.put(Tenor.ONE_DAY, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("123")));
    cashIds.put(Tenor.ONE_WEEK, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("1234")));
    cashIds.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("12345")));
    cashIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("123456")));
    cashIds.put(Tenor.THREE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("1234567")));
    final Map<Tenor, CurveInstrumentProvider> creditSpreadIds = new HashMap<>();
    creditSpreadIds.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("ABC")));
    creditSpreadIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("DEF")));
    creditSpreadIds.put(Tenor.THREE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("GHI")));
    creditSpreadIds.put(Tenor.FOUR_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("JKL")));
    creditSpreadIds.put(Tenor.FIVE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("MNO")));
    creditSpreadIds.put(Tenor.SIX_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("PQR")));
    creditSpreadIds.put(Tenor.SEVEN_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("STU")));
    creditSpreadIds.put(Tenor.EIGHT_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("VWX")));
    final Map<Tenor, CurveInstrumentProvider> swapIds = new HashMap<>();
    swapIds.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("q")));
    swapIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("w")));
    swapIds.put(Tenor.THREE_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("e")));
    swapIds.put(Tenor.FOUR_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("r")));
    swapIds.put(Tenor.FIVE_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("t")));
    final Map<Tenor, CurveInstrumentProvider> continuouslyCompoundedRateIds = new HashMap<>();
    continuouslyCompoundedRateIds.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("z")));
    continuouslyCompoundedRateIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("x")));
    continuouslyCompoundedRateIds.put(Tenor.THREE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("c")));
    continuouslyCompoundedRateIds.put(Tenor.FOUR_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("v")));
    final Map<Tenor, CurveInstrumentProvider> periodicallyCompoundedRateIds = new HashMap<>();
    periodicallyCompoundedRateIds.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("z")));
    periodicallyCompoundedRateIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("x")));
    periodicallyCompoundedRateIds.put(Tenor.THREE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("c")));
    periodicallyCompoundedRateIds.put(Tenor.FOUR_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("v")));
    final Map<Tenor, CurveInstrumentProvider> discountFactorIds = new HashMap<>();
    discountFactorIds.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("m")));
    discountFactorIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("n")));
    discountFactorIds.put(Tenor.FOUR_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("b")));
    final Map<Tenor, CurveInstrumentProvider> fraIds = new HashMap<>();
    fraIds.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("j")));
    fraIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("k")));
    fraIds.put(Tenor.FIVE_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("l")));
    final Map<Tenor, CurveInstrumentProvider> fxForwardIds = new HashMap<>();
    fxForwardIds.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergBuidSecurityId("FX1")));
    fxForwardIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergBuidSecurityId("FX2")));
    fxForwardIds.put(Tenor.THREE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergBuidSecurityId("FX3")));
    final Map<Tenor, CurveInstrumentProvider> immFRAIds = new HashMap<>();
    immFRAIds.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergBuidSecurityId("f1")));
    immFRAIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergBuidSecurityId("f2")));
    immFRAIds.put(Tenor.EIGHTEEN_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergBuidSecurityId("f2")));
    final Map<Tenor, CurveInstrumentProvider> immSwapIds = new HashMap<>();
    immSwapIds.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergBuidSecurityId("d1")));
    immSwapIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergBuidSecurityId("d2")));
    immSwapIds.put(Tenor.EIGHTEEN_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergBuidSecurityId("d2")));
    final Map<Tenor, CurveInstrumentProvider> rateFutureIds = new HashMap<>();
    rateFutureIds.put(Tenor.ONE_YEAR, new BloombergFutureCurveInstrumentProvider("ED", "RATE"));
    rateFutureIds.put(Tenor.TWO_YEARS, new BloombergFutureCurveInstrumentProvider("ED", "RATE"));
    rateFutureIds.put(Tenor.EIGHTEEN_MONTHS, new BloombergFutureCurveInstrumentProvider("ED", "RATE"));
    final Map<Tenor, CurveInstrumentProvider> zeroCouponInflationIds = new HashMap<>();
    zeroCouponInflationIds.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("CPI1")));
    zeroCouponInflationIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("CPI2")));
    zeroCouponInflationIds.put(Tenor.THREE_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("CPI3")));
    zeroCouponInflationIds.put(Tenor.FOUR_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("CPI4")));
    final Map<Tenor, CurveInstrumentProvider> deliverableSwapFutureNodeIds = new HashMap<>();
    deliverableSwapFutureNodeIds.put(Tenor.TWO_YEARS, new BloombergFutureCurveInstrumentProvider("SW", "p"));
    deliverableSwapFutureNodeIds.put(Tenor.FIVE_YEARS, new BloombergFutureCurveInstrumentProvider("SW", "e"));
    deliverableSwapFutureNodeIds.put(Tenor.TEN_YEARS, new BloombergFutureCurveInstrumentProvider("SW", "k"));
    final Map<Tenor, CurveInstrumentProvider> threeLegBasisSwapIds = new HashMap<>();
    threeLegBasisSwapIds.put(Tenor.ONE_YEAR, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("EUR1")));
    threeLegBasisSwapIds.put(Tenor.TWO_YEARS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("EUR2")));
    final CurveNodeIdMapper mapper = CurveNodeIdMapper.builder().name(name)
        .billNodeIds(billIds)
        .bondNodeIds(bondIds)
        .cashNodeIds(cashIds)
        .continuouslyCompoundedRateNodeIds(continuouslyCompoundedRateIds)
        .periodicallyCompoundedRateNodeIds(periodicallyCompoundedRateIds)
        .creditSpreadNodeIds(creditSpreadIds)
        .deliverableSwapFutureNodeIds(deliverableSwapFutureNodeIds)
        .discountFactorNodeIds(discountFactorIds)
        .fraNodeIds(fraIds)
        .fxForwardNodeIds(fxForwardIds)
        .immFRANodeIds(immFRAIds)
        .immSwapNodeIds(immSwapIds)
        .rateFutureNodeIds(rateFutureIds)
        .swapNodeIds(swapIds)
        .threeLegBasisSwapNodeIds(threeLegBasisSwapIds)
        .zeroCouponInflationNodeIds(zeroCouponInflationIds).build();
    assertEquals(mapper, cycleObject(CurveNodeIdMapper.class, mapper));
  }
}
