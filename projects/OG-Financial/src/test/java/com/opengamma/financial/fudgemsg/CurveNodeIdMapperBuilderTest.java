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
import com.opengamma.financial.analytics.ircurve.CurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.util.test.TestGroup;
import com.opengamma.util.time.Tenor;

/**
 * Test.
 */
@Test(groups = TestGroup.UNIT)
public class CurveNodeIdMapperBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    final Map<Tenor, CurveInstrumentProvider> creditSpreadIds = new HashMap<>();
    creditSpreadIds.put(Tenor.ONE_MONTH, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("ABC")));
    creditSpreadIds.put(Tenor.TWO_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("DEF")));
    creditSpreadIds.put(Tenor.THREE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("GHI")));
    creditSpreadIds.put(Tenor.FOUR_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("JKL")));
    creditSpreadIds.put(Tenor.FIVE_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("MNO")));
    creditSpreadIds.put(Tenor.SIX_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("PQR")));
    creditSpreadIds.put(Tenor.SEVEN_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("STU")));
    creditSpreadIds.put(Tenor.EIGHT_MONTHS, new StaticCurveInstrumentProvider(ExternalSchemes.bloombergTickerSecurityId("VWX")));
    final CurveNodeIdMapper mapper = new CurveNodeIdMapper(creditSpreadIds);
    assertEquals(mapper, cycleObject(CurveNodeIdMapper.class, mapper));
  }
}
