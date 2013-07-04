/**
 * Copyright (C) 2013 - present by OpenGamma Inc. and the OpenGamma group of companies
 *
 * Please see distribution for license.
 */
package com.opengamma.financial.fudgemsg;

import static org.testng.AssertJUnit.assertEquals;

import org.testng.annotations.Test;

import com.opengamma.core.id.ExternalSchemes;
import com.opengamma.financial.analytics.fudgemsg.AnalyticsTestBase;
import com.opengamma.financial.analytics.ircurve.StaticCurveInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.util.test.TestGroup;

/**
 *
 */
@Test(groups = TestGroup.UNIT)
public class StaticCurveInstrumentProviderFudgeBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    StaticCurveInstrumentProvider provider = new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("ABCD"));
    assertEquals(provider, cycleObject(StaticCurveInstrumentProvider.class, provider));
    provider = new StaticCurveInstrumentProvider(ExternalSchemes.syntheticSecurityId("EFGH"), "LAST_CLOSE", DataFieldType.POINTS);
    assertEquals(provider, cycleObject(StaticCurveInstrumentProvider.class, provider));
  }
}
