/*
 * Copyright (C) 2014 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.BloombergFutureCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.FutureMonthCodeCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.id.ExternalScheme;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class FutureMonthCodeCurveInstrumentProviderFudgeBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    ExternalScheme ric = ExternalScheme.of("RIC");
    FutureMonthCodeCurveInstrumentProvider provider = new FutureMonthCodeCurveInstrumentProvider("AB", "Sector", ric);
    assertEquals(provider, cycleObject(FutureMonthCodeCurveInstrumentProvider.class, provider));
    provider = new FutureMonthCodeCurveInstrumentProvider("AB", "Sector", "LAST_CLOSE", DataFieldType.POINTS, ric);
    assertEquals(provider, cycleObject(FutureMonthCodeCurveInstrumentProvider.class, provider));
  }
}
