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
import com.opengamma.financial.analytics.ircurve.StaticCurvePointsInstrumentProvider;
import com.opengamma.financial.analytics.ircurve.strips.DataFieldType;
import com.opengamma.util.test.TestGroup;

/**
 * 
 */
@Test(groups = TestGroup.UNIT)
public class StaticCurvePointsInstrumentProviderFudgeBuilderTest extends AnalyticsTestBase {

  @Test
  public void test() {
    final StaticCurvePointsInstrumentProvider provider = new StaticCurvePointsInstrumentProvider(ExternalSchemes.syntheticSecurityId("ASD"), "Market_Value", DataFieldType.POINTS,
        ExternalSchemes.syntheticSecurityId("QWE"), "Last_Price");
    assertEquals(provider, cycleObject(StaticCurvePointsInstrumentProvider.class, provider));
  }
}
